package com.tiyb.tev.datamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Encapsulates the data needed for a Tumblr "Regular" style of post (just a
 * normal text post)
 * 
 * @author tiyb
 */
@Entity
@Table(name = "regular")
public class Regular implements Serializable {

	private static final long serialVersionUID = 1029059617501526454L;

	@Id
	private Long postId;
	private String title;
	@Lob
	@Column(name="body", length=50000)
	private String body;
	
	public void updateData(Regular newDataObject) {
		this.body = newDataObject.body;
		//this.postId = newDataObject.postId;
		this.title = newDataObject.title;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
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

	public Long getPostId() {
		return postId;
	}

	public void setPostId(Long postId) {
		this.postId = postId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

}
