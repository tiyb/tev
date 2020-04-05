package com.tiyb.tev.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception raised when a hashtag is created for no blog, but it already exists in the system for some blog
 * @author tiyb
 *
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ExistingTagException extends RuntimeException {

    private static final long serialVersionUID = -2127625884602289044L;

}
