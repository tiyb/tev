package com.tiyb.tev.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tiyb.tev.datamodel.Link;

public interface LinkRepository extends JpaRepository<Link, Long> {

}
