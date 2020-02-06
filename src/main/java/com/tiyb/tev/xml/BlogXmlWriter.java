package com.tiyb.tev.xml;

import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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
 * Helper class used for generating an XML file, in the same format as the
 * Tumblr export, so that posts can be imported on other sides (e.g. WordPress).
 * Because the user can select which posts they want to export, the export file
 * will contain only the posts that the user <i>wants</i> to export (and
 * therefore import somewhere else), rather than the entire blog.
 * </p>
 * 
 * <p>
 * The Java StAX library is used for generating the XML content.
 * </p>
 * 
 * @author tiyb
 *
 */
public class BlogXmlWriter {

	private static Logger logger = LoggerFactory.getLogger(BlogXmlWriter.class);

	/**
	 * <p>
	 * Takes in a list of Post IDs, and returns an XML document (String format)
	 * containing the data for those posts. A Post REST controller is used to get
	 * the data from the database; all the method needs is the list of IDs.
	 * </p>
	 * 
	 * <p>
	 * This method doesn't do much, other than retrieve the Post data from the REST
	 * controller for each post. The
	 * {@link #getDocument(List, TEVPostRestController, String) getDocument()}
	 * method (and subsequent, type-specific methods) do most of the work.
	 * </p>
	 * 
	 * @param postIDs        List of Post IDs to include in the export, regardless
	 *                       of type (photo, video, answer, link, regular)
	 * @param postController The REST controller for working with Posts
	 * @param blogName       Blog for which posts should be retrieved
	 * @return String containing the XML document
	 */
	public static String getStagedPostXMLForBlog(List<Long> postIDs, TEVPostRestController postController,
			String blogName) {
		List<Post> posts = new ArrayList<Post>();

		for (Long id : postIDs) {
			Post post = postController.getPostForBlogById(blogName, id);
			posts.add(post);
		}

		return getDocument(posts, postController, blogName);
	}

