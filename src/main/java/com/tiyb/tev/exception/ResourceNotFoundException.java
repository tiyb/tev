package com.tiyb.tev.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a resource (<i>any</i> resource) is not found.
 *
 * @author tiyb
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 4682807982851623120L;

    /**
     * Name of the resource that couldn't be found
     */
    private final String resourceName;

    /**
     * Field on the resource that couldn't be found
     */
    private final String fieldName;

    /**
     * Value of the field that couldn't be found
     */
    private final Object fieldValue;

    /**
     * Constructor for the exception
     *
     * @param resourceName Resource name
     * @param fieldName    Resource field name
     * @param fieldValue   resource field value
     */
    public ResourceNotFoundException(final String resourceName, final String fieldName, final Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
