package com.tiyb.tev.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Raised when an invalid hashtag is used (typically when trying to delete a
 * non-existent one0
 * 
 * @author tiyb
 *
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTagException extends RuntimeException {

    private static final long serialVersionUID = 580945707195889316L;

}
