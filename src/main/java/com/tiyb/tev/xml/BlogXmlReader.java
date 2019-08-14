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

import com.tiyb.tev.controller.TEVMetadataRestController;
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
 * This class is responsible for parsing the XML file exported from Tumblr for a
 * user's <b>blogs</b>. It leverages the <b>StAX</b> library's event-based
 * processing model to read the file.
 * </p>
 * 
 * <p>
 * The XML export from Tumblr is... not always a well thought out piece of
 * information architecture. There are inconsistencies as to how some things are
 * structured, and so it had to be reverse engineered to figure things out.
 * Especially when it comes to "photo" posts, where there are inconsistent ways
 * in which links to photos are handled. That being said, the <i>general</i>
 * structure is that there is a <code>&lt;post&gt;</code> element for each post,
 * with attributes for all of the data that is common to any kind of a post
 * (URL, "slug," date it was posted in a couple of different formats, etc.).
 * Under that <code>&lt;post&gt;</code> element are child elements, where the
 * child elements to be found are very different depending on the type of post.
 * An "answer" post, for example, will have two child elements (for the question
 * and the answer), whereas a "photo" post will have numerous child elements
 * covering the post's caption, various sizes of the images, etc.
 * </p>
 * 
 * <p>
 * Not all of the attributes or elements in the document are pulled out, only
 * the ones that are useful for the TEV application (or deemed to be potentially
 * useful). Probably about 90% of the information is retrieved, with the rest
 * being discarded.
 * </p>
 * 
 * <p>
 * The general approach taken is that the
 * {@link #parseDocument(InputStream, TEVPostRestController, TEVMetadataRestController)
 * parseDocument()} method sets up some initial variables, the
 * {@link #readPosts(InputStream, TEVPostRestController, boolean) readPosts()}
 * method then goes through the document post-by-post, and as it determines what
 * type each post is, additional methods are called to read the additional,
 * type-specific XML within the post's XML element (answer, link, photo, etc.).
 * </p>
 * 
 * @author tiyb
 * @apiviz.landmark
 * @apiviz.uses javax.xml.stream.XMLEventReader
 */
public class BlogXmlReader extends TEVXmlReader {

	/**
	 * <p>
	 * This is the main method of the class, which kicks off the processing of the
	 * document. It doesn't do much work itself, it simply calls the
	 * {@link #readPosts(InputStream, TEVPostRestController, boolean) readPosts()}
	 * method to get into the actual XML document.
	 * </p>
	 * 
	 * <p>
	 * The one piece of logic actually performed by this method is to delete all of
	 * the data in the database (via the REST controller), if the "overwrite post
	 * data" option is set.
	 * </p>
	 * 
	 * @param xmlFile        {@link java.io.InputStream InputStream} containing the
	 *                       XML document to be parsed.
	 * @param postController REST controller for the application, used for storing
	 *                       data
	 * @throws XMLParsingException
	 */
	public static void parseDocument(InputStream xmlFile, TEVPostRestController postController,
			TEVMetadataRestController mdController) throws XMLParsingException {
		boolean isOverwritePosts = mdController.getMetadata().getOverwritePostData();

		if (isOverwritePosts) {
			postController.deleteAllRegulars();
			postController.deleteAllAnswers();
			postController.deleteAllLinks();
			postController.deleteAllPhotos();
			postController.deleteAllVideos();
			postController.deleteAllPosts();
			postController.deleteAllHashtags();
			logger.debug("previous content deleted as part of post XML import");
		}

		readPosts(xmlFile, postController, isOverwritePosts);
	}

	/**
	 * <p>
	 * This is the first method that starts to get into the work of actually reading
	 * the XML file. Luckily, the structure of the <code>&lt;post&gt;</code> element
	 * is such that the generic, post-related information is contained in
	 * attributes, while the data that changes based on type is contained in child
	 * elements. This means that parsing the <code>&lt;post&gt;</code> element works
	 * like this:
	 * </p>
	 * 
	 * <ol>
	 * <li>As the "start element" event is encountered for each post, a new
	 * {@link com.tiyb.tev.datamodel.Post Post} object is created</li>
	 * <li>The attributes are read into that object via the
	 * {@link #readPostAttributes(StartElement, Post) readPostAttributes()} method
	 * to populate its data</li>
	 * <li>The post is inserted into the DB via the REST controller.
	 * <ul>
	 * <li>If the "overwrite posts" option is set in the metadata, the logic first
	 * checks to see if the post already exists, and only inserts it if it
	 * doesn't.</li>
	 * <li>A boolean value is set, based on this logic, so that insertions of
	 * subsequent data for this post doesn't have to figure it out all over
	 * again.</li>
	 * </ul>
	 * </li>
	 * <li>Depending on the value of the <code>type</code> attribute, one of the
	 * additional methods is called to parse the type-specific data</li>
	 * <li>Those child methods populate their own objects, and then use the REST
	 * controller to insert the data into the DB
	 * <ul>
	 * <li>The previously set boolean is checked first, before submitting the
	 * data.</li>
	 * </ul>
	 * </li>
	 * <li>A final update of the post is done. This is necessary because tags for
	 * the post were discovered <i>after</i> the post was originally inserted.</li>
	 * </ol>
	 * 
	 * @param xmlFile            The stream containing the XML file to be parsed
	 * @param postRestController REST controller used for storing the data
	 * @param isOverwritePosts   Indicates whether this is a clean upload, or
	 *                           additive; the REST controller could have been used
	 *                           to determine this, but since the calling method
	 *                           needed to figure it out anyway, it was just as easy
	 *                           to pass it as a parameter.
	 * @throws XMLParsingException
	 */
	private static void readPosts(InputStream xmlFile, TEVPostRestController postRestController,
			boolean isOverwritePosts) throws XMLParsingException {

		try {
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			XMLEventReader reader = inputFactory.createXMLEventReader(xmlFile);

			Post post = null;

			while (reader.hasNext()) {
				XMLEvent event = reader.nextEvent();

				if (event.isStartElement()) {
					StartElement se = event.asStartElement();

					if (se.getName().getLocalPart().equals("post")) {
						post = new Post();
						boolean isSubmitablePost = true;
						readPostAttributes(se, post);
						if (isOverwritePosts && post.getState().equals("published")) {
							postRestController.createPost(post);
						} else if (!post.getState().equals("published")) {
							isSubmitablePost = false;
						} else {
							try {
								Post serverPost = postRestController.getPostById(post.getId());
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
								postRestController.createPost(post);
							}
						}
						switch (post.getType()) {
						case "regular":
							Regular regular = readRegular(reader, post, postRestController);
							if (isSubmitablePost) {
								postRestController.createRegular(post.getId(), regular);
							}
							break;
						case "answer":
							Answer answer = readAnswer(reader, post, postRestController);
							if (isSubmitablePost) {
								postRestController.createAnswer(post.getId(), answer);
							}
							break;
						case "link":
							Link link = readLink(reader, post, postRestController);
							if (isSubmitablePost) {
								postRestController.createLink(post.getId(), link);
							}
							break;
						case "photo":
							List<Photo> photos = readPhotos(reader, post, postRestController);
							if (isSubmitablePost) {
								for (Photo p : photos) {
									postRestController.createPhoto(p);
								}
							}
							break;
						case "video":
							Video video = readVideos(reader, post, postRestController);
							if (isSubmitablePost) {
								postRestController.createVideo(post.getId(), video);
							}
							break;
						}
						if (isSubmitablePost) {
							post = postRestController.updatePost(post.getId(), post);
							if(post.getTags().length() > 0) {
								List<String> individualTags = Arrays.asList(post.getTags().split(","));
								for (String tag : individualTags) {
									tag = tag.trim();
									if(tag.equals("")) {
										logger.error("A hashtag was empty from this list: " + post.getTags());
									}
									postRestController.createHashtag(tag);
								}
							}
						}
					}
				}
			}
		} catch (XMLStreamException e) {
			logger.error("XML parser threw error: ", e);
			throw new XMLParsingException();
		} finally {

		}
	}

	/**
	 * Helper function specifically for reading the attributes from a
	 * <code>&lt;post&gt;</code> element. The logic could easily have been
	 * incorporated into
	 * {@link #readPosts(InputStream, TEVPostRestController, boolean) readPosts()},
	 * but the method would have gotten much longer.
	 * 
	 * @param startElement The {@link javax.xml.stream.events.StartElement
	 *                     StartElement} object being processed
	 * @param post         The {@link com.tiyb.tev.datamodel.Post Post} object to
	 *                     which the data from each element should be added
	 */
	private static void readPostAttributes(StartElement startElement, Post post) {
		Iterator<Attribute> atts = startElement.getAttributes();
		while (atts.hasNext()) {
			Attribute att = atts.next();
			String attName = att.getName().getLocalPart();
			switch (attName) {
			case "id":
				post.setId(Long.parseLong(att.getValue()));
				break;
			case "url":
				post.setUrl(att.getValue());
				break;
			case "url-with-slug":
				post.setUrlWithSlug(att.getValue());
				break;
			case "type":
				post.setType(att.getValue());
				break;
			case "date-gmt":
				post.setDateGmt(att.getValue());
				break;
			case "date":
				post.setDate(att.getValue());
				break;
			case "unix-timestamp":
				post.setUnixtimestamp(Long.parseLong(att.getValue()));
				break;
			case "format":
				// attribute not used
				break;
			case "reblog-key":
				post.setReblogKey(att.getValue());
				break;
			case "slug":
				post.setSlug(att.getValue());
				break;
			case "state":
				post.setState(att.getValue());
				break;
			case "is_reblog":
				post.setIsReblog(Boolean.parseBoolean(att.getValue()));
				break;
			case "tumblelog":
				post.setTumblelog(att.getValue());
				break;
			}
		}

	}

	/**
	 * This method is responsible for reading the sub-content under a post for
	 * content related to a "regular" post. The content is very simple, containing
	 * only "regular-title" and "regular-body" elements.
	 * 
	 * @param reader             The {@link javax.xml.stream.XMLEventReader
	 *                           XMLEventReader} object for the XML document being
	 *                           parsed.
	 * @param post               The current post, for use as the primary key of the
	 *                           Regular object and for setting tags
	 * @param postRestController The REST controller for working with posts, used
	 *                           for inserting Hashtags into the DB
	 * @return A {@link com.tiyb.tev.datamodel.Regular Regular} object with the data
	 *         read
	 * @throws XMLStreamException
	 */
	private static Regular readRegular(XMLEventReader reader, Post post, TEVPostRestController postRestController)
			throws XMLStreamException {
		Regular regular = new Regular();
		regular.setPostId(post.getId());

		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();

			if (event.isStartElement()) {
				StartElement se = event.asStartElement();

				if (se.getName().getLocalPart().equals("regular-title")) {
					regular.setTitle(readCharacters(reader));
				} else if (se.getName().getLocalPart().equals("regular-body")) {
					regular.setBody(readCharacters(reader));
				} else if (se.getName().getLocalPart().equals("tag")) {
					String hashtag = readCharacters(reader);
					post.setTags(addTagToString(post.getTags(), hashtag));
				}
			} else if (event.isEndElement()) {
				EndElement ee = event.asEndElement();

				if (ee.getName().getLocalPart().equals("post")) {
					return regular;
				}
			}
		}

		logger.error("Unexpected end of file reached in readRegular");
		throw new XMLStreamException(END_OF_FILE_ERROR);
	}

	/**
	 * This method is responsible for reading the sub-content under a post for
	 * content related to an "answer" post. The content is very simple, containing
	 * only "question" and "answer" elements.
	 * 
	 * @param reader             The event reader parsing the current document
	 * @param post               The current post, for use as the primary key of the
	 *                           Answer object and for setting tags
	 * @param postRestController The REST controller for working with posts, for
	 *                           inserting hashtags
	 * @return The "answer" data
	 * @throws XMLStreamException
	 */
	private static Answer readAnswer(XMLEventReader reader, Post post, TEVPostRestController postRestController)
			throws XMLStreamException {
		Answer answer = new Answer();
		answer.setPostId(post.getId());

		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();

			if (event.isStartElement()) {
				StartElement se = event.asStartElement();

				if (se.getName().getLocalPart().equals("question")) {
					answer.setQuestion(readCharacters(reader));
				} else if (se.getName().getLocalPart().equals("answer")) {
					answer.setAnswer(readCharacters(reader));
				} else if (se.getName().getLocalPart().equals("tag")) {
					String tag = readCharacters(reader);
					post.setTags(addTagToString(post.getTags(), tag));
				}
			} else if (event.isEndElement()) {
				EndElement ee = event.asEndElement();

				if (ee.getName().getLocalPart().equals("post")) {
					return answer;
				}
			}
		}

		logger.error("Unexpected end of file reached in readAnswer");
		throw new XMLStreamException(END_OF_FILE_ERROR);
	}

	/**
	 * This method is responsible for reading the sub-content under a post for
	 * content related to a "link" post. The content contains only three elements:
	 * "link-description", "link-text", and "link-url".
	 * 
	 * @param reader             The event parser
	 * @param post               The currently processed post
	 * @param postRestController The REST controller for working with posts, for
	 *                           inserting hashtags
	 * @return The link data
	 * @throws XMLStreamException
	 */
	private static Link readLink(XMLEventReader reader, Post post, TEVPostRestController postRestController)
			throws XMLStreamException {
		Link link = new Link();
		link.setPostId(post.getId());

		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();

			if (event.isStartElement()) {
				StartElement se = event.asStartElement();

				if (se.getName().getLocalPart().equals("link-description")) {
					link.setDescription(readCharacters(reader));
				} else if (se.getName().getLocalPart().equals("link-text")) {
					link.setText(readCharacters(reader));
				} else if (se.getName().getLocalPart().equals("link-url")) {
					link.setUrl(readCharacters(reader));
				} else if (se.getName().getLocalPart().equals("tag")) {
					String tag = readCharacters(reader);
					post.setTags(addTagToString(post.getTags(), tag));
				}
			} else if (event.isEndElement()) {
				EndElement ee = event.asEndElement();

				if (ee.getName().getLocalPart().equals("post")) {
					return link;
				}
			}
		}

		logger.error("Unexpected end of file reached in readLink");
		throw new XMLStreamException(END_OF_FILE_ERROR);
	}

	/**
	 * <p>
	 * This method is responsible for reading the sub-content under a post for
	 * content related to a "photo" post. This is the most complex of the post
	 * types, as far as the XML is concerned, and is also the least consistent.
	 * There is one child element that always appears ("photo-caption"), and then
	 * there is content for each photo within the post. For each photo, there are 6
	 * "photo-url" elements, for URLs to differently sized versions of the same
	 * image (1280, 500, 400, ...).
	 * </p>
	 * 
	 * <p>
	 * The tricky part, for "photo" posts, is that there could be <i>one</i> photo,
	 * or there could be <i>multiple</i> (each with 6 "photo-url" elements), and the
	 * structure is different for both cases:
	 * </p>
	 * 
	 * <ul>
	 * <li>If there is only one photo, it is represented by simply appending the 6
	 * "photo-url" elements right to the main "post" element</li>
	 * <li>If there are multiple photos, Tumblr treats it like a <b>photoset</b>, in
	 * which case a "photoset" element is added to the "post", under which is one
	 * "photo" element for each photo, under which are the 6 "photo-url" elements
	 * (the same as for a single photo).</li>
	 * <li>Even in the case where there is a "photoset", the <i>first</i> image in
	 * that photoset is duplicated, as 6 "photo-url" elements under the "post"
	 * element. These "photo-url" elements come before the "photoset", so they're
	 * read before the XML parser even realizes that there is a "photoset"
	 * coming.</li>
	 * </ul>
	 * 
	 * <p>
	 * In order to deal with this complexity, processing of a "photo" post works as
	 * follows:
	 * </p>
	 * 
	 * <ol>
	 * <li>The "photo-caption" element is read in. (It is added to every
	 * <code>Photo</code> object created, which is not optimal, but... still.)</li>
	 * <li>Any "photo-url" elements are read in, and the information is saved for
	 * later use</li>
	 * <li><i>If</i> there is a "photoset" element, a sub-method is called for
	 * parsing the content. That method will create a
	 * {@link com.tiyb.tev.datamodel.Photo Photo} object for each photo it
	 * encounters.</li>
	 * <li>When the end of the "post" element is reached, the list of
	 * {@link com.tiyb.tev.datamodel.Photo Photo} objects is checked, to see if any
	 * were created (as part of a photoset) or not; if not, a single
	 * {@link com.tiyb.tev.datamodel.Photo Photo} object is created, with the URL
	 * and caption data already collected, and put in the collection. If the
	 * collection is already populated, it is simply returned as-is, since the
	 * "photo-url" elements at the root of the <code>&lt;post&gt;</code> element are
	 * duplicated in the "photoset", and have therefore already been added.</li>
	 * </ol>
	 * 
	 * @param reader             The event reader being used to parse the XML
	 * @param post               The currently processed post
	 * @param postRestController The REST controller for working with posts, for
	 *                           inserting hashtags
	 * @return A list of {@link com.tiyb.tev.datamodel.Photo Photo} objects
	 * @throws XMLStreamException
	 */
	private static List<Photo> readPhotos(XMLEventReader reader, Post post, TEVPostRestController postRestController)
			throws XMLStreamException {
		List<Photo> photos = new ArrayList<Photo>();
		String caption = "";
		String url1280 = "";
		String url500 = "";
		String url400 = "";
		String url250 = "";
		String url100 = "";
		String url75 = "";

		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();

			if (event.isStartElement()) {
				StartElement se = event.asStartElement();

				if (se.getName().getLocalPart().equals("photo-caption")) {
					caption = readCharacters(reader);
				} else if (se.getName().getLocalPart().equalsIgnoreCase("photo-url")) {
					String width = se.getAttributeByName(new QName("max-width")).getValue();
					switch (width) {
					case "1280":
						url1280 = readCharacters(reader);
						break;
					case "500":
						url500 = readCharacters(reader);
						break;
					case "400":
						url400 = readCharacters(reader);
						break;
					case "250":
						url250 = readCharacters(reader);
						break;
					case "100":
						url100 = readCharacters(reader);
						break;
					case "75":
						url75 = readCharacters(reader);
						break;
					}
				} else if (se.getName().getLocalPart().equals("photoset")) {
					readPhotoStream(reader, post.getId(), caption, photos);
				} else if (se.getName().getLocalPart().equals("tag")) {
					String tag = readCharacters(reader);
					post.setTags(addTagToString(post.getTags(), tag));
				}
			} else if (event.isEndElement()) {
				EndElement ee = event.asEndElement();

				if (ee.getName().getLocalPart().equals("post")) {
					if (photos.size() < 1) {
						Photo photo = new Photo();
						photo.setPostId(post.getId());
						photo.setCaption(caption);
						photo.setUrl1280(url1280);
						photo.setUrl100(url100);
						photo.setUrl250(url250);
						photo.setUrl400(url400);
						photo.setUrl500(url500);
						photo.setUrl75(url75);

						photos.add(photo);
					}

					return photos;
				}
			}
		}

		logger.error("Unexpected end of file reached in readPhotos");
		throw new XMLStreamException(END_OF_FILE_ERROR);
	}

	/**
	 * This method is responsible for reading the sub-components of a "photo" post
	 * specific to the "photoset". Within a photoset, there are multiple "photo"
	 * elements, each of which contains 6 "photo-url" elements, for the different
	 * sizes of that photo. Because there is some duplication in the model, the
	 * caption is passed from the parent method into this one, and added to each
	 * photo.
	 * 
	 * @param reader       The event reader used for parsing
	 * @param postID       The ID of the currently processed post
	 * @param photoCaption The caption to be used on each photo
	 * @param photos       The list of photos that already exists, to which new
	 *                     photos will be added.
	 * @throws XMLStreamException
	 */
	private static List<Photo> readPhotoStream(XMLEventReader reader, Long postID, String photoCaption,
			List<Photo> photos) throws XMLStreamException {
		Photo currentPhoto = new Photo();

		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();

			if (event.isStartElement()) {
				StartElement se = event.asStartElement();

				if (se.getName().getLocalPart().equals("photo")) {
					currentPhoto = new Photo();
					currentPhoto.setPostId(postID);
					currentPhoto.setCaption(photoCaption);
					String offset = se.getAttributeByName(new QName("offset")).getValue();
					currentPhoto.setOffset(offset);
				} else if (se.getName().getLocalPart().equals("photo-url")) {
					String width = se.getAttributeByName(new QName("max-width")).getValue();
					switch (width) {
					case "1280":
						currentPhoto.setUrl1280(readCharacters(reader));
						break;
					case "500":
						currentPhoto.setUrl500(readCharacters(reader));
						break;
					case "400":
						currentPhoto.setUrl400(readCharacters(reader));
						break;
					case "250":
						currentPhoto.setUrl250(readCharacters(reader));
						break;
					case "100":
						currentPhoto.setUrl100(readCharacters(reader));
						break;
					case "75":
						currentPhoto.setUrl75(readCharacters(reader));
						break;
					}
				}
			} else if (event.isEndElement()) {
				EndElement ee = event.asEndElement();

				if (ee.getName().getLocalPart().equals("photo")) {
					photos.add(currentPhoto);
				} else if (ee.getName().getLocalPart().equals("photoset")) {
					return photos;
				}
			}
		}

		logger.error("Unexpected end of file reached in readPhotoStream");
		throw new XMLStreamException(END_OF_FILE_ERROR);
	}

	/**
	 * This method is responsible for reading the sub-content under a post for
	 * content related to a "video" post. The content contains a number of elements,
	 * most of which are simply read into a property of the <code>Video</code>
	 * object
	 * 
	 * <ul>
	 * <li>content-type</li>
	 * <li>extension (the filename extension)</li>
	 * <li>width</li>
	 * <li>height</li>
	 * <li>duration</li>
	 * <li>revision</li>
	 * <li>video-caption (the body of the post)</li>
	 * <li>multiple "video-player" elements (for different sizes of the video) which
	 * are ignored</li>
	 * </ul>
	 * 
	 * @param reader             The event parser
	 * @param post               The currently processed post
	 * @param postRestController The REST controller for working with posts, for
	 *                           inserting hashtags
	 * @return The video data
	 * @throws XMLStreamException
	 */
	private static Video readVideos(XMLEventReader reader, Post post, TEVPostRestController postRestController)
			throws XMLStreamException {
		Video video = new Video();
		video.setPostId(post.getId());

		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();

			if (event.isStartElement()) {
				StartElement se = event.asStartElement();

				if (se.getName().getLocalPart().equals("content-type")) {
					video.setContentType(readCharacters(reader));
				} else if (se.getName().getLocalPart().equals("extension")) {
					video.setExtension(readCharacters(reader));
				} else if (se.getName().getLocalPart().equals("width")) {
					video.setWidth(Integer.parseInt(readCharacters(reader)));
				} else if (se.getName().getLocalPart().equals("height")) {
					video.setHeight(Integer.parseInt(readCharacters(reader)));
				} else if (se.getName().getLocalPart().equals("duration")) {
					video.setDuration(Integer.parseInt(readCharacters(reader)));
				} else if (se.getName().getLocalPart().equals("revision")) {
					video.setRevision(readCharacters(reader));
				} else if (se.getName().getLocalPart().equals("video-caption")) {
					video.setVideoCaption(readCharacters(reader));
				} else if (se.getName().getLocalPart().equals("tag")) {
					String tag = readCharacters(reader);
					post.setTags(addTagToString(post.getTags(), tag));
				}
			} else if (event.isEndElement()) {
				EndElement ee = event.asEndElement();

				if (ee.getName().getLocalPart().equals("post")) {
					return video;
				}
			}
		}

		logger.error("Unexpected end of file reached in readVideos");
		throw new XMLStreamException(END_OF_FILE_ERROR);
	}

	/**
	 * Helper function to add a tag to a list of tags; the resultant list is
	 * comma-separated. Hashtagas are always converted to lowercase.
	 * 
	 * @param original The existing list of tags (which could be empty)
	 * @param tag      The tag to be added
	 * @return Amended string
	 */
	private static String addTagToString(String original, String tag) {
		tag = tag.toLowerCase();

		if (original.length() == 0) {
			return tag;
		}

		StringBuilder builder = new StringBuilder();
		builder.append(original);
		builder.append(", ");
		builder.append(tag);
		return builder.toString();
	}

}
