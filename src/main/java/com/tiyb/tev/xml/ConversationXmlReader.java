package com.tiyb.tev.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.tiyb.tev.controller.TEVRestController;
import com.tiyb.tev.datamodel.Conversation;
import com.tiyb.tev.datamodel.ConversationMessage;
import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.exception.XMLParsingException;

/**
 * This class is responsible for reading in an XML export from Tumblr containing
 * conversations. The XML generated from Tumblr for conversations is better
 * structured than that for posts, so less strange logic is required here than
 * is required in {@link com.tiyb.tev.xml.BlogXmlReader}.} The only interesting
 * caveat is that message[@type=IMAGE] has a child element, for the photo's URL,
 * whereas other messages just have the text of the message in the "message"
 * element itself.
 * 
 * @author tiyb
 * @apiviz.landmark
 * @apiviz.uses com.tiyb.tev.datamodel.helpers.TEVConversationSuperClass
 * @apiviz.uses javax.xml.stream.XMLEventReader
 *
 */
public class ConversationXmlReader {

	/**
	 * Error message used when the end of a file is unexpectedly reached
	 */
	private static final String END_OF_FILE_MESSAGE = "Premature end of file";

	/**
	 * Main method; simply gets the metadata (for the name of the Tumblr user), and
	 * then calls <code>readConversations()</code> to do the work.
	 * 
	 * @param xmlFile        Stream containing file to be parsed
	 * @param restController Used for updating the database as each
	 *                       conversation/message is parsed
	 */
	public static void parseDocument(InputStream xmlFile, TEVRestController restController) {
		Metadata md = restController.getMetadata();
		readConversations(xmlFile, md.getMainTumblrUser(), restController);
	}

	/**
	 * Reads in the XML file, conversation by conversation. Whenever a new
	 * conversation element is reached, a new object is created, the participant is
	 * discovered (via <code>getParticipantName()</code>, and then
	 * <code>getMessages()</code> is called to get all of the messages. The count of
	 * that list is added to the conversation object.
	 * 
	 * @param xmlFile        Stream containing the XML file to be parsed
	 * @param tumblrUser     The Tumblr name of the user of the application
	 * @param restController Controller used to update the database with
	 *                       conversations/messages
	 * @throws XMLParsingException
	 */
	private static void readConversations(InputStream xmlFile, String tumblrUser, TEVRestController restController)
			throws XMLParsingException {
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
						messages = getMessages(reader, tumblrUser);

						conversation = new Conversation();
						conversation.setParticipant(participant.getName());
						conversation.setParticipantAvatarUrl(participant.getAvatarUrl());
						conversation.setNumMessages(messages.size());

						conversation = restController.createConversation(conversation);
						for (ConversationMessage msg : messages) {
							msg.setConversationId(conversation.getId());
							restController.createConvoMessage(msg);
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
	 * Gets a list of messages (stops when the end of the conversation is reached).
	 * The two attributes are read (via <code>readMessageAttributes()</code>,
	 * followed by the text of the element.
	 * 
	 * @param reader     Stream containing the XML document being read
	 * @param tumblrUser Tumblr name of the TEV user
	 * @return list of messages
	 * @throws XMLStreamException
	 */
	private static List<ConversationMessage> getMessages(XMLEventReader reader, String tumblrUser)
			throws XMLStreamException {
		List<ConversationMessage> messages = new ArrayList<ConversationMessage>();

		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();

			if (event.isStartElement()) {
				StartElement se = event.asStartElement();

				if (se.getName().getLocalPart().equals("message")) {
					ConversationMessage currentMessage = new ConversationMessage();
					readMessageAttributes(se, currentMessage, tumblrUser);
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
	 * @param tumblrUser
	 */
	private static void readMessageAttributes(StartElement se, ConversationMessage currentMessage, String tumblrUser) {
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
				if (participant.equals(tumblrUser)) {
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
	 * "username-deactivatedyyyymmdd". This function just keeps the username,
	 * without the "-deactivated" or "-deactivatedyyyymmdd" part.
	 * 
	 * @param participantName "raw" username
	 * @return Username without the postfix (if any)
	 */
	private static String fixName(String participantName) {
		int postfix = participantName.indexOf("-deactivated");
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

}
