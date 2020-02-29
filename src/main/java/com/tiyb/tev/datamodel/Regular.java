package com.tiyb.tev.datamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Encapsulates the data needed for a Tumblr "Regular" style of post (just a normal text post)
 *
 * @author tiyb
 */
@Entity
@Table(name = "regular")
public class Regular implements Serializable, TEVCommonItems<Regular> {

    private static final long serialVersionUID = 1029059617501526454L;

    /**
     * ID of the post to which this 'regular' applies
     */
    @Id
    private String postId;

    /**
     * Title of the post
     */
    private String title;

    /**
     * Body of the post
     */
    @Lob
    @Column(name = "body", length = Post.LONG_FIELD_SIZE)
    private String body;

    /**
     * Helper method to update the data in this object with properties from another copy of the
     * object. ID ignored.
     *
     * @param newDetails Object from which to copy the properties
     */
    @Override
    public void updateItem(final Regular newDetails) {
        this.body = newDetails.body;
        // this.postId = newItem.postId;
        this.title = newDetails.title;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Regular [");
        if (postId != null) {
            builder.append("postId=");
            builder.append(postId);
            builder.append(", ");
        }
        if (title != null) {
            builder.append("title=");
            builder.append(title);
            builder.append(", ");
        }
        if (body != null) {
            builder.append("body=");
            builder.append(body);
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

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(final String body) {
        this.body = body;
    }

}
