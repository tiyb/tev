package com.tiyb.tev.datamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * Encapsulates the data for any Tumblr post. This entity has the main post details, and other
 * entities (Answer, Link, Photo, Regular, and Video) have the posts' content. TEV uses the post
 * type quite a bit, so constants are defined here that are used in application logic.
 * </p>
 *
 * <p>
 * Unless otherwise specified, all members are read in from the Tumblr export XML, rather than being
 * set by TEV itself.
 * </p>
 *
 * @author tiyb
 */
@Entity
@Table(name = "post")
public class Post implements Serializable {

    /**
     * The 'regular' post type; used in logic in the application, as well as for XML generation
     */
    public static final String POST_TYPE_REGULAR = "regular";

    /**
     * The 'link' post type; used in logic in the application, as well as for XML generation
     */
    public static final String POST_TYPE_LINK = "link";

    /**
     * The 'answer' post type; used in logic in the application, as well as for XML generation
     */
    public static final String POST_TYPE_ANSWER = "answer";

    /**
     * The 'photo' post type; used in logic in the application, as well as for XML generation
     */
    public static final String POST_TYPE_PHOTO = "photo";

    /**
     * The 'video' post type; used in logic in the application, as well as for XML generation
     */
    public static final String POST_TYPE_VIDEO = "video";

    /**
     * Used in the {@link javax.persistence.Column Column} annotation for any fields which store a
     * large amount of text.
     */
    public static final int LONG_FIELD_SIZE = 50000;

    private static final long serialVersionUID = -7988281852593439595L;

    /**
     * Unique ID of the post, as specified by Tumblr. (TEV doesn't have post creation capabilities,
     * so Tumblr's ID is always used.)
     */
    @Id
    private String id;

    /**
     * Short URL of the post in Tumblr
     */
    private String url;

    /**
     * Long URL of the post in Tumblr, including the 'slug'
     */
    @Lob
    @Column(name = "url_with_slug", length = LONG_FIELD_SIZE)
    private String urlWithSlug;

    /**
     * GMT date when the post was posted to Tumblr
     */
    private String dateGmt;

    /**
     * Date the post was posted to Tumblr
     */
    private String date;

    /**
     * Unix-style timestamp the psot was posted to Tumblr
     */
    private Long unixtimestamp;

    /**
     * Unique 'reblog key' for this post. Tumblr uses these keys to identify all reposts of a given
     * post within the site.
     */
    private String reblogKey;

    /**
     * 'Slug' for the post; typically the first few characters of the post's content.
     */
    private String slug;

    /**
     * Whether this post is a re-blog of another post (true) or a unique post (false).
     */
    private Boolean isReblog;

    /**
     * Name of the blog for which this post is posted.
     */
    private String tumblelog;

    /**
     * Type of post; one of {@link #POST_TYPE_ANSWER}, {@link Post#POST_TYPE_LINK},
     * {@link #POST_TYPE_REGULAR}, {@link #POST_TYPE_PHOTO}, or {@link #POST_TYPE_VIDEO}.
     */
    private String type;

    /**
     * Whether this post has been read/viewed in TEV. Set by TEV, rather than being read from the
     * XML.
     */
    private Boolean isRead = false;

    /**
     * Hashtags associated with the post.
     */
    @Lob
    @Column(name = "tags", length = LONG_FIELD_SIZE)
    private String tags = StringUtils.EMPTY;

    /**
     * Whether this post has been marked a 'favourite' in TEV. Set by TEV, rather than being read
     * from the XML.
     */
    private Boolean isFavourite = false;

    /**
     * State of the post (published, draft, ...).
     */
    private String state;

    /**
     * Height (used only for image posts, and even then not always).
     */
    private Integer height;

    /**
     * Width (used only for image posts, and even then not always).
     */
    private Integer width;

    /**
     * Helper method for updating this object with properties from another copy of the object. ID
     * and type are ignored, they're left as-is.
     *
     * @param newDataObject Object from which to copy the properties.
     */
    public void updateData(final Post newDataObject) {
        // this.id = newDataObject.id;
        this.url = newDataObject.url;
        this.urlWithSlug = newDataObject.urlWithSlug;
        this.dateGmt = newDataObject.dateGmt;
        this.date = newDataObject.date;
        this.unixtimestamp = newDataObject.unixtimestamp;
        this.reblogKey = newDataObject.reblogKey;
        this.slug = newDataObject.slug;
        this.isReblog = newDataObject.isReblog;
        this.tumblelog = newDataObject.tumblelog;
        // this.type = newDataObject.type;
        this.isRead = newDataObject.isRead;
        this.tags = newDataObject.tags;
        this.isFavourite = newDataObject.isFavourite;
        this.state = newDataObject.state;
        this.height = newDataObject.height;
        this.width = newDataObject.width;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
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
            builder.append(", ");
        }
        if (height != null) {
            builder.append("height=");
            builder.append(height);
            builder.append(", ");
        }
        if (width != null) {
            builder.append("width=");
            builder.append(width);
        }
        builder.append("]");
        return builder.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getUrlWithSlug() {
        return urlWithSlug;
    }

    public void setUrlWithSlug(final String urlWithSlug) {
        this.urlWithSlug = urlWithSlug;
    }

    public String getDateGmt() {
        return dateGmt;
    }

    public void setDateGmt(final String dateGmt) {
        this.dateGmt = dateGmt;
    }

    public String getDate() {
        return date;
    }

    public void setDate(final String date) {
        this.date = date;
    }

    public Long getUnixtimestamp() {
        return unixtimestamp;
    }

    public void setUnixtimestamp(final Long unixtimestamp) {
        this.unixtimestamp = unixtimestamp;
    }

    public String getReblogKey() {
        return reblogKey;
    }

    public void setReblogKey(final String reblogKey) {
        this.reblogKey = reblogKey;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(final String slug) {
        this.slug = slug;
    }

    public Boolean getIsReblog() {
        return isReblog;
    }

    public void setIsReblog(final Boolean isReblog) {
        this.isReblog = isReblog;
    }

    public String getTumblelog() {
        return tumblelog;
    }

    public void setTumblelog(final String tumblelog) {
        this.tumblelog = tumblelog;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(final Boolean isRead) {
        this.isRead = isRead;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(final String tags) {
        this.tags = tags;
    }

    public Boolean getIsFavourite() {
        return isFavourite;
    }

    public void setIsFavourite(final Boolean isFavourite) {
        this.isFavourite = isFavourite;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(final Integer height) {
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(final Integer width) {
        this.width = width;
    }
}
