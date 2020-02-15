package com.tiyb.tev.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang3.StringUtils;

import com.tiyb.tev.controller.TEVPostRestController;
import com.tiyb.tev.datamodel.Answer;
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.datamodel.Video;
import com.tiyb.tev.exception.ResourceNotFoundException;
import com.tiyb.tev.exception.XMLParsingException;

/**
 * <p>
 * This class is responsible for parsing the XML file exported from Tumblr for a user's
 * <b>blogs</b>. It leverages the <b>StAX</b> library's event-based processing model to read the
 * file.
 * </p>
 *
 * <p>
 * The XML export from Tumblr is... not always a well thought out piece of information architecture.
 * There are inconsistencies as to how some things are structured, and so it had to be reverse
 * engineered to figure things out. Especially when it comes to "photo" posts, where there are
 * inconsistent ways in which links to photos are handled. That being said, the <i>general</i>
 * structure is that there is a <code>&lt;post&gt;</code> element for each post, with attributes for
 * all of the data that is common to any kind of a post (URL, "slug," date it was posted in a couple
 * of different formats, etc.). Under that <code>&lt;post&gt;</code> element are child elements,
 * where the child elements to be found are very different depending on the type of post. An
 * "answer" post, for example, will have two child elements (for the question and the answer),
 * whereas a "photo" post will have numerous child elements covering the post's caption, various
 * sizes of the images, etc.
 * </p>
 *
 * <p>
 * Not all of the attributes or elements in the document are pulled out, only the ones that are
 * useful for the TEV application (or deemed to be potentially useful). Probably about 90% of the
 * information is retrieved, with the rest being discarded.
 * </p>
 *
 * <p>
 * The general approach taken is that the
 * {@link #parseDocument(InputStream, TEVPostRestController, String) parseDocument()} method sets up
 * some initial variables, the
 * {@link #readPosts(InputStream, TEVPostRestController, boolean, String) readPosts()} method then
 * goes through the document post-by-post, and as it determines what type each post is, additional
 * methods are called to read the additional, type-specific XML within the post's XML element
 * (answer, link, photo, etc.).
 * </p>
 *
 * @author tiyb
 */
public class BlogXmlReader extends TEVXmlReader {

    /**
     * String used for separating tags, when combining them together in one string
     */
    public static final String TAG_COMMA_SEPARATOR = ", ";

    /**
     * XML tag name for the given element
     */
    public static final String VIDEO_TAG_CAPTION = "video-caption";

    /**
     * XML tag name for the given element
     */
    public static final String VIDEO_TAG_REVISION = "revision";

    /**
     * XML tag name for the given element
     */
    public static final String VIDEO_TAG_DURATION = "duration";

    /**
     * XML tag name for the given element
     */
    public static final String VIDEO_TAG_HEIGHT = "height";

    /**
     * XML tag name for the given element
     */
    public static final String VIDEO_TAG_WIDTH = "width";

    /**
     * XML tag name for the given element
     */
    public static final String VIDEO_TAG_EXTENSION = "extension";

    /**
     * XML tag name for the given element
     */
    public static final String VIDEO_TAG_CONTENTTYPE = "content-type";

    /**
     * XML attribute name for the given element
     */
    public static final String PHOTO_ATTRIBUTE_OFFSET = "offset";

    /**
     * XML tag name for the given element
     */
    public static final String PHOTO_TAG_PHOTO = "photo";

    /**
     * XML tag name for the given element
     */
    public static final String PHOTO_TAG_LINKURL = "photo-link-url";

    /**
     * XML tag name for the given element
     */
    public static final String PHOTO_TAG_PHOTOSET = "photoset";

    /**
     * One of the standard Tumblr photo sizes
     */
    public static final String PHOTO_SIZE_75 = "75";

    /**
     * One of the standard Tumblr photo sizes
     */
    public static final String PHOTO_SIZE_100 = "100";

    /**
     * One of the standard Tumblr photo sizes
     */
    public static final String PHOTO_SIZE_250 = "250";

    /**
     * One of the standard Tumblr photo sizes
     */
    public static final String PHOTO_SIZE_400 = "400";

    /**
     * One of the standard Tumblr photo sizes
     */
    public static final String PHOTO_SIZE_500 = "500";

    /**
     * One of the standard Tumblr photo sizes
     */
    public static final String PHOTO_SIZE_1280 = "1280";

    /**
     * XML attribute name for the given element
     */
    public static final String PHOTO_ATTRIBUTE_MAXWIDTH = "max-width";

