package com.tiyb.tev.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception raised when an XML document can't be parsed successfully.
 * 
 * @author tiyb
 * @apiviz.landmark
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class XMLParsingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

}
