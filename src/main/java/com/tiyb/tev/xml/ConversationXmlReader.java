package com.tiyb.tev.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.springframework.web.multipart.MultipartFile;

import com.tiyb.tev.controller.TEVConvoRestController;
import com.tiyb.tev.controller.TEVMetadataRestController;
import com.tiyb.tev.datamodel.Conversation;
import com.tiyb.tev.datamodel.ConversationMessage;
import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.exception.BlogMismatchParsingException;
import com.tiyb.tev.exception.ResourceNotFoundException;
import com.tiyb.tev.exception.XMLParsingException;

/**
 * This class is responsible for reading in an XML export from Tumblr containing
 * conversations. A couple of interesting caveats:
 * 
 * <ul>
 * <li>message[@type=IMAGE] has a child element, for the photo's URL, whereas
 * other messages just have the text of the message in the "message" element
 * itself.</li>
 * <li>For some reason two different formats are used for identifying users; the
 * "participants" element has their <b>name</b>, but actual messages have their
 * internal <b>ID</b>. These two don't relate to each other in any way. So the
 * internal ID for the TEV user has to be inferred.</li>
 * </ul>
 * 
 * @author tiyb
 */
public class ConversationXmlReader extends TEVXmlReader {

	/**
	 * <p>
	 * Main method. There are multiple steps to parsing the XML file:
	 * </p>
	 * 
	 * <ol>
	 * <li>Get a stream for the file</li>
	 * <li>Use that stream to call the {@link #getMainParticipant(InputStream)
	 * getMainParticipant()} method to parse the XML document, to determine the main
	 * Tumblr user's name, avatar URL, and internal ID</li>
	 * <li>Update the application's metadata with that information</li>
	 * <li>Get a <i>new</i> stream (to start over at the beginning of the file)</li>
	 * <li>Use that stream to call the
	 * {@link #readConversations(InputStream, String, String, TEVMetadataRestController, TEVConvoRestController, String)
	 * readConversations()} method to parse the document again, this time reading in
	 * all of the data (making use of the main Tumblr user information captured
	 * earlier)</li>
	 * </ol>
	 * 
	 * <p>
	 * Data is inserted directly into the database, via the REST APIs (accessed via
	 * the <code>convoRestController</code> parameter).
	 * </p>
	 * 
	 * @param xmlFile         File containing XML to be parsed
	 * @param mdController    Used for working with the application's metadata
	 * @param convoController Used for updating the database as each
	 *                        conversation/message is parsed
	 */
	public static void parseDocument(MultipartFile xmlFile, TEVMetadataRestController mdController,
			TEVConvoRestController convoController, String blogName) {
		Metadata md = mdController.getMetadataForBlog(blogName);
		InputStream xmlStream;

		try {
			InputStream participantXmlStream = xmlFile.getInputStream();
			Participant mainParticipant = getMainParticipant(participantXmlStream);
			if (!blogName.equals(mainParticipant.name)) {
				logger.error("Mismatch between expected blog name (" + blogName + ") and main participant name ("
						+ mainParticipant.name + ")");
				throw new BlogMismatchParsingException(blogName, mainParticipant.name);
			}
			md.setMainTumblrUser(mainParticipant.name);
			md.setMainTumblrUserAvatarUrl(mainParticipant.avatarURL);
			String mainParticipantID = mainParticipant.id;
			md = mdController.updateMetadata(md.getId(), md);

			xmlStream = xmlFile.getInputStream();
			readConversations(xmlStream, md.getMainTumblrUser(), mainParticipantID, mdController, convoController,
					blogName);
		} catch (IOException e) {
			logger.error("Error parsing XML file: ", e);
			throw new XMLParsingException();
		}
	}

