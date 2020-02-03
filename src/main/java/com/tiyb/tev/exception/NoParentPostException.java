package com.tiyb.tev.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception raised when a sub-type is being inserted into the system (e.g.
 * Answer, Link, ...), but no parent post actually exists for it.
 * 
 * @author tiyb
 *
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class NoParentPostException extends RuntimeException {

	private static final long serialVersionUID = -727659149992893280L;

}
