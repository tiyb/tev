package com.tiyb.tev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.Hashtag;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

	public Hashtag findByTag(String tag);
}
