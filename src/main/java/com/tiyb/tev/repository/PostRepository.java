package com.tiyb.tev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.Post;

/**
 * Repo for Posts
 *
 * @author tiyb
 *
 */
@Repository
public interface PostRepository extends JpaRepository<Post, String> {

    /**
     * Get all posts for a given blog, for a particular type
     *
     * @param blog     Blog to search
     * @param postType Post type to retrieve
     * @return List of posts
     */
    public List<Post> findByTumblelogAndType(String blog, String postType);

    /**
     * Get all posts for a given blog
     *
     * @param tumblelog Blog to search for
     * @return List of posts
     */
    public List<Post> findByTumblelog(String tumblelog);

    /**
     * Delete all posts for a given Blog
     *
     * @param tumblelog Blog for which to delete posts
     * @return Return code
     */
    Long deleteByTumblelog(String tumblelog);
}
