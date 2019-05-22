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
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.springframework.web.multipart.MultipartFile;

import com.tiyb.tev.controller.TEVRestController;
import com.tiyb.tev.datamodel.Conversation;
import com.tiyb.tev.datamodel.ConversationMessage;
import com.tiyb.tev.datamodel.Metadata;
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
 * "participants" element has their name, but actual messages have their
 * internal ID. These two don't relate to each other in any way. So the internal
 * ID for the TEV user has to be inferred.</li>
 * </ul>
 * 
 * @author tiyb
 * @apiviz.landmark
 * @apiviz.uses javax.xml.stream.XMLEventReader
 *
 */
public class ConversationXmlReader {

	/**
	 * Error message used when the end of a file is unexpectedly reached
	 */
	private static final String END_OF_FILE_MESSAGE = "Premature end of file";

	/**
	 * <p>
	 * Main method. There are multiple steps to parsing the XML file:
	 * </p>
	 * 
	 * <ol>
	 * <li>Get a stream for the file</li>
	 * <li>Use that stream to call the <code>getMainParticipant()</code> method to
	 * parse the XML document, to determine the main Tumblr user's name, avatar URL,
	 * and internal ID</li>
	 * <li>Update the application's metadata with that information</li>
	 * <li>Get a <i>new</i> stream (to start over at the beginning of the file)</li>
	 * <li>Use that stream to call the <code>readConversations()</code> method to
	 * parse the document again, this time reading in all of the data (making use of
	 * the main Tumblr user information captured earlier)</li>
	 * </ol>
	 * 
	 * <p>
	 * Data is inserted directly into the database, via the REST APIs (accessed via
	 * the <code>restController</code> parameter).
	 * </p>
	 * 
	 * @param xmlFile        File containing XML to be parsed
	 * @param restController Used for updating the database as each
	 *                       conversation/message is parsed
	 */
	public static void parseDocument(MultipartFile xmlFile, TEVRestController restController) {
		Metadata md = restController.getMetadata();
		InputStream xmlStream;

		try {
			InputStream participantXmlStream = xmlFile.getInputStream();
			ConversationUsers cu = getMainParticipant(participantXmlStream);
			md.setMainTumblrUser(cu.name);
			md.setMainTumblrUserAvatarUrl(cu.avatarURL);
			String mainParticipantID = cu.id;
			md = restController.updateMetadata(md);

			xmlStream = xmlFile.getInputStream();
			readConversations(xmlStream, md.getMainTumblrUser(), mainParticipantID, restController);
		} catch (IOException e) {
			throw new XMLParsingException();
		}
	}