    /**
     * XML attribute name for the given element
     */
    public static final String PHOTO_ATTRIBUTE_WIDTH = "width";

    /**
     * XML attribute name for the given element
     */
    public static final String PHOTO_ATTRIBUTE_HEIGHT = "height";

    /**
     * XML attribute name for the given element
     */
    public static final String PHOTO_ATTRIBUTE_CAPTION = "caption";

    /**
     * XML tag name for the given element
     */
    public static final String PHOTO_TAG_URL = "photo-url";

    /**
     * XML tag name for the given element
     */
    public static final String PHOTO_TAG_CAPTION = "photo-caption";

    /**
     * XML tag name for the given element
     */
    public static final String LINK_TAG_URL = "link-url";

    /**
     * XML tag name for the given element
     */
    public static final String LINK_TAG_TEXT = "link-text";

    /**
     * XML tag name for the given element
     */
    public static final String LINK_TAG_DESCRIPTION = "link-description";

    /**
     * XML tag name for the given element
     */
    public static final String ANSWER_ANSWER_TAG = "answer";

    /**
     * XML tag name for the given element
     */
    public static final String ANSWER_QUESTION_TAG = "question";

    /**
     * XML tag name for the given element
     */
    public static final String POST_TAG_HASHTAG = "tag";

    /**
     * XML tag name for the given element
     */
    public static final String REGULAR_TAG_BODY = "regular-body";

    /**
     * XML tag name for the given element
     */
    public static final String REGULAR_TAG_TITLE = "regular-title";

    /**
     * XML attribute name for the given element
     */
    public static final String POST_ATTRIBUTE_HEIGHT = "height";

    /**
     * XML attribute name for the given element
     */
    public static final String POST_ATTRIBUTE_WIDTH = "width";

    /**
     * XML attribute name for the given element
     */
    public static final String POST_ATTRIBUTE_TUMBLELOG = "tumblelog";

    /**
     * XML attribute name for the given element
     */
    public static final String POST_ATTRIBUTE_ISREBLOG = "is_reblog";

    /**
     * XML attribute name for the given element
     */
    public static final String POST_ATTRIBUTE_STATE = "state";

    /**
     * XML attribute name for the given element
     */
    public static final String POST_ATTRIBUTE_SLUG = "slug";

    /**
     * XML attribute name for the given element
     */
    public static final String POST_ATTRIBUTE_REBLOGKEY = "reblog-key";

    /**
     * XML attribute name for the given element
     */
    public static final String POST_ATTRIBUTE_FORMAT = "format";

    /**
     * XML attribute name for the given element
     */
    public static final String POST_ATTRIBUTE_UNIXTIMESTAMP = "unix-timestamp";

    /**
     * XML attribute name for the given element
     */
    public static final String POST_ATTRIBUTE_DATE = "date";

    /**
     * XML attribute name for the given element
     */
    public static final String POST_ATTRIBUTE_DATEGMT = "date-gmt";

    /**
     * XML attribute name for the given element
     */
    public static final String POST_ATTRIBUTE_TYPE = "type";

    /**
     * XML attribute name for the given element
     */
    public static final String POST_ATTRIBUTE_URLWITHSLUG = "url-with-slug";

    /**
     * XML attribute name for the given element
     */
    public static final String POST_ATTRIBUTE_URL = "url";

    /**
     * XML attribute name for the given element
     */
    public static final String POST_ATTRIBUTE_ID = "id";

    /**
     * XML tag name for the given element
     */
    public static final String POST_TAG_NAME = "post";

    /**
     * <p>
     * This is the main method of the class, which kicks off the processing of the document. It
     * doesn't do much work itself, it simply calls the
     * {@link #readPosts(InputStream, TEVPostRestController, boolean, String) readPosts()} method to
     * get into the actual XML document.
     * </p>
     *
     * <p>
     * The one piece of logic actually performed by this method is to delete all of the data in the
     * database (via the REST controller), if the "overwrite post data" option is set.
     * </p>
     *
     * @param xmlFile        {@link java.io.InputStream InputStream} containing the XML document to
     *                       be parsed.
     * @param postController REST controller for the application, used for storing data
     * @throws XMLParsingException For any errors in parsing the XML document, business or technical
     */
    public static void parseDocument(final InputStream xmlFile, final TEVPostRestController postController,
            final String blogName) throws XMLParsingException {
        final boolean isOverwritePosts =
                postController.getMdController().getMetadataForBlog(blogName).getOverwritePostData();

        if (isOverwritePosts) {
            postController.getRegController().deleteAllRegularsForBlog(blogName);
            postController.getAnswerController().deleteAllAnswersForBlog(blogName);
            postController.getLinkController().deleteAllLinksForBlog(blogName);
            postController.getPhotoController().deleteAllPhotosForBlog(blogName);
            postController.getVideoController().deleteAllVideosForBlog(blogName);
            postController.deleteAllPostsForBlog(blogName);
            postController.deleteAllHashtagsForBlog(blogName);
            logger.debug("Previous content deleted as part of post XML import");
        }

        readPosts(xmlFile, postController, isOverwritePosts, blogName);
    }

