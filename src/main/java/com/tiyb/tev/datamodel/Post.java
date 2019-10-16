package com.tiyb.tev.datamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Encapsulates the data for any Tumblr post. This entity has the main post
 * details, and other entities (Answer, Link, Photo, Regular, and Video) have
 * the posts' content.
 * 
 * @author tiyb
 */
@Entity
@Table(name = "post")
public class Post implements Serializable {

	private static final long serialVersionUID = -7988281852593439595L;

	@Id
	private Long id;

	private String url;
	@Lob
	@Column(name="url_with_slug", length=10000)
	private String urlWithSlug;
	private String dateGmt;
	private String date;
	private Long unixtimestamp;
	private String reblogKey;
	private String slug;
	private Boolean isReblog;
	private String tumblelog;
	private String type;
	private Boolean isRead = false;
	@Lob
	@Column(name="tags", length=50000)
	private String tags = "";
	private Boolean isFavourite = false;
	private String state;
	
	public void updateData(Post newDataObject) {
		//this.id = newDataObject.id;
		this.url = newDataObject.url;
		this.urlWithSlug = newDataObject.urlWithSlug;
		this.dateGmt = newDataObject.dateGmt;
		this.date = newDataObject.date;
		this.unixtimestamp = newDataObject.unixtimestamp;
		this.reblogKey = newDataObject.reblogKey;
		this.slug = newDataObject.slug;
		this.isReblog = newDataObject.isReblog;
		this.tumblelog = newDataObject.tumblelog;
		//this.type = newDataObject.type;
		this.isRead = newDataObject.isRead;
		this.tags = newDataObject.tags;
		this.isFavourite = newDataObject.isFavourite;
		this.state = newDataObject.state;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Post [");
		if (id != null) {
			builder.append("id=");
			builder.append(id);
			builder.append(", ");
		}
		if (url != null) {
			builder.append("url=");
			builder.append(url);
			builder.append(", ");
		}
		if (urlWithSlug != null) {
			builder.append("urlWithSlug=");
			builder.append(urlWithSlug);
			builder.append(", ");
		}
		if (dateGmt != null) {
			builder.append("dateGmt=");
			builder.append(dateGmt);
			builder.append(", ");
		}
		if (date != null) {
			builder.append("date=");
			builder.append(date);
			builder.append(", ");
		}
		if (unixtimestamp != null) {
			builder.append("unixtimestamp=");
			builder.append(unixtimestamp);
			builder.append(", ");
		}
		if (reblogKey != null) {
			builder.append("reblogKey=");
			builder.append(reblogKey);
			builder.append(", ");
		}
		if (slug != null) {
			builder.append("slug=");
			builder.append(slug);
			builder.append(", ");
		}
		if (isReblog != null) {
			builder.append("isReblog=");
			builder.append(isReblog);
			builder.append(", ");
		}
		if (tumblelog != null) {
			builder.append("tumblelog=");
			builder.append(tumblelog);
			builder.append(", ");
		}
		if (type != null) {
			builder.append("type=");
			builder.append(type);
			builder.append(", ");
		}
		if (isRead != null) {
			builder.append("isRead=");
			builder.append(isRead);
			builder.append(", ");
		}
		if (tags != null) {
			builder.append("tags=");
			builder.append(tags);
			builder.append(", ");
		}
		if (isFavourite != null) {
			builder.append("isFavourite=");
			builder.append(isFavourite);
			builder.append(", ");
		}
		if (state != null) {
			builder.append("state=");
			builder.append(state);
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrlWithSlug() {
		return urlWithSlug;
	}

	public void setUrlWithSlug(String urlWithSlug) {
		this.urlWithSlug = urlWithSlug;
	}

	public String getDateGmt() {
		return dateGmt;
	}

	public void setDateGmt(String dateGmt) {
		this.dateGmt = dateGmt;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Long getUnixtimestamp() {
		return unixtimestamp;
	}

	public void setUnixtimestamp(Long unixtimestamp) {
		this.unixtimestamp = unixtimestamp;
	}

	public String getReblogKey() {
		return reblogKey;
	}

	public void setReblogKey(String reblogKey) {
		this.reblogKey = reblogKey;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public Boolean getIsReblog() {
		return isReblog;
	}

	public void setIsReblog(Boolean isReblog) {
		this.isReblog = isReblog;
	}

	public String getTumblelog() {
		return tumblelog;
	}

	public void setTumblelog(String tumblelog) {
		this.tumblelog = tumblelog;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getIsRead() {
		return isRead;
	}

	public void setIsRead(Boolean isRead) {
		this.isRead = isRead;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public Boolean getIsFavourite() {
		return isFavourite;
	}

	public void setIsFavourite(Boolean isFavourite) {
		this.isFavourite = isFavourite;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
