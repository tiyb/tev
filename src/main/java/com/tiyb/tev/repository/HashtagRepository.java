package com.tiyb.tev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.Hashtag;

/**
 * Repo for Hashtags
 *
 * @author tiyb
 *
 */
@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    /**
     * Get a given hashtag given the tag name and blog
     *
     * @param tag  Tag name
     * @param blog Blog to search
     * @return Hashtag (if any)
     */
    public Hashtag findByTagAndBlog(String tag, String blog);

    /**
     * Get all hashtags for a given blog
     *
     * @param blog Blog to search
     * @return List of hashtags
     */
    public List<Hashtag> findByBlog(String blog);

    /**
     * Delete all hashtags for a given blog
     *
     * @param blog Blog for which to delete the tags
     * @return Return code
     */
    Long deleteByBlog(String blog);
}
