package com.tiyb.tev.controller.helper;

import java.io.Serializable;

/**
 * Simple bean for information needed by the photo viewing page.
 * 
 * @author tiyb
 *
 */
public class PhotoViewerAbstractor implements Serializable {
    private static final long serialVersionUID = -5141716088025748413L;

    /**
     * Image name to use for the photo
     */
    private String photoName;

    /**
     * Original URL of the photo from Tumblr
     */
    private String photoOriginalUrl;

    /**
     * Constructor to pre-load object
     * 
     * @param photoName        Name of the photo on the TEV file system
     * @param photoOriginalUrl Original URL for the photo
     */
    public PhotoViewerAbstractor(final String photoName, final String photoOriginalUrl) {
        this.photoName = photoName;
        this.photoOriginalUrl = photoOriginalUrl;
    }

    /**
     * Default constructor
     */
    public PhotoViewerAbstractor() {
        this.photoName = "";
        this.photoOriginalUrl = "";
    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(final String photoName) {
        this.photoName = photoName;
    }

    public String getPhotoOriginalUrl() {
        return photoOriginalUrl;
    }

    public void setPhotoOriginalUrl(final String photoOriginalUrl) {
        this.photoOriginalUrl = photoOriginalUrl;
    }
}