	/**
	 * <p>
	 * The XML is set up such that each conversation is between two participants:
	 * the main Tumblr user, and another user. Therefore, this helper function
	 * parses through the XML document looking for participants; as soon as it finds
	 * a participant that's been listed <b>more than once,</b> it makes that
	 * participant the main Tumblr user, so it returns that user (and, URL for the
	 * user's avatar, and ID).
	 * </p>
	 * 
	 * <p>
	 * There are two versions of the user in the XML: the <b>name</b> and an
	 * <b>opaque ID</b>, but they don't map to each other in the XML document, so
	 * there's no way to tell which ID belongs to which user. So the code also looks
	 * for duplicate versions of IDs; when an ID is found in multiple conversations,
	 * it's assumed to be the ID of the TEV user.
	 * </p>
	 * 
	 * @param xmlStream The XML file to be parsed
	 * @return A {@link com.tiyb.tev.xml.ConversationXmlReader.Participant
	 *         Participant} object, with the relevant data
	 */
	private static Participant getMainParticipant(InputStream xmlStream) {
		Participant returnParticipant = new Participant();
		Map<String, String> participants = new HashMap<String, String>();
		List<String> nameIds = new ArrayList<String>();
		boolean newConversation = false;

		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLEventReader reader = factory.createXMLEventReader(xmlStream);

			while (reader.hasNext()) {
				XMLEvent event = reader.nextEvent();

				if (event.isStartElement()) {
					StartElement se = event.asStartElement();

					if (se.getName().getLocalPart().equals("participant")) {
						@SuppressWarnings("unchecked")
						Iterator<Attribute> atts = se.getAttributes();
						String participantName = "";
						String participantURL = "";

						while (atts.hasNext()) {
							Attribute att = atts.next();
							if (att.getName().getLocalPart().equals("avatar_url")) {
								participantURL = att.getValue();
							}
						}

						participantName = readCharacters(reader);

						String participantInList = participants.get(participantName);
						if (participantInList == null) {
							participants.put(participantName, participantURL);
						} else {
							returnParticipant.name = participantName;
							returnParticipant.avatarURL = participantURL;
						}
					} else if (se.getName().getLocalPart().equals("message")) {
						@SuppressWarnings("unchecked")
						Iterator<Attribute> atts = se.getAttributes();

						while (atts.hasNext()) {
							Attribute att = atts.next();
							if (att.getName().getLocalPart().equals("participant")) {
								if (newConversation) {
									if (nameIds.contains(att.getValue())) {
										returnParticipant.id = att.getValue();
										return returnParticipant;
									}
								} else {
									if (!nameIds.contains(att.getValue())) {
										nameIds.add(att.getValue());
									}
								}
							}
						}
					}
				} else if (event.isEndElement()) {
					EndElement ee = event.asEndElement();

					if (ee.getName().getLocalPart().equals("messages")) {
						newConversation = true;
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception thrown by XML parser: ", e);
			throw new XMLParsingException();
		}

		logger.error("Unexpected end of file found in getMainParticipant()");
		throw new XMLParsingException();
	}

	/**
	 * <p>
	 * Reads in the XML file, conversation by conversation. Whenever a new
	 * conversation element is reached a new object is created, the participant is
	 * discovered (via {@link #getParticipantName(XMLEventReader, String)
	 * getParticipantName()}, and then {@link #getMessages(XMLEventReader, String)
	 * getMessages()} is called to get all of the messages. The count of that list
	 * is added to the conversation object.
	 * </p>
	 * 
	 * <p>
	 * Logic also takes into account the "overwrite conversations" flag in metadata.
	 * If the flag is set, all data is wiped ahead of time. When the flag is not
	 * set, as each conversation is encountered the REST API is used to look for
	 * that conversation: if it's found and the number of new messages isn't greater
	 * than the number of existing messages it is left alone; otherwise, messages
	 * are wiped, the conversation is set to no longer be read-only, and the new set
	 * of messages is uploaded.
	 * </p>
	 * 
	 * <p>
	 * Finally, it should be noted that participant <b>names</b> sometimes change,
	 * but their <b>IDs</b> stay the same. So when searching the database for
	 * existing conversations, the search is initially done by ID. In cases where a
	 * participant has <i>changed</i> their name (and overwrite is set to false),
	 * the name will be updated in the DB. In some cases, where all of the messages
	 * were sent from the main blog (none received from the participant), the
	 * conversation is saved to the DB with an empty Participant ID (since the only
	 * place to get that ID is from a message received from the Participant). For
	 * this reason, the search for Conversations has a fallback, whereby it searches
	 * by name if searching by ID turns up 0 results. In cases where where 1) an
	 * initial import is performed, including a conversation with messages sent from
	 * the main blog and non received from the Participant; 2) a new export is
	 * uploaded (with overwrite turned off); 3) the conversation was augmented with
	 * messages from the Participant; <i>and</i> 4) the Participant changed their
	 * name, a duplicate conversation will be created. This is made even more
	 * confusing by the fact that participant <b>names</b> are sometimes reused, so
	 * the code looks for duplicate names, and appends a "1" to the end. (i.e.
	 * "blogname", "blogname 1", "blogname 1 1", etc.)
	 * </p>
	 * 
	 * @param xmlFile            Stream containing the XML file to be parsed
	 * @param mainTumblrUserName The Tumblr name of the user of the application
	 * @param mainTumblrUserId   The ID assigned to the user of the application by
	 *                           Tumblr
	 * @param mdController       Controller used to update the database with
	 *                           conversations/messages
	 * @throws XMLParsingException
	 */
	private static void readConversations(InputStream xmlFile, String mainTumblrUserName, String mainTumblrUserId,
			TEVMetadataRestController mdController, TEVConvoRestController convoController, String blogName)
			throws XMLParsingException {
		Metadata md = mdController.getMetadataForBlog(blogName);
		boolean isOverwriteConvos = md.getOverwriteConvoData();
		List<String> allParticipants = new ArrayList<String>();

		if (isOverwriteConvos) {
			convoController.deleteAllConvoMsgsForBlog(blogName);
			convoController.deleteAllConversationsForBlog(blogName);
		}
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLEventReader reader = factory.createXMLEventReader(xmlFile);

			Conversation conversation = null;
			List<ConversationMessage> messages = null;
			ConversationXmlReader.Participant participant = null;

			while (reader.hasNext()) {
				XMLEvent event = reader.nextEvent();

				if (event.isStartElement()) {
					StartElement se = event.asStartElement();

					if (se.getName().getLocalPart().equals("conversation")) {
						participant = getParticipantName(reader, mainTumblrUserName);
						while (allParticipants.contains(participant.name)) {
							participant.name = participant.name + " 1";
						}
						allParticipants.add(participant.name);
						MessageSuperStructure messageData = getMessages(reader, mainTumblrUserId);
						messages = messageData.messages;

						conversation = new Conversation();
						conversation.setParticipant(participant.name);
						conversation.setParticipantAvatarUrl(participant.avatarURL);
						conversation.setParticipantId(messageData.participantId);
						conversation.setNumMessages(messages.size());
						conversation.setBlog(blogName);

						boolean isSendConvoToServer = true;

						if (isOverwriteConvos) {
							conversation = convoController.createConversationForBlog(blogName, conversation);
							isSendConvoToServer = true;
						} else {
							try {
								Conversation convoOnServer = convoController
										.getConversationForBlogByParticipantIdOrName(blogName,
												conversation.getParticipantId(), participant.name);
								if ((messages.size() > convoOnServer.getNumMessages())
										|| !convoOnServer.getParticipant().equals(conversation.getParticipant())) {
									convoOnServer.setHideConversation(false);
									convoOnServer.setNumMessages(messages.size());
									convoOnServer.setParticipant(participant.name);
									convoOnServer.setParticipantAvatarUrl(participant.avatarURL);
									convoOnServer = convoController.updateConversationForBlog(blogName,
											convoOnServer.getId(), convoOnServer);
									List<ConversationMessage> msgsForConv = convoController.getConvoMsgForBlogByConvoID(
											convoOnServer.getBlog(), convoOnServer.getId());
									for (ConversationMessage msg : msgsForConv) {
										convoController.deleteConversationMessageForBlog(convoOnServer.getBlog(),
												msg.getId());
									}
									isSendConvoToServer = true;
									conversation.setId(convoOnServer.getId());
								} else {
									isSendConvoToServer = false;
								}
							} catch (ResourceNotFoundException e) {
								conversation = convoController.createConversationForBlog(blogName, conversation);
								isSendConvoToServer = true;
							}
						}

						if (isSendConvoToServer) {
							uploadMessagesForConvo(convoController, messages, conversation.getId(),
									conversation.getBlog());
						}
					}
				}
			}
		} catch (XMLStreamException e) {
			logger.error("Error thrown by XML parser: ", e);
			throw new XMLParsingException();
		} finally {

		}
	}

	/**
	 * Helper function to upload messages for a given conversation
	 * 
	 * @param restController The controller for accessing the REST API
	 * @param messages       The messages to upload
	 * @param convoID        The ID of the conversation
	 */
	private static void uploadMessagesForConvo(TEVConvoRestController restController,
			List<ConversationMessage> messages, Long convoID, String blogName) {
		for (ConversationMessage msg : messages) {
			msg.setConversationId(convoID);
			restController.createConvoMessageForBlog(blogName, msg);
		}
	}

	/**
	 * Gets a list of messages (stops when the end of the conversation is reached).
	 * The two attributes are read (via
	 * {@link #readMessageAttributes(StartElement, ConversationMessage, String)
	 * readMessageAttributes()}), followed by the text of the element.
	 * 
	 * @param reader       Stream containing the XML document being read
	 * @param tumblrUserID Tumblr ID of the TEV user
	 * @return list of messages, instead a
	 *         {@link com.tiyb.tev.xml.ConversationXmlReader.MessageSuperStructure
	 *         MessageSuperStructure} object
	 * @throws XMLStreamException
	 */
	private static MessageSuperStructure getMessages(XMLEventReader reader, String tumblrUserID)
			throws XMLStreamException {
		List<ConversationMessage> messages = new ArrayList<ConversationMessage>();
		MessageSuperStructure returnObject = new MessageSuperStructure();

		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();

			if (event.isStartElement()) {
				StartElement se = event.asStartElement();

				if (se.getName().getLocalPart().equals("message")) {
					ConversationMessage currentMessage = new ConversationMessage();
					String pId = readMessageAttributes(se, currentMessage, tumblrUserID);
					if ((returnObject.participantId.equals("")) && (!pId.equals(""))) {
						returnObject.participantId = pId;
					}
					if (currentMessage.getType().equals("IMAGE")) {
						currentMessage.setMessage(readImageMessage(reader));
					} else {
						currentMessage.setMessage(readCharacters(reader));
					}
					messages.add(currentMessage);
				}
			} else if (event.isEndElement()) {
				EndElement ee = event.asEndElement();

				if (ee.getName().getLocalPart().equals("conversation")) {
					returnObject.messages = messages;
					return returnObject;
				}
			}
		}

		logger.error("Unexpected end of file reached in getMessages");
		throw new XMLStreamException(END_OF_FILE_ERROR);
	}

