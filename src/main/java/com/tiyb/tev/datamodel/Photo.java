package com.tiyb.tev.datamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Encapsulates the data needed for a Tumblr "Photo" style of post
 *
 * @author tiyb
 */
@Entity
@Table(name = "photo")
public class Photo implements Serializable, TEVCommonItems<Photo> {

    private static final long serialVersionUID = 454344897567310660L;

    /**
     * Unique ID of the photo
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * ID of the post to which this photo belongs
     */
    private Long postId;

    /**
     * Caption for the photo (copied across all photos in the DB)
     */
    @Lob
    @Column(name = "caption", length = Post.LONG_FIELD_SIZE)
    private String caption;

    /**
     * Photo's URL from Tumblr; in TEV, this field is typically ignored, if there is a 1280 URL
     */
    @Column(name = "photo_link_url", length = Post.LONG_FIELD_SIZE)
    private String photoLinkUrl;

    /**
     * "Offset" of this photo within the series; in other words, the order in which this photo
     * should appear.
     */
    @Column(name = "photo_offset")
    private String offset;

    /**
     * Width of the photo; not used, except in writing XML
     */
    private Integer width;

    /**
     * Height of the photo; not used, except in writing XML
     */
    private Integer height;

    /**
     * URL to the photo in the given size
     */
    private String url1280;

    /**
     * URL to the photo in the given size
     */
    private String url500;

    /**
     * URL to the photo in the given size
     */
    private String url400;

    /**
     * URL to the photo in the given size
     */
    private String url250;

    /**
     * URL to the photo in the given size
     */
    private String url100;

    /**
     * URL to the photo in the given size
     */
    private String url75;

    /**
     * Helper function to update properties of the object from another copy of the object. Ignores
     * ID and Post ID.
     *
     * @param newDetails Object from which to copy the properties.
     */
    @Override
    public void updateItem(final Photo newDetails) {
        this.caption = newDetails.caption;
        this.height = newDetails.height;
        // this.id = newDetails.id;
        this.offset = newDetails.offset;
        this.photoLinkUrl = newDetails.photoLinkUrl;
        // this.postId = newDetails.postId;
        this.url100 = newDetails.url100;
        this.url1280 = newDetails.url1280;
        this.url250 = newDetails.url250;
        this.url400 = newDetails.url400;
        this.url500 = newDetails.url500;
        this.url75 = newDetails.url75;
        this.width = newDetails.width;

    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Photo [");
        if (postId != null) {
            builder.append("postId=");
            builder.append(postId);
            builder.append(", ");
        }
        if (caption != null) {
            builder.append("caption=");
            builder.append(caption);
            builder.append(", ");
        }
        if (photoLinkUrl != null) {
            builder.append("photoLinkUrl=");
            builder.append(photoLinkUrl);
            builder.append(", ");
        }
        if (offset != null) {
            builder.append("offset=");
            builder.append(offset);
            builder.append(", ");
        }
        if (width != null) {
            builder.append("width=");
            builder.append(width);
            builder.append(", ");
        }
        if (height != null) {
            builder.append("height=");
            builder.append(height);
            builder.append(", ");
        }
        if (url1280 != null) {
            builder.append("url1280=");
            builder.append(url1280);
            builder.append(", ");
        }
        if (url500 != null) {
            builder.append("url500=");
            builder.append(url500);
            builder.append(", ");
        }
        if (url400 != null) {
            builder.append("url400=");
            builder.append(url400);
            builder.append(", ");
        }
        if (url250 != null) {
            builder.append("url250=");
            builder.append(url250);
            builder.append(", ");
        }
        if (url100 != null) {
            builder.append("url100=");
            builder.append(url100);
            builder.append(", ");
        }
        if (url75 != null) {
            builder.append("url75=");
            builder.append(url75);
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public Long getPostId() {
        return postId;
    }

    @Override
    public void setPostId(final Long postId) {
        this.postId = postId;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(final String caption) {
        this.caption = caption;
    }

    public String getPhotoLinkUrl() {
        return photoLinkUrl;
    }

    public void setPhotoLinkUrl(final String photoLinkUrl) {
        this.photoLinkUrl = photoLinkUrl;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(final String offset) {
        this.offset = offset;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(final Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(final Integer height) {
        this.height = height;
    }

    public String getUrl1280() {
        return url1280;
    }

    public void setUrl1280(final String url1280) {
        this.url1280 = url1280;
    }

    public String getUrl500() {
        return url500;
    }

    public void setUrl500(final String url500) {
        this.url500 = url500;
    }

    public String getUrl400() {
        return url400;
    }

    public void setUrl400(final String url400) {
        this.url400 = url400;
    }

    public String getUrl250() {
        return url250;
    }

    public void setUrl250(final String url250) {
        this.url250 = url250;
    }

    public String getUrl100() {
        return url100;
    }

    public void setUrl100(final String url100) {
        this.url100 = url100;
    }

    public String getUrl75() {
        return url75;
    }

    public void setUrl75(final String url75) {
        this.url75 = url75;
    }

    public Long getId() {
        return id;
    }

}
