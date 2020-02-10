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
public class Video implements Serializable {

	private static final long serialVersionUID = -1845632885836778430L;

	@Id
	private Long postId;
	private String contentType;
	private String extension;
	private Integer width;
	private Integer height;
	private Integer duration;
	private String revision;
	@Lob
	@Column(name="video_caption", length=50000)
	private String videoCaption;
	
	public void updateData(Video newDataObject) {
		this.contentType = newDataObject.contentType;
		this.duration = newDataObject.duration;
		this.extension = newDataObject.extension;
		this.height = newDataObject.height;
		//this.postId = newDataObject.postId;
		this.revision = newDataObject.revision;
		this.videoCaption = newDataObject.videoCaption;
		this.width = newDataObject.width;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
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

	public Long getPostId() {
		return postId;
	}

	public void setPostId(Long postId) {
		this.postId = postId;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getVideoCaption() {
		return videoCaption;
	}

	public void setVideoCaption(String videoCaption) {
		this.videoCaption = videoCaption;
	}
}
