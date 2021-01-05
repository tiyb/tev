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
    private String postId;

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
     * First "video player" tag for the video
     */
    @Lob
    @Column(name = "video_player", length = Post.LONG_FIELD_SIZE)
    private String videoPlayer;

    /**
     * Video player for max width of 500
     */
    @Lob
    @Column(name = "video_player_500", length = Post.LONG_FIELD_SIZE)
    private String videoPlayer500;

    /**
     * Video player for max width of 250
     */
    @Lob
    @Column(name = "video_player_250", length = Post.LONG_FIELD_SIZE)
    private String videoPlayer250;

    /**
     * Constructor to pre-load object
     * 
     * @param postId         ID of parent post
     * @param contentType    Content Type of vid
     * @param extension      Extension of vid file
     * @param width          Width of vid
     * @param height         Height of vid
     * @param duration       Duration of vid
     * @param revision       Revision
     * @param videoCaption   Caption for vid post
     * @param videoPlayer    Vid player content
     * @param videoPlayer500 500 size vid player content
     * @param videoPlayer250 250 size vid player content
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Video(final String postId, final String contentType, final String extension, final Integer width,
            final Integer height, final Integer duration, final String revision, final String videoCaption,
            final String videoPlayer, final String videoPlayer500, final String videoPlayer250) {
            this.postId = postId;
            this.contentType = contentType;
            this.extension = extension;
            this.width = width;
            this.height = height;
            this.duration = duration;
            this.revision = revision;
            this.videoCaption = videoCaption;
            this.videoPlayer = videoPlayer;
            this.videoPlayer500 = videoPlayer500;
            this.videoPlayer250 = videoPlayer250;
    }

    /**
     * Default/empty constructor
     */
    public Video() {

    }

    /**
     * Helper function to update the object's properties with properties from
     * another copy of the object. Post ID ignored.
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
        builder.append("Video [postId=");
        builder.append(postId);
        builder.append(", contentType=");
        builder.append(contentType);
        builder.append(", extension=");
        builder.append(extension);
        builder.append(", width=");
        builder.append(width);
        builder.append(", height=");
        builder.append(height);
        builder.append(", duration=");
        builder.append(duration);
        builder.append(", revision=");
        builder.append(revision);
        builder.append(", videoCaption=");
        builder.append(videoCaption);
        builder.append(", videoPlayer=");
        builder.append(videoPlayer);
        builder.append(", videoPlayer500=");
        builder.append(videoPlayer500);
        builder.append(", videoPlayer250=");
        builder.append(videoPlayer250);
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

    public String getVideoPlayer() {
        return videoPlayer;
    }

    public void setVideoPlayer(final String videoPlayer) {
        this.videoPlayer = videoPlayer;
    }

    public String getVideoPlayer500() {
        return videoPlayer500;
    }

    public void setVideoPlayer500(final String videoPlayer500) {
        this.videoPlayer500 = videoPlayer500;
    }

    public String getVideoPlayer250() {
        return videoPlayer250;
    }

    public void setVideoPlayer250(final String videoPlayer250) {
        this.videoPlayer250 = videoPlayer250;
    }

}
