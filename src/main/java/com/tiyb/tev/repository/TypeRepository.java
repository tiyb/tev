package com.tiyb.tev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.Type;

@Repository
public interface TypeRepository extends JpaRepository<Type, Long> {

}
