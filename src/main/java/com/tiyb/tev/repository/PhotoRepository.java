package com.tiyb.tev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.Photo;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

	List<Photo> findByPostIdOrderByOffset(Long postId);
}
