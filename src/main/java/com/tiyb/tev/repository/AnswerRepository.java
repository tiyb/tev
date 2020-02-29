package com.tiyb.tev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.Answer;

/**
 * Repo for Answers
 *
 * @author tiyb
 *
 */
@Repository
public interface AnswerRepository extends JpaRepository<Answer, String> {

}
