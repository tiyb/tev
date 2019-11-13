package com.tiyb.tev.xml.helper;

import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class used to wrap the StAX classes, for creating a more readable
 * version of the XML output by inserting newlines and indentation into the
 * output.
 * 
 * @author tiyb
 *
 */
public class PrettyPrintHandler implements InvocationHandler {

	private static Logger logger = LoggerFactory.getLogger(PrettyPrintHandler.class);

	/**
	 * The actual {@link javax.xml.stream.XMLStreamWriter XMLStreamWriter} object
	 * being wrapped
	 */
	private final XMLStreamWriter target;

	/**
	 * Current depth of nesting
	 */
	private int depth = 0;

	/**
	 * Used in the logic for determining depth and parentage
	 */
	private final Map<Integer, Boolean> hasChildElement = new HashMap<Integer, Boolean>();

	/**
	 * The sequence of characters used to insert indentation into the output.
	 * Currently using two spaces.
	 */
	private static final String INDENT_SEQUENCE = "  ";

	/**
	 * The string to be used for line separators. Using
	 * {@link java.lang.System#lineSeparator() System.lineSeparator()} for
	 * cross-platform use.
	 */
	private static final String LINEFEED_STRING = System.lineSeparator();

	/**
	 * Constructor, which is used to set the <code>target</code> member
	 * 
	 * @param target The {@link javax.xml.stream.XMLStreamWriter XMLStreamWriter}
	 *               object to be used/wrapped
	 */
	public PrettyPrintHandler(XMLStreamWriter target) {
		this.target = target;
	}

	/**
	 * Method which is allowing this code to wrap the underlying XML stream writer
	 * method. Catches each call to <code>writeStartElement()</code> or
	 * <code>writeEmptyElement()</code> or <code>writeEndElement()</code> to insert
	 * newlines and spaces as needed, before invoking the underlying object's
	 * method. (Underlying method is always invoked, regardless of whether pretty
	 * printing was added or not.)
	 * 
	 * @param proxy  The object being proxied
	 * @param method The method of the oject being proxied
	 * @param args   The arguments being passed to that method
	 * @return Always returns null
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String methodName = method.getName();
		logger.debug("XML event: " + methodName);

		if (methodName.equals("writeStartElement")) {
			if (depth > 0) {
				hasChildElement.put(depth - 1, true);
			}

			hasChildElement.put(depth, false);

			target.writeCharacters(LINEFEED_STRING);
			target.writeCharacters(repeat(depth, INDENT_SEQUENCE));
			++depth;
		} else if (methodName.equals("writeEndElement")) {
			--depth;
			if (hasChildElement.get(depth) == true) {
				target.writeCharacters(LINEFEED_STRING);
				target.writeCharacters(repeat(depth, INDENT_SEQUENCE));
			}
		} else if (methodName.equals("writeEmptyElement")) {
			if (depth > 0) {
				hasChildElement.put(depth - 1, true);
			}
			target.writeCharacters(LINEFEED_STRING);
			target.writeCharacters(repeat(depth, INDENT_SEQUENCE));
		}

		method.invoke(target, args);
		return null;
	}

	/**
	 * Helper function to repeat a given string the given number of times. For
	 * performance reasons, <b>streams</b> are used rather than string
	 * concatenation.
	 * 
	 * @param depth      The number of times to repeat the string.
	 * @param indentChar The string to be repeated
	 * @return The generated string
	 */
	private String repeat(int depth, String indentChar) {
		StringWriter generatedString = new StringWriter();

		while (depth-- > 0) {
			generatedString.append(indentChar);
		}

		return generatedString.toString();
	}

}
