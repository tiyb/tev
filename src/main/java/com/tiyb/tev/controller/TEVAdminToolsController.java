package com.tiyb.tev.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A non-advertised REST controller with some admin tools for working with the
 * database behind the scenes. If TEV were ever to become anything more than
 * just a local application, proper security would be needed around these
 * operations,
 * 
 * @author tiyb
 * @apiviz.landmark
 */
@RestController
@RequestMapping("/admintools")
public class TEVAdminToolsController {

	/**
	 * The JDBC template to be used for operations within the controller
	 */
	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * GET call to compress the database.
	 * 
	 * @return Boolean indicating whether the operation was successful or not.
	 */
	@GetMapping("/compressDB")
	public Boolean compressDB() {
		try {
			jdbcTemplate.execute("CHECKPOINT DEFRAG");
		} catch (DataAccessException e) {
			return false;
		}

		return true;
	}

}
