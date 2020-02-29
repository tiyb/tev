package com.tiyb.tev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.Photo;

/**
 * Repo for Photos
 *
 * @author tiyb
 *
 */
@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    /**
     * Get all photos for a given post, in order of the offset attribute
     *
     * @param postId Post ID to search
     * @return 0 or more Photos
     */
    List<Photo> findByPostIdOrderByOffset(String postId);

    /**
     * Get all photos for a given post
     *
     * @param postId Post ID to search
     * @return 0 or more Photos
     */
    List<Photo> findByPostId(String postId);
}