	/**
	 * <p>
	 * The XML is set up such that each conversation is between two participants:
	 * the main Tumblr user, and another user. Therefore, this helper function
	 * parses through the XML document looking for participants; as soon as it finds
	 * a participant that's been listed <b>more than once,</b> it makes that
	 * participant the main Tumblr user, so it returns that user (and the URL for
	 * the user's avatar).
	 * </p>
	 * 
	 * <p>
	 * Because the Tumblr engineers are incompetent at <i>everything</i>, they've
	 * included two versions of the user: the <b>name</b> and an <b>opaque ID</b>,
	 * but they don't map to each other in the XML document, so there's no way to
	 * tell which ID belongs to which user. So the code also looks for duplicate
	 * versions of IDs; when an ID is found in multiple conversations, it's assumed
	 * to be the ID of the TEV user.
	 * </p>
	 * 
	 * @param participantXmlStream The XML file to be parsed
	 * @return A <code>ConversationUsers</code> object, with the relevant data
	 */
	private static ConversationUsers getMainParticipant(InputStream participantXmlStream) {
		ConversationUsers cu = new ConversationUsers();
		Map<String, String> participants = new HashMap<String, String>();
		List<String> nameIds = new ArrayList<String>();
		boolean newConversation = false;

		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLEventReader reader = factory.createXMLEventReader(participantXmlStream);

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
							cu.name = participantName;
							cu.avatarURL = participantURL;
						}
					} else if (se.getName().getLocalPart().equals("message")) {
						@SuppressWarnings("unchecked")
						Iterator<Attribute> atts = se.getAttributes();

						while (atts.hasNext()) {
							Attribute att = atts.next();
							if (att.getName().getLocalPart().equals("participant")) {
								if (newConversation) {
									if (nameIds.contains(att.getValue())) {
										cu.id = att.getValue();
										return cu;
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
			throw new XMLParsingException();
		}

		throw new XMLParsingException();
	}

	/**
	 * <p>
	 * Reads in the XML file, conversation by conversation. Whenever a new
	 * conversation element is reached a new object is created, the participant is
	 * discovered (via <code>getParticipantName()</code>, and then
	 * <code>getMessages()</code> is called to get all of the messages. The count of
	 * that list is added to the conversation object.
	 * </p>
	 * 
	 * <p>
	 * Logic also takes into account the "overwrite conversations" flag in metadata.
	 * If the flag is set, all data is wiped ahead of time. When the flag is not
	 * set: as each conversation is encountered the REST API is used to look for
	 * that conversation: if it's found and the number of new messages isn't greater
	 * than the number of existing messages it is left alone; otherwise, messages
	 * are wiped, the conversation is set to no longer be read-only, and the new set
	 * of messages is uploaded.
	 * </p>
	 * 
	 * @param xmlFile           Stream containing the XML file to be parsed
	 * @param tumblrUser        The Tumblr name of the user of the application
	 * @param tumblrId          The ID assigned to the user of the application by
	 *                          Tumblr
	 * @param restController    Controller used to update the database with
	 *                          conversations/messages
	 * @param isOverwriteConvos Indicates whether conversations should be
	 *                          overwritten, or left alone
	 * @throws XMLParsingException
	 */
	private static void readConversations(InputStream xmlFile, String tumblrUser, String tumblrId,
			TEVRestController restController) throws XMLParsingException {
		boolean isOverwriteConvos = restController.getMetadata().getOverwriteConvoData();
		
		if(isOverwriteConvos) {
			restController.deleteAllConvoMsgs();
			restController.deleteAllConversations();
		}
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLEventReader reader = factory.createXMLEventReader(xmlFile);

			Conversation conversation = null;
			List<ConversationMessage> messages = null;
			Participant participant = null;

			while (reader.hasNext()) {
				XMLEvent event = reader.nextEvent();

				if (event.isStartElement()) {
					StartElement se = event.asStartElement();

					if (se.getName().getLocalPart().equals("conversation")) {
						participant = getParticipantName(reader, tumblrUser);
						messages = getMessages(reader, tumblrId);

						conversation = new Conversation();
						conversation.setParticipant(participant.getName());
						conversation.setParticipantAvatarUrl(participant.getAvatarUrl());
						conversation.setNumMessages(messages.size());

						boolean isSendConvoToServer = true;

						if (isOverwriteConvos) {
							conversation = restController.createConversation(conversation);
							isSendConvoToServer = true;
						} else {
							try {
								Conversation convoOnServer = restController
										.getConversationByParticipant(participant.getName());
								if (messages.size() > convoOnServer.getNumMessages()) {
									convoOnServer.setHideConversation(false);
									convoOnServer = restController.updateConversation(convoOnServer.getId(),
											convoOnServer);
									List<ConversationMessage> msgsForConv = restController
											.getConvoMsgByConvoID(convoOnServer.getId());
									for (ConversationMessage msg : msgsForConv) {
										restController.deleteConversationMessage(msg.getId());
									}
									isSendConvoToServer = true;
									conversation.setId(convoOnServer.getId());
								} else {
									isSendConvoToServer = false;
								}
							} catch (ResourceNotFoundException e) {
								conversation = restController.createConversation(conversation);
								isSendConvoToServer = true;
							}
						}

						if (isSendConvoToServer) {
							uploadMessagesForConvo(restController, messages, conversation.getId());
						}
					}
				}
			}
		} catch (XMLStreamException e) {
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
	private static void uploadMessagesForConvo(TEVRestController restController, List<ConversationMessage> messages,
			Long convoID) {
		for (ConversationMessage msg : messages) {
			msg.setConversationId(convoID);
			restController.createConvoMessage(msg);
		}
	}

	/**
	 * Gets a list of messages (stops when the end of the conversation is reached).
	 * The two attributes are read (via <code>readMessageAttributes()</code>,
	 * followed by the text of the element.
	 * 
	 * @param reader       Stream containing the XML document being read
	 * @param tumblrUserID Tumblr ID of the TEV user
	 * @return list of messages
	 * @throws XMLStreamException
	 */
	private static List<ConversationMessage> getMessages(XMLEventReader reader, String tumblrUserID)
			throws XMLStreamException {
		List<ConversationMessage> messages = new ArrayList<ConversationMessage>();

		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();

			if (event.isStartElement()) {
				StartElement se = event.asStartElement();

				if (se.getName().getLocalPart().equals("message")) {
					ConversationMessage currentMessage = new ConversationMessage();
					readMessageAttributes(se, currentMessage, tumblrUserID);
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
					return messages;
				}
			}
		}

		throw new XMLStreamException(END_OF_FILE_MESSAGE);
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

		throw new XMLStreamException(END_OF_FILE_MESSAGE);
	}

	/**
	 * Helper method to read in a message's attributes.
	 * 
	 * @param se             The <code>StartElement</code> object currently being
	 *                       processed
	 * @param currentMessage The message to add the data to
	 * @param tumblrUserID   The ID of the main user
	 */
	private static void readMessageAttributes(StartElement se, ConversationMessage currentMessage,
			String tumblrUserID) {
		@SuppressWarnings("unchecked")
		Iterator<Attribute> atts = se.getAttributes();

		while (atts.hasNext()) {
			Attribute att = atts.next();
			String attName = att.getName().getLocalPart();

			switch (attName) {
			case "ts":
				currentMessage.setTimestamp(new Long(att.getValue()));
				break;
			case "participant":
				String participant = att.getValue();
				if (participant.equals(tumblrUserID)) {
					currentMessage.setReceived(false);
				} else {
					currentMessage.setReceived(true);
				}
				break;
			case "type":
				currentMessage.setType(att.getValue());
				break;
			}
		}
	}

	/**
	 * Helper function to get the participant name (and avatar URL) from a
	 * conversation. Each conversation contains a list of exactly two participants:
	 * the TEV user's name, and the <i>other</i> participant's name. Each
	 * "participant" element is read, and the one that is <i>not</i> that of the
	 * current Tumblr user is eventually returned.
	 * 
	 * @param reader     The reader from which to read the "participant" elements
	 * @param tumblrUser Tumblr name of the TEV user
	 * @return Helper <code>Participant</code> object, with the details of the
	 *         <i>other</i> (non-TEV-user) participant in the conversation
	 * @throws XMLStreamException
	 */
	private static Participant getParticipantName(XMLEventReader reader, String tumblrUser) throws XMLStreamException {
		String participantName = "";
		String participantAvatar = "";
		Participant participant = new Participant();

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
					if (!participantName.equals(tumblrUser)) {
						participant.setAvatarUrl(participantAvatar);
						participant.setName(fixName(participantName));
					}
				}
			} else if (event.isEndElement()) {
				EndElement ee = event.asEndElement();

				if (ee.getName().getLocalPart().equals("participants")) {
					return participant;
				}
			}
		}

		throw new XMLStreamException(END_OF_FILE_MESSAGE);
	}

	/**
	 * Helper function used to "fix" names for deactivated users. When a user has
	 * been deactivated, Tumblr returns the name as "username-deactivated" or
	 * "username-deactivatedyyyymmdd" or even "username-deact". This function just
	 * keeps the username, without the "-deactivated" or "-deactivatedyyyymmdd"
	 * part.
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

		throw new XMLStreamException(END_OF_FILE_MESSAGE);
	}

	/**
	 * Helper class (essentially a struct), used by the
	 * <code>getMainParticipant()</code> method for returning a few pieces of
	 * information. Since this is just an inner, helper class, the trouble wasn't
	 * taken to make it a proper bean with getters/setters; just using public member
	 * variables.
	 *
	 */
	private static class ConversationUsers {
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

}
