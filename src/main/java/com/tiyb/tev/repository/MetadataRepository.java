package com.tiyb.tev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.Metadata;

@Repository
public interface MetadataRepository extends JpaRepository<Metadata, Integer> {

}
