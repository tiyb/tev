package com.tiyb.tev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.Link;

/**
 * Repo for Links
 *
 * @author tiyb
 *
 */
@Repository
public interface LinkRepository extends JpaRepository<Link, String> {

}
