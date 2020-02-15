package com.tiyb.tev.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Raised in cases where there is no MD object in the DB
 *
 * @author tiyb
 *
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NoMetadataFoundException extends RuntimeException {

    private static final long serialVersionUID = -3075562980853798062L;

}