	/**
	 * <p>
	 * This method starts the work of creating the outer shell of the document. The
	 * {@link #addPost(Post, XMLStreamWriter, TEVPostRestController, String)
	 * addPost()} method starts adding in the detailed data, post-by-post. The
	 * {@link com.tiyb.tev.xml.helper.PrettyPrintHandler PrettyPrintHandler} helper
	 * class is used to create a more readable version of the XML output.
	 * </p>
	 * 
	 * <p>
	 * This method also handles any exceptions that might have been raised by
	 * sub-methods; when an exception is encountered, it is logged, and NULL is
	 * returned for the response.
	 * </p>
	 * 
	 * @param posts          The list of posts to be included in the export.
	 * @param postController The REST controller, to be used for getting additional
	 *                       data
	 * @param blogName       Name of the blog for which the document is being
	 *                       returned
	 * @return String containing the XML document
	 */
	private static String getDocument(List<Post> posts, TEVPostRestController postController, String blogName) {
		String xmlString;
		try {
			StringWriter stringWriter = new StringWriter();

			XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
			XMLStreamWriter writer = outputFactory.createXMLStreamWriter(stringWriter);
			PrettyPrintHandler handler = new PrettyPrintHandler(writer);
			XMLStreamWriter prettyPrintWriter = (XMLStreamWriter) Proxy.newProxyInstance(
					XMLStreamWriter.class.getClassLoader(), new Class[] { XMLStreamWriter.class }, handler);
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
	 * Creates the wrapper XML for each post -- including attributes -- and then
	 * calls sub-methods for each individual type of post (regular, photo, answer,
	 * etc.).
	 * 
	 * @param post           The post to be written to XML
	 * @param writer         The StAX XML writer to which the data should be written
	 * @param postController The REST controller for getting additional post
	 *                       information from the DB
	 * @param blogName       Blog for which the post is being added
	 * @throws XMLStreamException if any errors are encountered by the StAX parser.
	 */
	private static void addPost(Post post, XMLStreamWriter writer, TEVPostRestController postController,
			String blogName) throws XMLStreamException {
		writer.writeStartElement("post");
		addPostAttributes(post, writer);
		switch (post.getType()) {
		case "answer":
			addAnswerBody(post, writer, postController);
			break;
		case "link":
			addLinkBody(post, writer, postController);
			break;
		case "photo":
			addPhotoBody(post, writer, postController, blogName);
			break;
		case "regular":
			addRegularBody(post, writer, postController);
			break;
		case "video":
			addVideoBody(post, writer, postController);
			break;
		default:
			logger.error("Invalid post type");
			throw new XMLStreamException();
		}

		String postTags = post.getTags();
		String enumeratedTags[] = postTags.split(",");

		for (String tag : enumeratedTags) {
			tag = tag.trim();
			if (tag.length() > 0) {
				writer.writeStartElement("tag");
				writer.writeCharacters(tag);
				writer.writeEndElement();
			}
		}

		writer.writeEndElement();
	}

	/**
	 * Adds a Photo post to the XML. The
	 * {@link #addPhotoWithSizes(Photo, XMLStreamWriter) addPhotoWithSizes()} helper
	 * method is used to insert the XML tags for different sizes of photo.
	 * 
	 * @param post           The post being added, with one or more photos
	 * @param writer         The StAX XML writer
	 * @param postController The REST controller for retrieving additional data
	 * @param blogName       Blog for which the photo si being added
	 * @throws XMLStreamException if any errors are encountered by the underlying
	 *                            StAX classes
	 */
	private static void addPhotoBody(Post post, XMLStreamWriter writer, TEVPostRestController postController,
			String blogName) throws XMLStreamException {
		List<Photo> photos = postController.getPhotoForBlogById(blogName, post.getId());

		writer.writeStartElement("photo-caption");
		writer.writeCharacters(photos.get(0).getCaption());
		writer.writeEndElement();

		if (photos.get(0).getPhotoLinkUrl() != null) {
			writer.writeStartElement("photo-link-url");
			writer.writeCharacters(photos.get(0).getPhotoLinkUrl());
			writer.writeEndElement();
		}

		addPhotoWithSizes(photos.get(0), writer);

		if (photos.size() > 1) {
			writer.writeStartElement("photoset");
			for (int i = 0; i < photos.size(); i++) {
				writer.writeStartElement("photo");
				writer.writeAttribute("offset", photos.get(i).getOffset());
				writer.writeAttribute("caption", "");
				writer.writeAttribute("width", String.valueOf(photos.get(i).getWidth()));
				writer.writeAttribute("height", String.valueOf(photos.get(i).getHeight()));
				addPhotoWithSizes(photos.get(i), writer);
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}
	}

	/**
	 * Helper function for adding a <code>photo-url</code> element to the XML, since
	 * these are added multiple times per photo.
	 * 
	 * @param photo  The specific photo being written
	 * @param writer The StAX XML writer
	 * @throws XMLStreamException if any errors are encountered by the underlying
	 *                            StAX objects
	 */
	private static void addPhotoWithSizes(Photo photo, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("photo-url");
		writer.writeAttribute("max-width", "1280");
		writer.writeCharacters(photo.getUrl1280());
		writer.writeEndElement();

		writer.writeStartElement("photo-url");
		writer.writeAttribute("max-width", "500");
		writer.writeCharacters(photo.getUrl500());
		writer.writeEndElement();

		writer.writeStartElement("photo-url");
		writer.writeAttribute("max-width", "400");
		writer.writeCharacters(photo.getUrl400());
		writer.writeEndElement();

		writer.writeStartElement("photo-url");
		writer.writeAttribute("max-width", "250");
		writer.writeCharacters(photo.getUrl250());
		writer.writeEndElement();

		writer.writeStartElement("photo-url");
		writer.writeAttribute("max-width", "100");
		writer.writeCharacters(photo.getUrl100());
		writer.writeEndElement();

		writer.writeStartElement("photo-url");
		writer.writeAttribute("max-width", "75");
		writer.writeCharacters(photo.getUrl75());
		writer.writeEndElement();

	}

	/**
	 * Adds the XML for a video to the export. <b>This method isn't fully
	 * functioning, as video posts require some XML elements which were not included
	 * in the original import into the TEV database;</b> for this reason, the UI is
	 * currently blocking users from adding video posts to the staging area.
	 * 
	 * @param post           The post to be written to the XML
	 * @param writer         The StAX XML writer
	 * @param postController The REST controller for getting additional data
	 * @throws XMLStreamException if any errors are encountered by the underlying
	 *                            StAX objects
	 */
	private static void addVideoBody(Post post, XMLStreamWriter writer, TEVPostRestController postController)
			throws XMLStreamException {
		Video video = postController.getVideoForBlogById(post.getTumblelog(), post.getId());

		writer.writeStartElement("video-source");

		writer.writeStartElement("content-type");
		writer.writeCharacters(video.getContentType());
		writer.writeEndElement();

		writer.writeStartElement("extension");
		writer.writeCharacters(video.getExtension());
		writer.writeEndElement();

		writer.writeStartElement("width");
		writer.writeCharacters(String.valueOf(video.getWidth()));
		writer.writeEndElement();

		writer.writeStartElement("height");
		writer.writeCharacters(String.valueOf(video.getHeight()));
		writer.writeEndElement();

		writer.writeStartElement("duration");
		writer.writeCharacters(String.valueOf(video.getDuration()));
		writer.writeEndElement();

		writer.writeStartElement("revision");
		writer.writeCharacters(video.getRevision());
		writer.writeEndElement();

		writer.writeEndElement();

		writer.writeStartElement("video-caption");
		writer.writeCharacters(video.getVideoCaption());
		writer.writeEndElement();

		// TODO: video-player elements - necessary?

	}

	/**
	 * Writes the XML for an answer post to the XML response.
	 * 
	 * @param post           The post being written
	 * @param writer         The StAX XML writer
	 * @param postController The REST controller for getting additional data
	 * @throws XMLStreamException if any errors are encountered by the underlying
	 *                            StAX objects
	 */
	private static void addAnswerBody(Post post, XMLStreamWriter writer, TEVPostRestController postController)
			throws XMLStreamException {
		Answer answer = postController.getAnswerForBlogById(post.getTumblelog(), post.getId());

		writer.writeStartElement("question");
		writer.writeCharacters(answer.getQuestion());
		writer.writeEndElement();

		writer.writeStartElement("answer");
		writer.writeCharacters(answer.getAnswer());
		writer.writeEndElement();

	}

	/**
	 * Writes the XML for a link post to the XML response
	 * 
	 * @param post           The Post being written
	 * @param writer         The StAX XML writer
	 * @param postController The REST controller for getting additional data
	 * @throws XMLStreamException if any errors are encountered by the underlying
	 *                            StAX objects
	 */
	private static void addLinkBody(Post post, XMLStreamWriter writer, TEVPostRestController postController)
			throws XMLStreamException {
		Link link = postController.getLinkForBlogById(post.getTumblelog(), post.getId());

		writer.writeStartElement("link-text");
		writer.writeCharacters(link.getText());
		writer.writeEndElement();

		writer.writeStartElement("link-url");
		writer.writeCharacters(link.getUrl());
		writer.writeEndElement();

		writer.writeStartElement("link-description");
		writer.writeCharacters(link.getDescription());
		writer.writeEndElement();

	}

	/**
	 * Writes the XML for a regular post
	 * 
	 * @param post           The post being written
	 * @param writer         The StAX XML writer
	 * @param postController The REST controller for getting additional data about
	 *                       the post
	 * @throws XMLStreamException if any errors are encountered by the underlying
	 *                            StAX objects
	 */
	private static void addRegularBody(Post post, XMLStreamWriter writer, TEVPostRestController postController)
			throws XMLStreamException {
		Regular regular = postController.getRegularForBlogById(post.getTumblelog(), post.getId());

		if (regular.getTitle() != null && regular.getTitle().length() > 0) {
			writer.writeStartElement("regular-title");
			writer.writeCharacters(regular.getTitle());
			writer.writeEndElement();
		}

		writer.writeStartElement("regular-body");
		writer.writeCharacters(regular.getBody());
		writer.writeEndElement();

	}

	/**
	 * Helper function for adding the attributes for the post to the
	 * <code>post</code> element.
	 * 
	 * @param post   The post being written
	 * @param writer The StAX XML writer
	 * @throws XMLStreamException if any errors are encountered by the underlying
	 *                            StAX objects
	 */
	private static void addPostAttributes(Post post, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeAttribute("id", String.valueOf(post.getId()));
		writer.writeAttribute("url", post.getUrl());
		writer.writeAttribute("url-with-slug", post.getUrlWithSlug());
		writer.writeAttribute("type", post.getType());
		writer.writeAttribute("date-gmt", post.getDateGmt());
		writer.writeAttribute("date", post.getDate());
		writer.writeAttribute("unix-timestamp", String.valueOf(post.getUnixtimestamp()));
		writer.writeAttribute("format", "html"); // hard-coded
		writer.writeAttribute("reblog-key", post.getReblogKey());
		writer.writeAttribute("slug", post.getSlug());
		writer.writeAttribute("state", post.getState());
		writer.writeAttribute("is_reblog", String.valueOf(post.getIsReblog()));
		writer.writeAttribute("tumblelog", String.valueOf(post.getTumblelog()));

		if (post.getType().equals("photo")) {
			writer.writeAttribute("width", String.valueOf(post.getWidth()));
			writer.writeAttribute("height", String.valueOf(post.getHeight()));
		}
	}
}
