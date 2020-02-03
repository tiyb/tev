package com.tiyb.tev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

	public List<Post> findByTumblelogAndType(String blog, String postType);
	public List<Post> findByTumblelog(String tumblelog);
	Long deleteByTumblelog(String tumblelog);
}
