package com.tiyb.tev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.Metadata;

/**
 * Repo for Metadata
 *
 * @author tiyb
 *
 */
@Repository
public interface MetadataRepository extends JpaRepository<Metadata, Integer> {

    /**
     * Get the metadata object for a particular blog
     *
     * @param blog Blog for which to retrieve the MD
     * @return Metadata object
     */
    Metadata findByBlog(String blog);

    /**
     * Get all MD objects that are set as the default (should only ever be one)
     *
     * @return Metadata object
     */
    Metadata findByIsDefaultTrue();

}
