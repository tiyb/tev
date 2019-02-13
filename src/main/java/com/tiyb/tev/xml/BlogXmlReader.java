package com.tiyb.tev.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.tiyb.tev.datamodel.Answer;
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.datamodel.Type;
import com.tiyb.tev.datamodel.Video;
import com.tiyb.tev.datamodel.helpers.TEVSuperClass;
import com.tiyb.tev.exception.XMLParsingException;

/**
 * <p>
 * This class is responsible for parsing the XML file exported from Tumblr for a
 * user's blogs. It leverages the <b>StAX</b> library's event-based processing
 * model to read the file.
 * </p>
 * 
 * <p>
 * The XML export from Tumblr is... not always a well thought out piece of
 * information architecture. There are inconsistencies as to how some things are
 * structured, and so it had to be reverse engineered to figure things out.
 * Especially when it comes to "photo" posts, there are inconsistent ways in
 * which links to photos are handled. That being said, the <i>general</i>
 * structure is that there is a <code>&lt;post&gt;</code> element for each post,
 * with attributes for all of the data that is common to <i>any</i> kind of a
 * post; URL, "slug," date it was posted (in various formats), etc. Under that
 * <code>&lt;post&gt;</code> element are child elements, however, the child
 * elements to be found are very different, depending on the type of post. An
 * "answer" post, for example, will have two child elements (for the question
 * and the answer), whereas a "photo" post will have numerous child elements
 * covering the post's caption, various sizes of the images, etc.
 * </p>
 * 
 * <p>
 * Not all of the attributes or elements in the document are pulled out; only
 * the ones that are useful for the TEV application. Probably about 90% of the
 * information is retrieved, however.
 * </p>
 * 
 * <p>
 * The general approach taken is that the <code>parseDocument()</code> method
 * sets up some initial variables, the <code>readPosts()</code> method then goes
 * through the document post-by-post, and as it determines what type each post
 * is, additional methods are called to read the additional, type-specific XML
 * within the post's XML element (answer, link, photo, etc.).
 * </p>
 * 
 * @author tiyb
 * @apiviz.landmark
 * @apiviz.uses com.tiyb.tev.datamodel.helpers.TEVSuperClass
 * @apiviz.uses javax.xml.stream.XMLEventReader
 */
public class BlogXmlReader {

	/**
	 * Value used for the exception encountered when XML parsing fails because of
	 * unexpected conditions (i.e. a closing tag was expected but never arrived).
	 */
	private static final String END_OF_FILE_ERROR = "Premature end of file";

