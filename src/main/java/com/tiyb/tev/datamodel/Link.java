package com.tiyb.tev.datamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

/**
 * Encapsulates the data needed for a Tumblr "Link" style of post
 *
 * @author tiyb
 */
@Entity
@Table(name = "link")
public class Link implements Serializable, TEVCommonItems<Link> {

    private static final long serialVersionUID = 2074528769160771080L;

    /**
     * ID of the post to which this Link points
     */
    @Id
    private String postId;

    /**
     * Text accompanying the link
     */
    @Lob
    @Column(name = "text", length = Post.LONG_FIELD_SIZE)
    private String text;

    /**
     * URL of the link
     */
    @NotBlank
    private String url;

    /**
     * Link description
     */
    @Lob
    @Column(name = "description", length = Post.LONG_FIELD_SIZE)
    private String description;

    /**
     * Helper method used to update this object's properties with another copy of the object. Post
     * ID ignored.
     *
     * @param newDetails Object from which to copy the properties
     */
    @Override
    public void updateItem(final Link newDetails) {
        this.description = newDetails.description;
        // this.postId = newDetails.postId;
        this.text = newDetails.text;
        this.url = newDetails.url;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Link [");
        if (postId != null) {
            builder.append("postId=");
            builder.append(postId);
            builder.append(", ");
        }
        if (text != null) {
            builder.append("text=");
            builder.append(text);
            builder.append(", ");
        }
        if (url != null) {
            builder.append("url=");
            builder.append(url);
            builder.append(", ");
        }
        if (description != null) {
            builder.append("description=");
            builder.append(description);
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public String getPostId() {
        return postId;
    }

    @Override
    public void setPostId(final String postId) {
        this.postId = postId;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

}
