package com.tiyb.tev.controller;

import java.util.List;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiyb.tev.datamodel.Post;

/**
 * <p>
 * A non-advertised REST controller with some administration tools for working
 * with the database behind the scenes.
 * </p>
 * 
 * <p>
 * If TEV were ever to become anything more than just a local application,
 * proper security would be needed around these operations, but since that's not
 * the case the security is lax. None of these APIs are ever intended to be used
 * by the UI.
 * </p>
 * 
 * @author tiyb
 * @apiviz.landmark
 * @apiviz.uses org.springframework.jdbc.core.JdbcTemplate
 * @apiviz.uses com.tiyb.tev.controller.TEVPostRestController
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
	private TEVPostRestController postController;

	/**
	 * Used to compact the database upon shutdown. This causes shutdown to take
	 * longer, but it's not very noticeable for an application of this size.
	 */
	@PreDestroy
	public void preDestroy() {
		try {
			jdbcTemplate.execute("SHUTDOWN COMPACT");
		} catch (DataAccessException e) {
			// do nothing
		}
	}

	/**
	 * GET request to mark all posts in the database as read
	 * 
	 * @return Success indicator
	 */
	@GetMapping("/posts/markAllRead")
	public String markAllPostsRead() {
		List<Post> posts = postController.getAllPosts();

		for (Post post : posts) {
			postController.markPostRead(post.getId());
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
		List<Post> posts = postController.getAllPosts();

		for (Post post : posts) {
			postController.markPostUnread(post.getId());
		}

		return "Success";
	}

}
