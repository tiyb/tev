package com.tiyb.tev.xml;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Super-class used by all XML-reading classes of the application, to reduce
 * redundant code.
 * 
 * @author tiyb
 */
public class TEVXmlReader {
	
	protected static Logger logger = LoggerFactory.getLogger(TEVXmlReader.class);

	/**
	 * Value used for the exception encountered when XML parsing fails because of
	 * unexpected conditions (i.e. a closing tag was expected but never arrived).
	 */
	protected static final String END_OF_FILE_ERROR = "Premature end of file"; 

	/**
	 * Raised when unexpectedly getting to the end of an XML file
	 */
	protected static final String UNEXPECTED_EOF_LOG = "Unexpected end of file reached in method {}"; 
	
	/**
	 * Used when logging XML parsing errors
	 */
	protected static final String XML_PARSER_ERROR = "Error parsing XML file: "; 

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
	 * of &gt; and &lt; -- get un-escaped, which is the desired behaviour for this
	 * application.
	 * </p>
	 * 
	 * @param reader The event parser from which the text should be extracted.
	 * @return A simple <code>String</code> with the returned text
	 * @throws XMLStreamException
	 */
	protected static String readCharacters(XMLEventReader reader) throws XMLStreamException {
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

		logger.error(UNEXPECTED_EOF_LOG, "readCharacters"); 
		throw new XMLStreamException(END_OF_FILE_ERROR);
	}
}