    /**
     * <p>
     * This is the first method that starts to get into the work of actually reading the XML file.
     * Luckily, the structure of the <code>&lt;post&gt;</code> element is such that the generic,
     * post-related information is contained in attributes, while the data that changes based on
     * type is contained in child elements. This means that parsing the <code>&lt;post&gt;</code>
     * element works like this:
     * </p>
     *
     * <ol>
     * <li>As the "start element" event is encountered for each post, a new
     * {@link com.tiyb.tev.datamodel.Post Post} object is created</li>
     * <li>The attributes are read into that object via the
     * {@link #readPostAttributes(StartElement, Post) readPostAttributes()} method to populate its
     * data</li>
     * <li>The post is inserted into the DB via the REST controller.
     * <ul>
     * <li>If the "overwrite posts" option is set in the metadata, the logic first checks to see if
     * the post already exists, and only inserts it if it doesn't.</li>
     * <li>A boolean value is set, based on this logic, so that insertions of subsequent data for
     * this post doesn't have to figure it out all over again.</li>
     * </ul>
     * </li>
     * <li>Depending on the value of the <code>type</code> attribute, one of the additional methods
     * is called to parse the type-specific data</li>
     * <li>Those child methods populate their own objects, and then use the REST controller to
     * insert the data into the DB
     * <ul>
     * <li>The previously set boolean is checked first, before submitting the data.</li>
     * </ul>
     * </li>
     * <li>A final update of the post is done. This is necessary because tags for the post were
     * discovered <i>after</i> the post was originally inserted.</li>
     * </ol>
     *
     * @param xmlFile            The stream containing the XML file to be parsed
     * @param postRestController REST controller used for storing the data
     * @param isOverwritePosts   Indicates whether this is a clean upload, or additive; the REST
     *                           controller could have been used to determine this, but since the
     *                           calling method needed to figure it out anyway, it was just as easy
     *                           to pass it as a parameter.
     * @param blogName           Name of the blog which is being read in
     * @throws XMLParsingException For any XML parsing errors
     */
    private static void readPosts(final InputStream xmlFile, final TEVPostRestController postRestController,
            final boolean isOverwritePosts, final String blogName) throws XMLParsingException {

        try {
            final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            final XMLEventReader reader = inputFactory.createXMLEventReader(xmlFile);

            Post post = null;

            while (reader.hasNext()) {
                final XMLEvent event = reader.nextEvent();

                if (event.isStartElement()) {
                    final StartElement se = event.asStartElement();

                    if (se.getName().getLocalPart().equals(POST_TAG_NAME)) {
                        post = new Post();
                        boolean isSubmitablePost = true;
                        readPostAttributes(se, post);
                        assert blogName.equals(post.getTumblelog());
                        if (isOverwritePosts) {
                            postRestController.createPostForBlog(post.getTumblelog(), post);
                        } else {
                            try {
                                final Post serverPost =
                                        postRestController.getPostForBlogById(post.getTumblelog(), post.getId());
                                if (!serverPost.getState().equals(post.getState())) {
                                    isSubmitablePost = true;
                                } else if (!serverPost.getDate().equals(post.getDate())) {
                                    isSubmitablePost = true;
                                } else if (!serverPost.getDateGmt().equals(post.getDateGmt())) {
                                    isSubmitablePost = true;
                                } else if (!serverPost.getUnixtimestamp().equals(post.getUnixtimestamp())) {
                                    isSubmitablePost = true;
                                } else {
                                    isSubmitablePost = false;
                                }
                            } catch (ResourceNotFoundException e) {
                                postRestController.createPostForBlog(post.getTumblelog(), post);
                            }
                        }
                        switch (post.getType()) {
                        case Post.POST_TYPE_REGULAR:
                            final Regular regular = readRegular(reader, post, postRestController);
                            if (isSubmitablePost) {
                                postRestController.getRegController().createRegularForBlog(post.getTumblelog(),
                                        post.getId(), regular);
                            }
                            break;
                        case Post.POST_TYPE_ANSWER:
                            final Answer answer = readAnswer(reader, post, postRestController);
                            if (isSubmitablePost) {
                                postRestController.getAnswerController().createAnswerForBlog(post.getTumblelog(),
                                        post.getId(), answer);
                            }
                            break;
                        case Post.POST_TYPE_LINK:
                            final Link link = readLink(reader, post, postRestController);
                            if (isSubmitablePost) {
                                postRestController.getLinkController().createLinkForBlog(post.getTumblelog(),
                                        post.getId(), link);
                            }
                            break;
                        case Post.POST_TYPE_PHOTO:
                            final List<Photo> photos = readPhotos(reader, post, postRestController);
                            if (isSubmitablePost) {
                                for (Photo p : photos) {
                                    postRestController.getPhotoController().createPhotoForBlog(post.getTumblelog(), p);
                                }
                            }
                            break;
                        case Post.POST_TYPE_VIDEO:
                            final Video video = readVideos(reader, post, postRestController);
                            if (isSubmitablePost) {
                                postRestController.getVideoController().createVideoForBlog(post.getTumblelog(),
                                        post.getId(), video);
                            }
                            break;
                        default:
                            logger.error("Invalid post type encountered");
                            throw new XMLParsingException();
                        }
                        if (isSubmitablePost) {
                            post = postRestController.updatePostForBlog(post.getTumblelog(), post.getId(), post);
                            if (post.getTags().length() > 0) {
                                final List<String> individualTags = Arrays.asList(post.getTags().split(","));
                                for (String tag : individualTags) {
                                    tag = tag.trim();
                                    if (tag.equals(StringUtils.EMPTY)) {
                                        logger.error("A hashtag was empty from this list: {}", post.getTags());
                                    }
                                    postRestController.createHashtagForBlog(blogName, tag);
                                }
                            }
                        }
                    }
                }
            }
        } catch (XMLStreamException e) {
            logger.error("XML parser threw error: ", e);
            throw new XMLParsingException();
        }
    }

