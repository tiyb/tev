package com.tiyb.tev.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown for cases when a conversation is attempted to be updated but there is
 * a mismatch in blog name
 * 
 * @author tiyb
 *
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidConvoParentException extends RuntimeException {

	private static final long serialVersionUID = 7898125207454004903L;

}
