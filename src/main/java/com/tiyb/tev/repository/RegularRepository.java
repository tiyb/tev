package com.tiyb.tev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.Regular;

@Repository
public interface RegularRepository extends JpaRepository<Regular, Long> {

}