    /**
     * Helper function specifically for reading the attributes from a <code>&lt;post&gt;</code>
     * element. The logic could easily have been incorporated into
     * {@link #readPosts(InputStream, TEVPostRestController, boolean, String) readPosts()}, but the
     * method would have gotten much longer.
     *
     * @param startElement The {@link javax.xml.stream.events.StartElement StartElement} object
     *                     being processed
     * @param post         The {@link com.tiyb.tev.datamodel.Post Post} object to which the data
     *                     from each element should be added
     */
    private static void readPostAttributes(final StartElement startElement, final Post post) {
        @SuppressWarnings("unchecked")
        final Iterator<Attribute> atts = startElement.getAttributes();

        while (atts.hasNext()) {
            final Attribute att = atts.next();
            final String attName = att.getName().getLocalPart();

            switch (attName) {
            case POST_ATTRIBUTE_ID:
                post.setId(Long.parseLong(att.getValue()));
                break;
            case POST_ATTRIBUTE_URL:
                post.setUrl(att.getValue());
                break;
            case POST_ATTRIBUTE_URLWITHSLUG:
                post.setUrlWithSlug(att.getValue());
                break;
            case POST_ATTRIBUTE_TYPE:
                post.setType(att.getValue());
                break;
            case POST_ATTRIBUTE_DATEGMT:
                post.setDateGmt(att.getValue());
                break;
            case POST_ATTRIBUTE_DATE:
                post.setDate(att.getValue());
                break;
            case POST_ATTRIBUTE_UNIXTIMESTAMP:
                post.setUnixtimestamp(Long.parseLong(att.getValue()));
                break;
            case POST_ATTRIBUTE_FORMAT:
                // attribute not used
                break;
            case POST_ATTRIBUTE_REBLOGKEY:
                post.setReblogKey(att.getValue());
                break;
            case POST_ATTRIBUTE_SLUG:
                post.setSlug(att.getValue());
                break;
            case POST_ATTRIBUTE_STATE:
                post.setState(att.getValue());
                break;
            case POST_ATTRIBUTE_ISREBLOG:
                post.setIsReblog(Boolean.parseBoolean(att.getValue()));
                break;
            case POST_ATTRIBUTE_TUMBLELOG:
                post.setTumblelog(att.getValue());
                break;
            case POST_ATTRIBUTE_WIDTH:
                post.setWidth(Integer.parseInt(att.getValue()));
                break;
            case POST_ATTRIBUTE_HEIGHT:
                post.setHeight(Integer.parseInt(att.getValue()));
                break;
            default:
                // ignore any other attributes
            }
        }

    }