	/**
	 * This is the main method of the class, which kicks off the processing of the
	 * document. It doesn't do any work itself, it simply sets up some type-related
	 * metadata, and calls the <code>readPosts()</code> method to get into the
	 * actual XML document.
	 * 
	 * @param xmlFile        <code>InputStream</code> containing the XML document to
	 *                       be parsed.
	 * @param allowableTypes A list of the different types (link, answer, photo,
	 *                       etc.), with their associated IDs, so that the posts can
	 *                       be linked in the database.
	 * @return A <code>TEVSuperClass</code> object, which simply packages up all of
	 *         the objects that have been generated.
	 * @throws XMLParsingException
	 */
	public static TEVSuperClass parseDocument(InputStream xmlFile, List<Type> allowableTypes)
			throws XMLParsingException {
		Map<Long, String> typeEntries = new HashMap<Long, String>();
		Map<String, Long> typeIDs = new HashMap<String, Long>();
		loadTypeData(allowableTypes, typeEntries, typeIDs);

		TEVSuperClass masterData = readPosts(xmlFile, typeEntries, typeIDs);
		return masterData;
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
	 * <code>Post</code> object is created.</li>
	 * <li>The attributes are read into that object (via the
	 * <code>readPostAttributes()</code> method, to populate its data.</li>
	 * <li>Depending on the value of the <code>type</code> attribute, one of the
	 * additional methods is called to parse the type-specific data</li>
	 * <li>Those child methods populate their own objects, and then add them to the
	 * <code>TEVSuperClass</code> object</i>
	 * <li>Once the child method has completed, the <code>Post</code> object is also
	 * added to the <code>TEVSuperClass</code> object. (This would normally have been
	 * done in the "end element" event for the <code>&lt;post&gt;</code> element,
	 * however, the nature of this processing means that the "end element" event is
	 * actually consumed by the child methods.)</li>
	 * </ol>
	 * 
	 * @param xmlFile     The stream containing the XML file to be parsed
	 * @param typeEntries A list of available types, in a <code>Map</code> for easy
	 *                    access by ID
	 * @param typeIDs     A list of available types, in a <code>Map</code> for easy
	 *                    access by name
	 * @return A <code>TEVSuperClass</code> object, containing all of the data
	 *         pulled out of the XML document.
	 * @throws XMLParsingException
	 */
	private static TEVSuperClass readPosts(InputStream xmlFile, Map<Long, String> typeEntries,
			Map<String, Long> typeIDs) throws XMLParsingException {
		TEVSuperClass masterData = new TEVSuperClass();

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
						readPostAttributes(se, post, typeIDs);
						switch (typeEntries.get(post.getType())) {
						case "regular":
							Regular regular = readRegular(reader, post);
							masterData.getRegulars().add(regular);
							break;
						case "answer":
							Answer answer = readAnswer(reader, post);
							masterData.getAnswers().add(answer);
							break;
						case "link":
							Link link = readLink(reader, post);
							masterData.getLinks().add(link);
							break;
						case "photo":
							List<Photo> photos = readPhotos(reader, post);
							masterData.getPhotos().addAll(photos);
							break;
						case "video":
							Video video = readVideos(reader, post);
							masterData.getVideos().add(video);
							break;
						}

						masterData.getPosts().add(post);
					}
				}
			}
		} catch (XMLStreamException e) {
			throw new XMLParsingException();
		} finally {

		}

		return masterData;
	}

	/**
	 * Helper function specifically for reading the attributes from a
	 * <code>&lt;post&gt;</code> element. The logic could easily have been
	 * incorporated into <code>readPosts()</code>, but the method would have gotten
	 * much longer.
	 * 
	 * @param se      The <code>StartElement</code> object being processed
	 * @param post    The <code>Post</code> object to which the data from each
	 *                element should be added
	 * @param typeIDs The <code>Map</code> containing the list of types
	 */
	private static void readPostAttributes(StartElement se, Post post, Map<String, Long> typeIDs) {
		@SuppressWarnings("unchecked")
		Iterator<Attribute> atts = se.getAttributes();
		while (atts.hasNext()) {
			Attribute att = atts.next();
			String attName = att.getName().getLocalPart();
			switch (attName) {
			case "id":
				post.setId(new Long(att.getValue()));
				break;
			case "url":
				post.setUrl(att.getValue());
				break;
			case "url-with-slug":
				post.setUrlWithSlug(att.getValue());
				break;
			case "type":
				post.setType(typeIDs.get(att.getValue()));
				break;
			case "date-gmt":
				post.setDateGmt(att.getValue());
				break;
			case "date":
				post.setDate(att.getValue());
				break;
			case "unix-timestamp":
				post.setUnixtimestamp(new Long(att.getValue()));
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
				// attribute not used
				break;
			case "is_reblog":
				post.setIsReblog(new Boolean(att.getValue()));
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
	 * @param reader The <code>XMLEventReader</code> object for the XML document
	 *               being parsed.
	 * @param post   The current post, for use as the primary key of the
	 *               <code>Regular</code> object and for setting tags
	 * @return A <code>Regular</code> object with the data read
	 * @throws XMLStreamException
	 */
	private static Regular readRegular(XMLEventReader reader, Post post) throws XMLStreamException {
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
					post.setTags(addTagToString(post.getTags(), readCharacters(reader)));
				}
			} else if (event.isEndElement()) {
				EndElement ee = event.asEndElement();

				if (ee.getName().getLocalPart().equals("post")) {
					return regular;
				}
			}
		}

		throw new XMLStreamException(END_OF_FILE_ERROR);
	}

	/**
	 * This method is responsible for reading the sub-content under a post for
	 * content related to an "answer" post. The content is very simple, containing
	 * only "question" and "answer" elements.
	 * 
	 * @param reader The event reader parsing the current document
	 * @param post   The current post, for use as the primary key of the
	 *               <code>Answer</code> object and for setting tags
	 * @return The "answer" data
	 * @throws XMLStreamException
	 */
	private static Answer readAnswer(XMLEventReader reader, Post post) throws XMLStreamException {
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
					post.setTags(addTagToString(post.getTags(), readCharacters(reader)));
				}
			} else if (event.isEndElement()) {
				EndElement ee = event.asEndElement();

				if (ee.getName().getLocalPart().equals("post")) {
					return answer;
				}
			}
		}

		throw new XMLStreamException(END_OF_FILE_ERROR);
	}

	/**
	 * This method is responsible for reading the sub-content under a post for
	 * content related to a "link" post. The content contains only three elements:
	 * "link-description", "link-text", and "link-url".
	 * 
	 * @param reader The event parser
	 * @param post   The currently processed post
	 * @return The link data
	 * @throws XMLStreamException
	 */
	private static Link readLink(XMLEventReader reader, Post post) throws XMLStreamException {
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
					post.setTags(addTagToString(post.getTags(), readCharacters(reader)));
				}
			} else if (event.isEndElement()) {
				EndElement ee = event.asEndElement();

				if (ee.getName().getLocalPart().equals("post")) {
					return link;
				}
			}
		}

		throw new XMLStreamException(END_OF_FILE_ERROR);
	}

	/**
	 * <p>
	 * This method is responsible for reading the sub-content under a post for
	 * content related to a "photo" post. This is the most complex of the post
	 * types, as far as the XML is concerned -- but is also the least consistent of
	 * the different data types. There is one consistent child element that always
	 * appears ("photo-caption"), and then there is content for each photo within
	 * the post. For each photo, there are 6 "photo-url" elements, for URLs to
	 * differently sized versions of the same image (1280, 500, 400, ...).
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
	 * <li>Any "photo-url" elements are read in, and the inforamtion is saved for
	 * later use</li>
	 * <li><i>If</i> there is a "photoset" element, a sub-method is called for
	 * parsing the content. That method will create a <code>Photo</code> object for
	 * each photo it encounters.</li>
	 * <li>When the end of the "post" element is reached, the list of
	 * <code>Photo</code> objects is checked, to see if any were created (as part of
	 * a photoset) or not; if not, a single <code>Photo</code> object is created,
	 * with the URL and caption data already collected, and put in the collection.
	 * If the collection is already populated, it is simply returned as-is, since
	 * the "photo-url" elements at the root of the <code>&lt;post&gt;</code> element
	 * are duplicated in the "photoset", and have therefore already been added.</li>
	 * </ol>
	 * 
	 * @param reader The event reader being used to parse the XML
	 * @param post   The currently processed post
	 * @return A list of <code>Photo</code> objects
	 * @throws XMLStreamException
	 */
	private static List<Photo> readPhotos(XMLEventReader reader, Post post) throws XMLStreamException {
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
					post.setTags(addTagToString(post.getTags(), readCharacters(reader)));
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
	 * @param reader The event parser
	 * @param post   The currently processed post
	 * @return The video data
	 * @throws XMLStreamException
	 */
	private static Video readVideos(XMLEventReader reader, Post post) throws XMLStreamException {
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
					video.setWidth(new Integer(readCharacters(reader)));
				} else if (se.getName().getLocalPart().equals("height")) {
					video.setHeight(new Integer(readCharacters(reader)));
				} else if (se.getName().getLocalPart().equals("duration")) {
					video.setDuration(new Integer(readCharacters(reader)));
				} else if (se.getName().getLocalPart().equals("revision")) {
					video.setRevision(readCharacters(reader));
				} else if (se.getName().getLocalPart().equals("video-caption")) {
					video.setVideoCaption(readCharacters(reader));
				} else if (se.getName().getLocalPart().equals("tag")) {
					post.setTags(addTagToString(post.getTags(), readCharacters(reader)));
				}
			} else if (event.isEndElement()) {
				EndElement ee = event.asEndElement();

				if (ee.getName().getLocalPart().equals("post")) {
					return video;
				}
			}
		}

		throw new XMLStreamException(END_OF_FILE_ERROR);
	}

	/**
	 * <p>
	 * This is a helper function, which reads characters out of an element, up to
	 * the end of that element (the closing tag). There can be multiple events fired
	 * as the event reader goes through the content, up to the end of the tag,
	 * because there can be a mix of "text" content and "CDATA" content; both of
	 * these are combined together into one String. Because the event for the
	 * closing tag is consumed here, it cannot be consumed by the calling method,
	 * but the logic for all of the calling methods takes that into account.
	 * </p>
	 * 
	 * <p>
	 * Any character entities within the data -- e.g. &amp;gt; and &amp;lt; instead
	 * of &gt; and &lt; -- get unescaped, which is the desired behaviour for this
	 * app.
	 * </p>
	 * 
	 * @param reader The event parser from which the text should be extracted.
	 * @return A simple <code>String</code> with the returned text
	 * @throws XMLStreamException
	 */
	private static String readCharacters(XMLEventReader reader) throws XMLStreamException {
		StringBuilder result = new StringBuilder();

		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();

			if (event.isCharacters() || event.isEntityReference()) {
				Characters chars = event.asCharacters();
				result.append(chars.getData());
			} else if (event.isEndElement()) {
				return result.toString();
			}
		}

		throw new XMLStreamException(END_OF_FILE_ERROR);
	}

	/**
	 * Helper function which does nothing but create two opposite <code>Map</code>
	 * objects, with Type data:
	 * 
	 * <ul>
	 * <li>One with the ID as the key, and the type as the field, and</li>
	 * <li>One with the type as the key, and the ID as the field</li>
	 * </ul>
	 * 
	 * @param allowableTypes The full list of types from the database, with their
	 *                       corresponding IDs
	 * @param typeEntries    The <code>Map</code> to be used with ID as key and type
	 *                       as value
	 * @param typeIDs        The <code>Map</code> to be used with type as the key
	 *                       and ID as value
	 */
	private static void loadTypeData(List<Type> allowableTypes, Map<Long, String> typeEntries,
			Map<String, Long> typeIDs) {
		for (Type type : allowableTypes) {
			typeEntries.put(type.getId(), type.getType());
			typeIDs.put(type.getType(), type.getId());
		}
	}

	/**
	 * Helper function to add a tag to a list of tags; the intent is for the list to
	 * be comma-separated
	 * 
	 * @param original The existing list of tags (which could be empty)
	 * @param tag      The tag to be added
	 * @return Amended string
	 */
	private static String addTagToString(String original, String tag) {
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
