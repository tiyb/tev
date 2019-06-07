package com.tiyb.tev.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiyb.tev.datamodel.Post;

/**
 * A non-advertised REST controller with some admin tools for working with the
 * database behind the scenes. If TEV were ever to become anything more than
 * just a local application, proper security would be needed around these
 * operations, but since that's not the case the security is lax. None of these
 * APIs are ever intended to be used by the UI.
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
	 * The REST controller used for working with posts
	 */
	@Autowired
	private TEVRestController restController;

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

	/**
	 * GET request to mark all posts in the database as read
	 * 
	 * @return Success indicator
	 */
	@GetMapping("/posts/markAllRead")
	public String markAllPostsRead() {
		List<Post> posts = restController.getAllPosts();

		for (Post post : posts) {
			restController.markPostRead(post.getId());
		}

		return "Success";
	}

	/**
	 * GET request to mark all posts in the database as unread
	 * 
	 * @return Success indicator
	 */
	@GetMapping("/posts/markAllUnread")
	public String markAllPostsUnread() {
		List<Post> posts = restController.getAllPosts();

		for (Post post : posts) {
			restController.markPostUnread(post.getId());
		}

		return "Success";
	}

}