	/**
	 * This helper method reads in message content for IMAGE messages. The structure
	 * of an IMAGE is different from other messages; typically, the message contents
	 * are simply the child text of the "message" element, but when the type is
	 * IMAGE, there is a child "photo-url" element as well. The "photo-url" also has
	 * a max-width attribute, but this app ignores it
	 * 
	 * @param reader The event reader containing the XML document
	 * @return String for the message content - i.e., the photo's URL
	 * @throws XMLStreamException
	 */
	private static String readImageMessage(XMLEventReader reader) throws XMLStreamException {
		String imageMessage = "";

		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();

			if (event.isStartElement()) {
				StartElement se = event.asStartElement();

				if (se.getName().getLocalPart().equals("photo-url")) {
					imageMessage = readCharacters(reader);
				}
			} else if (event.isEndElement()) {
				EndElement ee = event.asEndElement();

				if (ee.getName().getLocalPart().equals("message")) {
					return imageMessage;
				}
			}
		}

		logger.error("Unexpected end of file reached in readImageMessage");
		throw new XMLStreamException(END_OF_FILE_ERROR);
	}

	/**
	 * Helper method to read in a message's attributes
	 * 
	 * @param startElement   The {@link javax.xml.stream.events.StartElement
	 *                       StartElement} object currently being processed
	 * @param currentMessage The message to add the data to
	 * @param tumblrUserID   The ID of the main user
	 * @return The ID of the participant
	 */
	private static String readMessageAttributes(StartElement startElement, ConversationMessage currentMessage,
			String tumblrUserID) {
		@SuppressWarnings("unchecked")
		Iterator<Attribute> atts = startElement.getAttributes();
		String participantId = "";

		while (atts.hasNext()) {
			Attribute att = atts.next();
			String attName = att.getName().getLocalPart();

			switch (attName) {
			case "ts":
				currentMessage.setTimestamp(Long.parseLong(att.getValue()));
				break;
			case "participant":
				String participant = att.getValue();
				if (participant.equals(tumblrUserID)) {
					currentMessage.setReceived(false);
				} else {
					currentMessage.setReceived(true);
					participantId = participant;
				}
				break;
			case "type":
				currentMessage.setType(att.getValue());
				break;
			}
		}

		return participantId;
	}

	/**
	 * Helper function to get the participant name (and avatar URL) from a
	 * conversation. Each conversation contains a list of exactly two participants:
	 * the TEV user, and the <i>other</i> participant. Each "participant" element is
	 * read, and the one that is <i>not</i> that of the current Tumblr user is
	 * returned.
	 * 
	 * @param reader         The reader from which to read the "participant"
	 *                       elements
	 * @param tumblrUserName Tumblr name of the TEV user
	 * @return Helper {@link com.tiyb.tev.xml.ConversationXmlReader.Participant
	 *         Participant} object, with the details of the <i>other</i>
	 *         (non-TEV-user) participant in the conversation
	 * @throws XMLStreamException
	 */
	private static ConversationXmlReader.Participant getParticipantName(XMLEventReader reader, String tumblrUserName)
			throws XMLStreamException {
		String participantName = "";
		String participantAvatar = "";
		ConversationXmlReader.Participant participant = new ConversationXmlReader.Participant();

		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();

			if (event.isStartElement()) {
				StartElement se = event.asStartElement();

				if (se.getName().getLocalPart().equals("participant")) {
					@SuppressWarnings("unchecked")
					Iterator<Attribute> atts = se.getAttributes();
					while (atts.hasNext()) {
						Attribute att = atts.next();
						String attName = att.getName().getLocalPart();
						if (attName == "avatar_url") {
							participantAvatar = att.getValue();
							break;
						}
					}
					participantName = readCharacters(reader);
					if (!participantName.equals(tumblrUserName)) {
						participant.avatarURL = participantAvatar;
						participant.name = fixName(participantName);
					}
				}
			} else if (event.isEndElement()) {
				EndElement ee = event.asEndElement();

				if (ee.getName().getLocalPart().equals("participants")) {
					if (participant.name.equals("")) {
						participant.name = "NO NAME";
					}
					return participant;
				}
			}
		}

		logger.error("Unexpected end of file reached in getParticipantName");
		throw new XMLStreamException(END_OF_FILE_ERROR);
	}

	/**
	 * Helper function used to "fix" names for deactivated users. When a user has
	 * been deactivated, Tumblr returns the name as
	 * <code>username-deactivated</code> or
	 * <code>username-deactivatedyyyymmdd</code> or even
	 * <code>username-deact</code>. This function just keeps the username, without
	 * the "-deactivated" or "-deactivatedyyyymmdd" part.
	 * 
	 * @param participantName "raw" username
	 * @return Username without the postfix (if any)
	 */
	private static String fixName(String participantName) {
		int postfix = participantName.indexOf("-deact");
		if (postfix == -1) {
			return participantName;
		}

		participantName = participantName.substring(0, postfix);
		return participantName;
	}

	/**
	 * Helper class (essentially a struct), used for working with participant
	 * information. Since this is just an inner, helper class, the trouble wasn't
	 * taken to make it a proper bean with getters/setters, it just exposes public
	 * member variables.
	 *
	 */
	private static class Participant {
		/**
		 * The Tumblr user's public-facing name
		 */
		public String name;

		/**
		 * The URL of the Tumblr user's avatar
		 */
		public String avatarURL;

		/**
		 * The Tumblr user's ID, as contained in the conversation XML.
		 */
		public String id;
	}

	/**
	 * Helper class (essentially a struct), returned from the
	 * {@link ConversationXmlReader#getMessages(XMLEventReader, String)
	 * getMessages()} method, which needs to return more complicated data than just
	 * the list of messages - it also needs to determine the participant ID
	 */
	private static class MessageSuperStructure {
		/**
		 * List of populated messages
		 */
		public List<ConversationMessage> messages;

		/**
		 * The ID of the participant in the conversation
		 */
		public String participantId = "";
	}

}
