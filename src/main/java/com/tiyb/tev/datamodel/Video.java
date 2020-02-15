package com.tiyb.tev.datamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Encapsulates the data needed for a Tumblr "Video" style of post
 *
 * @author tiyb
 */
@Entity
@Table(name = "video")
public class Video implements Serializable, TEVCommonItems<Video> {

    private static final long serialVersionUID = -1845632885836778430L;

    /**
     * ID of the post to which this video belongs
     */
    @Id
    private Long postId;

    /**
     * Video's MIME content type
     */
    private String contentType;

    /**
     * Video file extension
     */
    private String extension;

    /**
     * Width of the video
     */
    private Integer width;

    /**
     * Height of the video
     */
    private Integer height;

    /**
     * Duration of the video
     */
    private Integer duration;

    /**
     * Revision of the video
     */
    private String revision;

    /**
     * Caption of the video
     */
    @Lob
    @Column(name = "video_caption", length = Post.LONG_FIELD_SIZE)
    private String videoCaption;

    /**
     * Helper function to update the object's properties with properties from another copy of the
     * object. Post ID ignored.
     *
     * @param newDetails Object from which to copy the properties
     */
    @Override
    public void updateItem(final Video newDetails) {
        this.contentType = newDetails.contentType;
        this.duration = newDetails.duration;
        this.extension = newDetails.extension;
        this.height = newDetails.height;
        // this.postId = newDetails.postId;
        this.revision = newDetails.revision;
        this.videoCaption = newDetails.videoCaption;
        this.width = newDetails.width;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Video [");
        if (postId != null) {
            builder.append("postId=");
            builder.append(postId);
            builder.append(", ");
        }
        if (contentType != null) {
            builder.append("contentType=");
            builder.append(contentType);
            builder.append(", ");
        }
        if (extension != null) {
            builder.append("extension=");
            builder.append(extension);
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
        if (duration != null) {
            builder.append("duration=");
            builder.append(duration);
            builder.append(", ");
        }
        if (revision != null) {
            builder.append("revision=");
            builder.append(revision);
            builder.append(", ");
        }
        if (videoCaption != null) {
            builder.append("videoCaption=");
            builder.append(videoCaption);
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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(final String extension) {
        this.extension = extension;
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

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(final Integer duration) {
        this.duration = duration;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(final String revision) {
        this.revision = revision;
    }

    public String getVideoCaption() {
        return videoCaption;
    }

    public void setVideoCaption(final String videoCaption) {
        this.videoCaption = videoCaption;
    }

}
