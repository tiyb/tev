package com.tiyb.tev.xml;

import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiyb.tev.controller.TEVPostRestController;
import com.tiyb.tev.datamodel.Answer;
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.datamodel.Video;
import com.tiyb.tev.xml.helper.PrettyPrintHandler;

/**
 * <p>
 * Helper class used for generating an XML file, in the same format as the Tumblr export, so that
 * posts can be imported on other sides (e.g. WordPress). Because the user can select which posts
 * they want to export, the export file will contain only the posts that the user <i>wants</i> to
 * export (and therefore import somewhere else), rather than the entire blog.
 * </p>
 *
 * <p>
 * The Java StAX library is used for generating the XML content.
 * </p>
 *
 * @author tiyb
 *
 */
public final class BlogXmlWriter {

    /**
     * XML tag name for the specified item
     */
    private static final String VIDEO_TAG_SOURCE = "video-source";

    private static Logger logger = LoggerFactory.getLogger(BlogXmlWriter.class);

    /**
     * Ensures that the class is never instantiated as an object
     */
    private BlogXmlWriter() {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * Takes in a list of Post IDs, and returns an XML document (String format) containing the data
     * for those posts. A Post REST controller is used to get the data from the database; all the
     * method needs is the list of IDs.
     * </p>
     *
     * <p>
     * This method doesn't do much, other than retrieve the Post data from the REST controller for
     * each post. The {@link #getDocument(List, TEVPostRestController, String) getDocument()} method
     * (and subsequent, type-specific methods) do most of the work.
     * </p>
     *
     * @param postIDs        List of Post IDs to include in the export, regardless of type (photo,
     *                       video, answer, link, regular)
     * @param postController The REST controller for working with Posts
     * @param blogName       Blog for which posts should be retrieved
     * @return String containing the XML document
     */
    public static String getStagedPostXMLForBlog(final List<Long> postIDs, final TEVPostRestController postController,
            final String blogName) {
        final List<Post> posts = new ArrayList<Post>();

        for (Long id : postIDs) {
            final Post post = postController.getPostForBlogById(blogName, id);
            posts.add(post);
        }

        return getDocument(posts, postController, blogName);
    }

    /**
     * <p>
     * This method starts the work of creating the outer shell of the document. The
     * {@link #addPost(Post, XMLStreamWriter, TEVPostRestController, String) addPost()} method
     * starts adding in the detailed data, post-by-post. The
     * {@link com.tiyb.tev.xml.helper.PrettyPrintHandler PrettyPrintHandler} helper class is used to
     * create a more readable version of the XML output.
     * </p>
     *
     * <p>
     * This method also handles any exceptions that might have been raised by sub-methods; when an
     * exception is encountered, it is logged, and NULL is returned for the response.
     * </p>
     *
     * @param posts          The list of posts to be included in the export.
     * @param postController The REST controller, to be used for getting additional data
     * @param blogName       Name of the blog for which the document is being returned
     * @return String containing the XML document
     */
    private static String getDocument(final List<Post> posts, final TEVPostRestController postController,
            final String blogName) {
        String xmlString = StringUtils.EMPTY;

        try {
            final StringWriter stringWriter = new StringWriter();

            final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(stringWriter);
            final PrettyPrintHandler handler = new PrettyPrintHandler(writer);
            final XMLStreamWriter prettyPrintWriter =
                    (XMLStreamWriter) Proxy.newProxyInstance(XMLStreamWriter.class.getClassLoader(),
                            new Class[] { XMLStreamWriter.class }, handler);
            writer = prettyPrintWriter;

            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeStartElement("tumblr");
            writer.writeAttribute("version", "1.0");

            writer.writeStartElement("posts");

            for (Post post : posts) {
                addPost(post, writer, postController, blogName);
            }

            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndDocument();

            xmlString = stringWriter.toString();
        } catch (XMLStreamException e) {
            logger.error("Error creating XML document", e);
            return null;
        }

        return xmlString;
    }

    /**
     * Creates the wrapper XML for each post -- including attributes -- and then calls sub-methods
     * for each individual type of post (regular, photo, answer, etc.).
     *
     * @param post           The post to be written to XML
     * @param writer         The StAX XML writer to which the data should be written
     * @param postController The REST controller for getting additional post information from the DB
     * @param blogName       Blog for which the post is being added
     * @throws XMLStreamException if any errors are encountered by the StAX parser.
     */
    private static void addPost(final Post post, final XMLStreamWriter writer,
            final TEVPostRestController postController, final String blogName) throws XMLStreamException {
        writer.writeStartElement(BlogXmlReader.POST_TAG_NAME);
        addPostAttributes(post, writer);

        switch (post.getType()) {
        case Post.POST_TYPE_ANSWER:
            addAnswerBody(post, writer, postController);
            break;
        case Post.POST_TYPE_LINK:
            addLinkBody(post, writer, postController);
            break;
        case Post.POST_TYPE_PHOTO:
            addPhotoBody(post, writer, postController, blogName);
            break;
        case Post.POST_TYPE_REGULAR:
            addRegularBody(post, writer, postController);
            break;
        case Post.POST_TYPE_VIDEO:
            addVideoBody(post, writer, postController);
            break;
        default:
            logger.error("Invalid post type");
            throw new XMLStreamException();
        }

        final String postTags = post.getTags();
        final String[] enumeratedTags = postTags.split(",");

        for (String tag : enumeratedTags) {
            tag = tag.trim();
            if (tag.length() > 0) {
                writer.writeStartElement(BlogXmlReader.POST_TAG_HASHTAG);
                writer.writeCharacters(tag);
                writer.writeEndElement();
            }
        }

        writer.writeEndElement();
    }

    /**
     * Adds a Photo post to the XML. The {@link #addPhotoWithSizes(Photo, XMLStreamWriter)
     * addPhotoWithSizes()} helper method is used to insert the XML tags for different sizes of
     * photo.
     *
     * @param post           The post being added, with one or more photos
     * @param writer         The StAX XML writer
     * @param postController The REST controller for retrieving additional data
     * @param blogName       Blog for which the photo si being added
     * @throws XMLStreamException if any errors are encountered by the underlying StAX classes
     */
    private static void addPhotoBody(final Post post, final XMLStreamWriter writer,
            final TEVPostRestController postController, final String blogName) throws XMLStreamException {
        final List<Photo> photos = postController.getPhotoController().getPhotoForBlogById(blogName, post.getId());

        writer.writeStartElement(BlogXmlReader.PHOTO_TAG_CAPTION);
        writer.writeCharacters(photos.get(0).getCaption());
        writer.writeEndElement();

        if (photos.get(0).getPhotoLinkUrl() != null) {
            writer.writeStartElement(BlogXmlReader.PHOTO_TAG_LINKURL);
            writer.writeCharacters(photos.get(0).getPhotoLinkUrl());
            writer.writeEndElement();
        }

        addPhotoWithSizes(photos.get(0), writer);

        if (photos.size() > 1) {
            writer.writeStartElement(BlogXmlReader.PHOTO_TAG_PHOTOSET);
            for (int i = 0; i < photos.size(); i++) {
                writer.writeStartElement(BlogXmlReader.PHOTO_TAG_PHOTO);
                writer.writeAttribute(BlogXmlReader.PHOTO_ATTRIBUTE_OFFSET, photos.get(i).getOffset());
                writer.writeAttribute(BlogXmlReader.PHOTO_ATTRIBUTE_CAPTION, StringUtils.EMPTY);
                writer.writeAttribute(BlogXmlReader.PHOTO_ATTRIBUTE_WIDTH, String.valueOf(photos.get(i).getWidth()));
                writer.writeAttribute(BlogXmlReader.PHOTO_ATTRIBUTE_HEIGHT, String.valueOf(photos.get(i).getHeight()));
                addPhotoWithSizes(photos.get(i), writer);
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
    }

    /**
     * Helper function for adding a <code>photo-url</code> element to the XML, since these are added
     * multiple times per photo.
     *
     * @param photo  The specific photo being written
     * @param writer The StAX XML writer
     * @throws XMLStreamException if any errors are encountered by the underlying StAX objects
     */
    private static void addPhotoWithSizes(final Photo photo, final XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(BlogXmlReader.PHOTO_TAG_URL);
        writer.writeAttribute(BlogXmlReader.PHOTO_ATTRIBUTE_MAXWIDTH, BlogXmlReader.PHOTO_SIZE_1280);
        writer.writeCharacters(photo.getUrl1280());
        writer.writeEndElement();

        writer.writeStartElement(BlogXmlReader.PHOTO_TAG_URL);
        writer.writeAttribute(BlogXmlReader.PHOTO_ATTRIBUTE_MAXWIDTH, BlogXmlReader.PHOTO_SIZE_500);
        writer.writeCharacters(photo.getUrl500());
        writer.writeEndElement();

        writer.writeStartElement(BlogXmlReader.PHOTO_TAG_URL);
        writer.writeAttribute(BlogXmlReader.PHOTO_ATTRIBUTE_MAXWIDTH, BlogXmlReader.PHOTO_SIZE_400);
        writer.writeCharacters(photo.getUrl400());
        writer.writeEndElement();

        writer.writeStartElement(BlogXmlReader.PHOTO_TAG_URL);
        writer.writeAttribute(BlogXmlReader.PHOTO_ATTRIBUTE_MAXWIDTH, BlogXmlReader.PHOTO_SIZE_250);
        writer.writeCharacters(photo.getUrl250());
        writer.writeEndElement();

        writer.writeStartElement(BlogXmlReader.PHOTO_TAG_URL);
        writer.writeAttribute(BlogXmlReader.PHOTO_ATTRIBUTE_MAXWIDTH, BlogXmlReader.PHOTO_SIZE_100);
        writer.writeCharacters(photo.getUrl100());
        writer.writeEndElement();

        writer.writeStartElement(BlogXmlReader.PHOTO_TAG_URL);
        writer.writeAttribute(BlogXmlReader.PHOTO_ATTRIBUTE_MAXWIDTH, BlogXmlReader.PHOTO_SIZE_75);
        writer.writeCharacters(photo.getUrl75());
        writer.writeEndElement();

    }

    /**
     * Adds the XML for a video to the export. <b>This method isn't fully functioning, as video
     * posts require some XML elements which were not included in the original import into the TEV
     * database;</b> for this reason, the UI is currently blocking users from adding video posts to
     * the staging area.
     *
     * @param post           The post to be written to the XML
     * @param writer         The StAX XML writer
     * @param postController The REST controller for getting additional data
     * @throws XMLStreamException if any errors are encountered by the underlying StAX objects
     */
    private static void addVideoBody(final Post post, final XMLStreamWriter writer,
            final TEVPostRestController postController) throws XMLStreamException {
        final Video video = postController.getVideoController().getVideoForBlogById(post.getTumblelog(), post.getId());

        writer.writeStartElement(VIDEO_TAG_SOURCE);

        writer.writeStartElement(BlogXmlReader.VIDEO_TAG_CONTENTTYPE);
        writer.writeCharacters(video.getContentType());
        writer.writeEndElement();

        writer.writeStartElement(BlogXmlReader.VIDEO_TAG_EXTENSION);
        writer.writeCharacters(video.getExtension());
        writer.writeEndElement();

        writer.writeStartElement(BlogXmlReader.VIDEO_TAG_WIDTH);
        writer.writeCharacters(String.valueOf(video.getWidth()));
        writer.writeEndElement();

        writer.writeStartElement(BlogXmlReader.VIDEO_TAG_HEIGHT);
        writer.writeCharacters(String.valueOf(video.getHeight()));
        writer.writeEndElement();

        writer.writeStartElement(BlogXmlReader.VIDEO_TAG_DURATION);
        writer.writeCharacters(String.valueOf(video.getDuration()));
        writer.writeEndElement();

        writer.writeStartElement(BlogXmlReader.VIDEO_TAG_REVISION);
        writer.writeCharacters(video.getRevision());
        writer.writeEndElement();

        writer.writeEndElement();

        writer.writeStartElement(BlogXmlReader.VIDEO_TAG_CAPTION);
        writer.writeCharacters(video.getVideoCaption());
        writer.writeEndElement();
    }

    /**
     * Writes the XML for an answer post to the XML response.
     *
     * @param post           The post being written
     * @param writer         The StAX XML writer
     * @param postController The REST controller for getting additional data
     * @throws XMLStreamException if any errors are encountered by the underlying StAX objects
     */
    private static void addAnswerBody(final Post post, final XMLStreamWriter writer,
            final TEVPostRestController postController) throws XMLStreamException {
        final Answer answer =
                postController.getAnswerController().getAnswerForBlogById(post.getTumblelog(), post.getId());

        writer.writeStartElement(BlogXmlReader.ANSWER_QUESTION_TAG);
        writer.writeCharacters(answer.getQuestion());
        writer.writeEndElement();

        writer.writeStartElement(BlogXmlReader.ANSWER_ANSWER_TAG);
        writer.writeCharacters(answer.getAnswer());
        writer.writeEndElement();

    }

    /**
     * Writes the XML for a link post to the XML response
     *
     * @param post           The Post being written
     * @param writer         The StAX XML writer
     * @param postController The REST controller for getting additional data
     * @throws XMLStreamException if any errors are encountered by the underlying StAX objects
     */
    private static void addLinkBody(final Post post, final XMLStreamWriter writer,
            final TEVPostRestController postController) throws XMLStreamException {
        final Link link = postController.getLinkController().getLinkForBlogById(post.getTumblelog(), post.getId());

        writer.writeStartElement(BlogXmlReader.LINK_TAG_TEXT);
        writer.writeCharacters(link.getText());
        writer.writeEndElement();

        writer.writeStartElement(BlogXmlReader.LINK_TAG_URL);
        writer.writeCharacters(link.getUrl());
        writer.writeEndElement();

        writer.writeStartElement(BlogXmlReader.LINK_TAG_DESCRIPTION);
        writer.writeCharacters(link.getDescription());
        writer.writeEndElement();

    }

    /**
     * Writes the XML for a regular post
     *
     * @param post           The post being written
     * @param writer         The StAX XML writer
     * @param postController The REST controller for getting additional data about the post
     * @throws XMLStreamException if any errors are encountered by the underlying StAX objects
     */
    private static void addRegularBody(final Post post, final XMLStreamWriter writer,
            final TEVPostRestController postController) throws XMLStreamException {
        final Regular regular =
                postController.getRegController().getRegularForBlogById(post.getTumblelog(), post.getId());

        if (regular.getTitle() != null && regular.getTitle().length() > 0) {
            writer.writeStartElement(BlogXmlReader.REGULAR_TAG_TITLE);
            writer.writeCharacters(regular.getTitle());
            writer.writeEndElement();
        }

        writer.writeStartElement(BlogXmlReader.REGULAR_TAG_BODY);
        writer.writeCharacters(regular.getBody());
        writer.writeEndElement();

    }

    /**
     * Helper function for adding the attributes for the post to the <code>post</code> element.
     *
     * @param post   The post being written
     * @param writer The StAX XML writer
     * @throws XMLStreamException if any errors are encountered by the underlying StAX objects
     */
    private static void addPostAttributes(final Post post, final XMLStreamWriter writer) throws XMLStreamException {
        writer.writeAttribute(BlogXmlReader.POST_ATTRIBUTE_ID, String.valueOf(post.getId()));
        writer.writeAttribute(BlogXmlReader.POST_ATTRIBUTE_URL, post.getUrl());
        writer.writeAttribute(BlogXmlReader.POST_ATTRIBUTE_URLWITHSLUG, post.getUrlWithSlug());
        writer.writeAttribute(BlogXmlReader.POST_ATTRIBUTE_TYPE, post.getType());
        writer.writeAttribute(BlogXmlReader.POST_ATTRIBUTE_DATEGMT, post.getDateGmt());
        writer.writeAttribute(BlogXmlReader.POST_ATTRIBUTE_DATE, post.getDate());
        writer.writeAttribute(BlogXmlReader.POST_ATTRIBUTE_UNIXTIMESTAMP, String.valueOf(post.getUnixtimestamp()));
        writer.writeAttribute(BlogXmlReader.POST_ATTRIBUTE_FORMAT, "html");
        writer.writeAttribute(BlogXmlReader.POST_ATTRIBUTE_REBLOGKEY, post.getReblogKey());
        writer.writeAttribute(BlogXmlReader.POST_ATTRIBUTE_SLUG, post.getSlug());
        writer.writeAttribute(BlogXmlReader.POST_ATTRIBUTE_STATE, post.getState());
        writer.writeAttribute(BlogXmlReader.POST_ATTRIBUTE_ISREBLOG, String.valueOf(post.getIsReblog()));
        writer.writeAttribute(BlogXmlReader.POST_ATTRIBUTE_TUMBLELOG, String.valueOf(post.getTumblelog()));

        if (post.getType().equals(Post.POST_TYPE_PHOTO)) {
            writer.writeAttribute(BlogXmlReader.PHOTO_ATTRIBUTE_WIDTH, String.valueOf(post.getWidth()));
            writer.writeAttribute(BlogXmlReader.PHOTO_ATTRIBUTE_HEIGHT, String.valueOf(post.getHeight()));
        }
    }
}