    /**
     * This method is responsible for reading the sub-content under a post for content related to a
     * "regular" post. The content is very simple, containing only "regular-title" and
     * "regular-body" elements.
     *
     * @param reader             The {@link javax.xml.stream.XMLEventReader XMLEventReader} object
     *                           for the XML document being parsed.
     * @param post               The current post, for use as the primary key of the Regular object
     *                           and for setting tags
     * @param postRestController The REST controller for working with posts, used for inserting
     *                           Hashtags into the DB
     * @return A {@link com.tiyb.tev.datamodel.Regular Regular} object with the data read
     * @throws XMLStreamException For any XML parsing exceptions
     */
    private static Regular readRegular(final XMLEventReader reader, final Post post,
            final TEVPostRestController postRestController) throws XMLStreamException {
        final Regular regular = new Regular();
        regular.setPostId(post.getId());

        while (reader.hasNext()) {
            final XMLEvent event = reader.nextEvent();

            if (event.isStartElement()) {
                final StartElement se = event.asStartElement();

                if (se.getName().getLocalPart().equals(REGULAR_TAG_TITLE)) {
                    regular.setTitle(readCharacters(reader));
                } else if (se.getName().getLocalPart().equals(REGULAR_TAG_BODY)) {
                    regular.setBody(readCharacters(reader));
                } else if (se.getName().getLocalPart().equals(POST_TAG_HASHTAG)) {
                    final String hashtag = readCharacters(reader);
                    post.setTags(addTagToString(post.getTags(), hashtag));
                }
            } else if (event.isEndElement()) {
                final EndElement ee = event.asEndElement();

                if (ee.getName().getLocalPart().equals(POST_TAG_NAME)) {
                    return regular;
                }
            }
        }

        logger.error(UNEXPECTED_EOF_LOG, "readRegular");
        throw new XMLStreamException(END_OF_FILE_ERROR);
    }

    /**
     * This method is responsible for reading the sub-content under a post for content related to an
     * "answer" post. The content is very simple, containing only "question" and "answer" elements.
     *
     * @param reader             The event reader parsing the current document
     * @param post               The current post, for use as the primary key of the Answer object
     *                           and for setting tags
     * @param postRestController The REST controller for working with posts, for inserting hashtags
     * @return The "answer" data
     * @throws XMLStreamException For any XML exceptions encountered
     */
    private static Answer readAnswer(final XMLEventReader reader, final Post post,
            final TEVPostRestController postRestController) throws XMLStreamException {
        final Answer answer = new Answer();
        answer.setPostId(post.getId());

        while (reader.hasNext()) {
            final XMLEvent event = reader.nextEvent();

            if (event.isStartElement()) {
                final StartElement se = event.asStartElement();

                if (se.getName().getLocalPart().equals(ANSWER_QUESTION_TAG)) {
                    answer.setQuestion(readCharacters(reader));
                } else if (se.getName().getLocalPart().equals(ANSWER_ANSWER_TAG)) {
                    answer.setAnswer(readCharacters(reader));
                } else if (se.getName().getLocalPart().equals(POST_TAG_HASHTAG)) {
                    final String tag = readCharacters(reader);
                    post.setTags(addTagToString(post.getTags(), tag));
                }
            } else if (event.isEndElement()) {
                final EndElement ee = event.asEndElement();

                if (ee.getName().getLocalPart().equals(POST_TAG_NAME)) {
                    return answer;
                }
            }
        }

        logger.error(UNEXPECTED_EOF_LOG, "readAnswer");
        throw new XMLStreamException(END_OF_FILE_ERROR);
    }

