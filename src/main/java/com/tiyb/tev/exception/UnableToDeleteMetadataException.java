package com.tiyb.tev.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception raised when a metadata object is attempted to be deleted, but business logic prevents
 * the action
 *
 * @author tiyb
 *
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class UnableToDeleteMetadataException extends RuntimeException {

    private static final long serialVersionUID = -8730373472517006090L;

}
