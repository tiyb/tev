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
 * @apiviz.landmark
 *
 */
@Entity
@Table(name = "link")
public class Link implements Serializable {

	private static final long serialVersionUID = 2074528769160771080L;

	@Id
	private Long postId;
	@Lob
	@Column(name="text", length=50000)
	private String text;
	@NotBlank
	private String url;
	@Lob
	@Column(name="description", length=50000)
	private String description;
	
	public void updateData(Link newDataObject) {
		this.description = newDataObject.description;
		//this.postId = newDataObject.postId;
		this.text = newDataObject.text;
		this.url = newDataObject.url;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
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

	public Long getPostId() {
		return postId;
	}

	public void setPostId(Long postId) {
		this.postId = postId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
