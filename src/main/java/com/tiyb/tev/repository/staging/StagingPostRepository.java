package com.tiyb.tev.repository.staging;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.staging.StagingPost;

/**
 * Repo for working with staged posts
 *
 * @author tiyb
 *
 */
@Repository
public interface StagingPostRepository extends JpaRepository<StagingPost, Long> {

    /**
     * Get all staged posts for a given blog
     *
     * @param blog Blof for which to return staged posts
     * @return List of staged posts
     */
    List<StagingPost> findByBlog(String blog);

    /**
     * Delete all staged posts for a given blog
     *
     * @param blog Blog for which to delete posts
     * @return Return code
     */
    Long deleteByBlog(String blog);

}
