package com.tiyb.tev.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BlogPostMismatchException extends RuntimeException {

	private static final long serialVersionUID = 3314432763369404426L;

}
