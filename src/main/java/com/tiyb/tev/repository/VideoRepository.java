package com.tiyb.tev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.Video;

/**
 * Repo for Videos
 *
 * @author tiyb
 *
 */
@Repository
public interface VideoRepository extends JpaRepository<Video, String> {

}
