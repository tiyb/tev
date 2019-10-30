package com.tiyb.tev.repository.staging;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.staging.StagingPost;

@Repository
public interface StagingPostRepository extends JpaRepository<StagingPost, Long> {

}
