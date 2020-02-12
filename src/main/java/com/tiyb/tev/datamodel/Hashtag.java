package com.tiyb.tev.datamodel;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;

/**
 * Entity for storing hashtags in the DB
 * 
 * @author tiyb
 */
@Entity
@Table(name = "hashtag")
public class Hashtag implements Serializable {

	private static final long serialVersionUID = 5295937621643057029L;

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Long id;
	@NotBlank
	private String tag;
	private Integer count = 0;
	private String blog;

	public Hashtag(String tag, Integer count, String blog) {
		super();
		this.tag = tag;
		this.count = count;
		this.blog = blog;
	}

	public Hashtag() {
		this.tag = StringUtils.EMPTY;
		this.count = 0;
		this.blog = StringUtils.EMPTY;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Hashtag [");
		if (id != null) {
			builder.append("id=");
			builder.append(id);
			builder.append(", ");
		}
		if (tag != null) {
			builder.append("tag=");
			builder.append(tag);
			builder.append(", ");
		}
		if (count != null) {
			builder.append("count=");
			builder.append(count);
			builder.append(", ");
		}
		if (blog != null) {
			builder.append("blog=");
			builder.append(blog);
		}
		builder.append("]");
		return builder.toString();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public String getBlog() {
		return blog;
	}

	public void setBlog(String blog) {
		this.blog = blog;
	}
}
