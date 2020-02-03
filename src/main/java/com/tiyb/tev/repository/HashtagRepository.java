package com.tiyb.tev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.Hashtag;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

	public Hashtag findByTagAndBlog(String tag, String blog);
	public List<Hashtag> findByBlog(String blog);
	Long deleteByBlog(String blog);
}
