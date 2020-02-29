package com.tiyb.tev.datamodel;

/**
 * Common methods/properties to be implemented by all T post types
 *
 * @author tiyb
 *
 * @param <T> The generic type being implemented
 */
public interface TEVCommonItems<T> {

    /**
     * postId getter
     *
     * @return postId
     */
    public String getPostId();

    /**
     * postId setter
     *
     * @param id postId
     */
    public void setPostId(String id);

    /**
     * Method to be implemented by every T type to update its properties with another copy of the
     * object
     *
     * @param newDetails Other copy of the object with properties to copy in
     */
    public void updateItem(T newDetails);
}