    /**
     * This method is responsible for reading the sub-content under a post for content related to a
     * "link" post. The content contains only three elements: "link-description", "link-text", and
     * "link-url".
     *
     * @param reader             The event parser
     * @param post               The currently processed post
     * @param postRestController The REST controller for working with posts, for inserting hashtags
     * @return The link data
     * @throws XMLStreamException For any XML parsing errors encountered
     */
    private static Link readLink(final XMLEventReader reader, final Post post,
            final TEVPostRestController postRestController) throws XMLStreamException {
        final Link link = new Link();
        link.setPostId(post.getId());

        while (reader.hasNext()) {
            final XMLEvent event = reader.nextEvent();

            if (event.isStartElement()) {
                final StartElement se = event.asStartElement();

                if (se.getName().getLocalPart().equals(LINK_TAG_DESCRIPTION)) {
                    link.setDescription(readCharacters(reader));
                } else if (se.getName().getLocalPart().equals(LINK_TAG_TEXT)) {
                    link.setText(readCharacters(reader));
                } else if (se.getName().getLocalPart().equals(LINK_TAG_URL)) {
                    link.setUrl(readCharacters(reader));
                } else if (se.getName().getLocalPart().equals(POST_TAG_HASHTAG)) {
                    final String tag = readCharacters(reader);
                    post.setTags(addTagToString(post.getTags(), tag));
                }
            } else if (event.isEndElement()) {
                final EndElement ee = event.asEndElement();

                if (ee.getName().getLocalPart().equals(POST_TAG_NAME)) {
                    return link;
                }
            }
        }

        logger.error(UNEXPECTED_EOF_LOG, "readLink");
        throw new XMLStreamException(END_OF_FILE_ERROR);
    }

    /**
     * <p>
     * This method is responsible for reading the sub-content under a post for content related to a
     * "photo" post. This is the most complex of the post types, as far as the XML is concerned, and
     * is also the least consistent. There is one child element that always appears
     * ("photo-caption"), and then there is content for each photo within the post. For each photo,
     * there are 6 "photo-url" elements, for URLs to differently sized versions of the same image
     * (1280, 500, 400, ...).
     * </p>
     *
     * <p>
     * The tricky part, for "photo" posts, is that there could be <i>one</i> photo, or there could
     * be <i>multiple</i> (each with 6 "photo-url" elements), and the structure is different for
     * both cases:
     * </p>
     *
     * <ul>
     * <li>If there is only one photo, it is represented by simply appending the 6 "photo-url"
     * elements right to the main "post" element</li>
     * <li>If there are multiple photos, Tumblr treats it like a <b>photoset</b>, in which case a
     * "photoset" element is added to the "post", under which is one "photo" element for each photo,
     * under which are the 6 "photo-url" elements (the same as for a single photo).</li>
     * <li>Even in the case where there is a "photoset", the <i>first</i> image in that photoset is
     * duplicated, as 6 "photo-url" elements under the "post" element. These "photo-url" elements
     * come before the "photoset", so they're read before the XML parser even realizes that there is
     * a "photoset" coming.</li>
     * </ul>
     *
     * <p>
     * In order to deal with this complexity, processing of a "photo" post works as follows:
     * </p>
     *
     * <ol>
     * <li>The "photo-caption" element is read in. (It is added to every <code>Photo</code> object
     * created, which is not optimal, but... still.)</li>
     * <li>Any "photo-url" elements are read in, and the information is saved for later use</li>
     * <li><i>If</i> there is a "photoset" element, a sub-method is called for parsing the content.
     * That method will create a {@link com.tiyb.tev.datamodel.Photo Photo} object for each photo it
     * encounters.</li>
     * <li>When the end of the "post" element is reached, the list of
     * {@link com.tiyb.tev.datamodel.Photo Photo} objects is checked, to see if any were created (as
     * part of a photoset) or not; if not, a single {@link com.tiyb.tev.datamodel.Photo Photo}
     * object is created, with the URL and caption data already collected, and put in the
     * collection. If the collection is already populated, it is simply returned as-is, since the
     * "photo-url" elements at the root of the <code>&lt;post&gt;</code> element are duplicated in
     * the "photoset", and have therefore already been added.</li>
     * </ol>
     *
     * @param reader             The event reader being used to parse the XML
     * @param post               The currently processed post
     * @param postRestController The REST controller for working with posts, for inserting hashtags
     * @return A list of {@link com.tiyb.tev.datamodel.Photo Photo} objects
     * @throws XMLStreamException For any XML parsing errors
     */
    private static List<Photo> readPhotos(final XMLEventReader reader, final Post post,
            final TEVPostRestController postRestController) throws XMLStreamException {
        final List<Photo> photos = new ArrayList<Photo>();
        String caption = StringUtils.EMPTY;
        String url1280 = StringUtils.EMPTY;
        String url500 = StringUtils.EMPTY;
        String url400 = StringUtils.EMPTY;
        String url250 = StringUtils.EMPTY;
        String url100 = StringUtils.EMPTY;
        String url75 = StringUtils.EMPTY;
        String photoLinkUrl = null;

        while (reader.hasNext()) {
            final XMLEvent event = reader.nextEvent();

            if (event.isStartElement()) {
                final StartElement se = event.asStartElement();

                if (se.getName().getLocalPart().equals(PHOTO_TAG_CAPTION)) {
                    caption = readCharacters(reader);
                } else if (se.getName().getLocalPart().equalsIgnoreCase(PHOTO_TAG_URL)) {
                    final String width = se.getAttributeByName(new QName(PHOTO_ATTRIBUTE_MAXWIDTH)).getValue();
                    switch (width) {
                    case PHOTO_SIZE_1280:
                        url1280 = readCharacters(reader);
                        break;
                    case PHOTO_SIZE_500:
                        url500 = readCharacters(reader);
                        break;
                    case PHOTO_SIZE_400:
                        url400 = readCharacters(reader);
                        break;
                    case PHOTO_SIZE_250:
                        url250 = readCharacters(reader);
                        break;
                    case PHOTO_SIZE_100:
                        url100 = readCharacters(reader);
                        break;
                    case PHOTO_SIZE_75:
                        url75 = readCharacters(reader);
                        break;
                    default:
                        // should not be reached
                    }
                } else if (se.getName().getLocalPart().equals(PHOTO_TAG_PHOTOSET)) {
                    readPhotoStream(reader, post.getId(), caption, photos);
                } else if (se.getName().getLocalPart().equals(POST_TAG_HASHTAG)) {
                    final String tag = readCharacters(reader);
                    post.setTags(addTagToString(post.getTags(), tag));
                } else if (se.getName().getLocalPart().equals(PHOTO_TAG_LINKURL)) {
                    photoLinkUrl = readCharacters(reader);
                }
            } else if (event.isEndElement()) {
                final EndElement ee = event.asEndElement();

                if (ee.getName().getLocalPart().equals(POST_TAG_NAME)) {
                    if (photos.size() < 1) {
                        final Photo photo = new Photo();
                        photo.setPostId(post.getId());
                        photo.setCaption(caption);
                        photo.setUrl1280(url1280);
                        photo.setUrl100(url100);
                        photo.setUrl250(url250);
                        photo.setUrl400(url400);
                        photo.setUrl500(url500);
                        photo.setUrl75(url75);
                        photo.setPhotoLinkUrl(photoLinkUrl);

                        photos.add(photo);
                    }

                    return photos;
                }
            }
        }

        logger.error(UNEXPECTED_EOF_LOG, "readPhotos");
        throw new XMLStreamException(END_OF_FILE_ERROR);
    }

