package com.tiyb.tev.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception returned when a user attempts to download staged posts, but there
 * are no staged posts to download.
 * 
 * @author tiyb
 *
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class NoStagedPostsException extends RuntimeException {

	private static final long serialVersionUID = -8794513681732891894L;

}
