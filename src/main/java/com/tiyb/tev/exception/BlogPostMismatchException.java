package com.tiyb.tev.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Raised by the API when a post is being updated, but the ID specified to the API and the ID in the
 * post don't match.
 *
 * @author tiyb
 *
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BlogPostMismatchException extends RuntimeException {

    private static final long serialVersionUID = 3314432763369404426L;

}