    /**
     * This method is responsible for reading the sub-components of a "photo" post specific to the
     * "photoset". Within a photoset, there are multiple "photo" elements, each of which contains 6
     * "photo-url" elements, for the different sizes of that photo. Because there is some
     * duplication in the model, the caption is passed from the parent method into this one, and
     * added to each photo.
     *
     * @param reader       The event reader used for parsing
     * @param postID       The ID of the currently processed post
     * @param photoCaption The caption to be used on each photo
     * @param photos       The list of photos that already exists, to which new photos will be
     *                     added.
     * @throws XMLStreamException For any XML parsing errors
     */
    private static List<Photo> readPhotoStream(final XMLEventReader reader, final Long postID,
            final String photoCaption, final List<Photo> photos) throws XMLStreamException {
        Photo currentPhoto = new Photo();

        while (reader.hasNext()) {
            final XMLEvent event = reader.nextEvent();

            if (event.isStartElement()) {
                final StartElement se = event.asStartElement();

                if (se.getName().getLocalPart().equals(PHOTO_TAG_PHOTO)) {
                    currentPhoto = new Photo();
                    currentPhoto.setPostId(postID);
                    currentPhoto.setCaption(photoCaption);
                    final String offset = se.getAttributeByName(new QName(PHOTO_ATTRIBUTE_OFFSET)).getValue();
                    final String width = se.getAttributeByName(new QName(PHOTO_ATTRIBUTE_WIDTH)).getValue();
                    final String height = se.getAttributeByName(new QName(PHOTO_ATTRIBUTE_HEIGHT)).getValue();
                    currentPhoto.setOffset(offset);
                    currentPhoto.setWidth(Integer.valueOf(width));
                    currentPhoto.setHeight(Integer.valueOf(height));
                } else if (se.getName().getLocalPart().equals(PHOTO_TAG_URL)) {
                    final String maxWidth = se.getAttributeByName(new QName(PHOTO_ATTRIBUTE_MAXWIDTH)).getValue();
                    switch (maxWidth) {
                    case PHOTO_SIZE_1280:
                        currentPhoto.setUrl1280(readCharacters(reader));
                        break;
                    case PHOTO_SIZE_500:
                        currentPhoto.setUrl500(readCharacters(reader));
                        break;
                    case PHOTO_SIZE_400:
                        currentPhoto.setUrl400(readCharacters(reader));
                        break;
                    case PHOTO_SIZE_250:
                        currentPhoto.setUrl250(readCharacters(reader));
                        break;
                    case PHOTO_SIZE_100:
                        currentPhoto.setUrl100(readCharacters(reader));
                        break;
                    case PHOTO_SIZE_75:
                        currentPhoto.setUrl75(readCharacters(reader));
                        break;
                    default:
                        // should not be reached
                    }
                }
            } else if (event.isEndElement()) {
                final EndElement ee = event.asEndElement();

                if (ee.getName().getLocalPart().equals(PHOTO_TAG_PHOTO)) {
                    photos.add(currentPhoto);
                } else if (ee.getName().getLocalPart().equals(PHOTO_TAG_PHOTOSET)) {
                    return photos;
                }
            }
        }

        logger.error(UNEXPECTED_EOF_LOG, "readPhotoStream");
        throw new XMLStreamException(END_OF_FILE_ERROR);
    }

