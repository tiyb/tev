package com.tiyb.tev.datamodel.staging;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Encapsulates the data for a Post that's being staged for later export
 *
 * @author tiyb
 *
 */
@Entity
@Table(name = "stagingposts")
public class StagingPost implements Serializable {

    private static final long serialVersionUID = -1260427401761905114L;

    /**
     * ID of the Post
     */
    @Id
    private Long id;

    /**
     * Blog for which this post is staged
     */
    private String blog;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getBlog() {
        return blog;
    }

    public void setBlog(final String blog) {
        this.blog = blog;
    }

}
