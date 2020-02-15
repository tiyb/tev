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

    /**
     * Unique ID of the hashtag
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * The hashtag's text
     */
    @NotBlank
    private String tag;

    /**
     * Number of times the hashtag occurs in the DB (for a given blog)
     */
    private Integer count = 0;

    /**
     * Blog for which this hashtag exists
     */
    private String blog;

    /**
     * Constructor to initialize a hashtag
     *
     * @param tag   The tag's text
     * @param count Number of times the tag exists for the given blog
     * @param blog  blog for which the hashtag is being created
     */
    public Hashtag(final String tag, final Integer count, final String blog) {
        this.tag = tag;
        this.count = count;
        this.blog = blog;
    }

    /**
     * Default constructor to initialize an empty hashtag
     */
    public Hashtag() {
        this.tag = StringUtils.EMPTY;
        this.count = 0;
        this.blog = StringUtils.EMPTY;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
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

    public void setId(final Long id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(final String tag) {
        this.tag = tag;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(final Integer count) {
        this.count = count;
    }

    public String getBlog() {
        return blog;
    }

    public void setBlog(final String blog) {
        this.blog = blog;
    }
}
