package com.tiyb.tev.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Class representing attempted usage of a "type" that doesn't exist in
 * Tumblr/TEV
 * 
 * @author tiyb
 * @apiviz.landmark
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class InvalidTypeException extends RuntimeException {

	private static final long serialVersionUID = 8177918614482686246L;

}
