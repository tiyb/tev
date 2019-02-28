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
 * @apiviz.landmark
 *
 */
@Entity
@Table(name = "photo")
public class Photo implements Serializable {

	private static final long serialVersionUID = 454344897567310660L;
	
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Long id;
	private Long postId;
	@Lob
	@Column(name="caption", length=50000)
	private String caption;
	private String photoLinkUrl;
	@Column(name="photo_offset")
	private String offset;
	private Integer width;
	private Integer height;
	private String url1280;
	private String url500;
	private String url400;
	private String url250;
	private String url100;
	private String url75;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
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

	public Long getPostId() {
		return postId;
	}

	public void setPostId(Long postId) {
		this.postId = postId;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getPhotoLinkUrl() {
		return photoLinkUrl;
	}

	public void setPhotoLinkUrl(String photoLinkUrl) {
		this.photoLinkUrl = photoLinkUrl;
	}

	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
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

	public String getUrl1280() {
		return url1280;
	}

	public void setUrl1280(String url1280) {
		this.url1280 = url1280;
	}

	public String getUrl500() {
		return url500;
	}

	public void setUrl500(String url500) {
		this.url500 = url500;
	}

	public String getUrl400() {
		return url400;
	}

	public void setUrl400(String url400) {
		this.url400 = url400;
	}

	public String getUrl250() {
		return url250;
	}

	public void setUrl250(String url250) {
		this.url250 = url250;
	}

	public String getUrl100() {
		return url100;
	}

	public void setUrl100(String url100) {
		this.url100 = url100;
	}

	public String getUrl75() {
		return url75;
	}

	public void setUrl75(String url75) {
		this.url75 = url75;
	}
}