    /**
     * This method is responsible for reading the sub-content under a post for content related to a
     * "video" post. The content contains a number of elements, most of which are simply read into a
     * property of the <code>Video</code> object
     *
     * <ul>
     * <li>content-type</li>
     * <li>extension (the filename extension)</li>
     * <li>width</li>
     * <li>height</li>
     * <li>duration</li>
     * <li>revision</li>
     * <li>video-caption (the body of the post)</li>
     * <li>multiple "video-player" elements (for different sizes of the video) which are
     * ignored</li>
     * </ul>
     *
     * @param reader             The event parser
     * @param post               The currently processed post
     * @param postRestController The REST controller for working with posts, for inserting hashtags
     * @return The video data
     * @throws XMLStreamException For any XML parsing errors
     */
    private static Video readVideos(final XMLEventReader reader, final Post post,
            final TEVPostRestController postRestController) throws XMLStreamException {
        final Video video = new Video();
        video.setPostId(post.getId());

        while (reader.hasNext()) {
            final XMLEvent event = reader.nextEvent();

            if (event.isStartElement()) {
                final StartElement se = event.asStartElement();

                if (se.getName().getLocalPart().equals(VIDEO_TAG_CONTENTTYPE)) {
                    video.setContentType(readCharacters(reader));
                } else if (se.getName().getLocalPart().equals(VIDEO_TAG_EXTENSION)) {
                    video.setExtension(readCharacters(reader));
                } else if (se.getName().getLocalPart().equals(VIDEO_TAG_WIDTH)) {
                    video.setWidth(Integer.parseInt(readCharacters(reader)));
                } else if (se.getName().getLocalPart().equals(VIDEO_TAG_HEIGHT)) {
                    video.setHeight(Integer.parseInt(readCharacters(reader)));
                } else if (se.getName().getLocalPart().equals(VIDEO_TAG_DURATION)) {
                    video.setDuration(Integer.parseInt(readCharacters(reader)));
                } else if (se.getName().getLocalPart().equals(VIDEO_TAG_REVISION)) {
                    video.setRevision(readCharacters(reader));
                } else if (se.getName().getLocalPart().equals(VIDEO_TAG_CAPTION)) {
                    video.setVideoCaption(readCharacters(reader));
                } else if (se.getName().getLocalPart().equals(POST_TAG_HASHTAG)) {
                    final String tag = readCharacters(reader);
                    post.setTags(addTagToString(post.getTags(), tag));
                }
            } else if (event.isEndElement()) {
                final EndElement ee = event.asEndElement();

                if (ee.getName().getLocalPart().equals(POST_TAG_NAME)) {
                    return video;
                }
            }
        }

        logger.error(UNEXPECTED_EOF_LOG, "readVideos");
        throw new XMLStreamException(END_OF_FILE_ERROR);
    }

    /**
     * Helper function to add a tag to a list of tags; the resultant list is comma-separated.
     * Hashtagas are always converted to lowercase.
     *
     * @param original The existing list of tags (which could be empty)
     * @param tag      The tag to be added
     * @return Amended string
     */
    private static String addTagToString(final String original, final String tag) {
        final String originalTag = tag.toLowerCase();

        if (original.length() == 0) {
            return originalTag;
        }

        final StringBuilder builder = new StringBuilder();
        builder.append(original);
        builder.append(TAG_COMMA_SEPARATOR);
        builder.append(originalTag);
        return builder.toString();
    }

}
