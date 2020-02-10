package com.tiyb.tev.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Tests for staging posts, ensuring that they are separated by blog
 * 
 * @author tiyb
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("nls")
public class TevStagingMultiBlog {

	@Autowired
	private TEVStagingController controller;

	private static final String BLOG1_NAME = "blog1";
	private static final String BLOG2_NAME = "blog2";

	/**
	 * Clean out all staged posts before each test
	 */
	@Before
	public void cleanStagedPosts() {
		controller.deleteAllStagedPostsForBlog(BLOG1_NAME);
		controller.deleteAllStagedPostsForBlog(BLOG2_NAME);
	}

	/**
	 * Creation of staged posts across multiple blogs
	 */
	@Test
	public void multiBlogStagingPosts() {
		controller.createStagedPostForBlog(BLOG1_NAME, 1L);
		controller.createStagedPostForBlog(BLOG2_NAME, 2L);
		controller.createStagedPostForBlog(BLOG2_NAME, 3L);

		List<Long> posts = controller.getAllPostsForBlog(BLOG1_NAME);
		assertThat(posts).isNotNull();
		assertThat(posts.size()).isEqualTo(1);
		assertThat(posts.get(0)).isEqualTo(1L);
		posts = controller.getAllPostsForBlog(BLOG2_NAME);
		assertThat(posts).isNotNull();
		assertThat(posts.size()).isEqualTo(2);
		assertThat(posts.get(0)).isEqualTo(2L);
		assertThat(posts.get(1)).isEqualTo(3L);
	}

	/**
	 * Deletion of a staged post doesn't impact others
	 */
	@Test
	public void testDeletingAStagedost() {
		controller.createStagedPostForBlog(BLOG1_NAME, 1L);
		controller.createStagedPostForBlog(BLOG2_NAME, 2L);
		controller.createStagedPostForBlog(BLOG2_NAME, 3L);

		controller.deleteStagedPostForBlog(BLOG2_NAME, 2L);

		List<Long> posts = controller.getAllPostsForBlog(BLOG1_NAME);
		assertThat(posts).isNotNull();
		assertThat(posts.size()).isEqualTo(1);
		assertThat(posts.get(0)).isEqualTo(1L);
		posts = controller.getAllPostsForBlog(BLOG2_NAME);
		assertThat(posts).isNotNull();
		assertThat(posts.size()).isEqualTo(1);
		assertThat(posts.get(0)).isEqualTo(3L);
	}

	/**
	 * Deleting all staged posts for a blog doesn't impact other blogs
	 */
	@Test
	public void testDeletingAllStagedPostsFromABlog() {
		controller.createStagedPostForBlog(BLOG1_NAME, 1L);
		controller.createStagedPostForBlog(BLOG2_NAME, 2L);
		controller.createStagedPostForBlog(BLOG2_NAME, 3L);

		controller.deleteAllStagedPostsForBlog(BLOG2_NAME);

		List<Long> posts = controller.getAllPostsForBlog(BLOG1_NAME);
		assertThat(posts).isNotNull();
		assertThat(posts.size()).isEqualTo(1);
		assertThat(posts.get(0)).isEqualTo(1L);
		posts = controller.getAllPostsForBlog(BLOG2_NAME);
		assertThat(posts).isNotNull();
		assertThat(posts.size()).isEqualTo(0);
	}

}
