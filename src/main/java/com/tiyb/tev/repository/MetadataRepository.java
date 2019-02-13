package com.tiyb.tev.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tiyb.tev.datamodel.Metadata;

public interface MetadataRepository extends JpaRepository<Metadata, Integer> {

}
