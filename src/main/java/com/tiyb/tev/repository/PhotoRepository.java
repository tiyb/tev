package com.tiyb.tev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tiyb.tev.datamodel.Photo;

public interface PhotoRepository extends JpaRepository<Photo, Long> {

	List<Photo> findByPostIdOrderByOffset(Long postId);
}
