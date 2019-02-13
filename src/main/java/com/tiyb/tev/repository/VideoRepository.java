package com.tiyb.tev.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tiyb.tev.datamodel.Video;

public interface VideoRepository extends JpaRepository<Video, Long> {

}
