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

import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.exception.BlogPostMismatchException;

/**
 * Unit tests for Posts that are focused on use cases where there are multiple
 * blogs in the DB, not just one. Includes some tests of the Admin Controller
 * and some of the Post controller.
 * 
 * @author tiyb
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("nls")
public class TevMultiBlogPostUnitTests {

	@Autowired
	private TEVPostRestController restController;
	@Autowired
	private TEVMetadataRestController mdRestController;
	@Autowired
	private TEVAdminToolsController adminController;
	
	private static final String FIRST_BLOG = "blog1";
	private static final String SECOND_BLOG = "blog2";
	
	@Before
	public void cleanPosts() {
		restController.deleteAllPostsForBlog(FIRST_BLOG);
		restController.deleteAllPostsForBlog(SECOND_BLOG);
	}

	/**
	 * Simple test that posts can be created in different blogs, and not mixed up
	 * with each other.
	 */
	@Test
	public void twoPostsTwoBlogs() {
		Post p1 = new Post();
		p1.setId(1L);
		p1.setTumblelog(FIRST_BLOG);
		p1 = restController.createPostForBlog(FIRST_BLOG, p1);

		Post p2 = new Post();
		p2.setId(2L);
		p2.setTumblelog(SECOND_BLOG);
		p2 = restController.createPostForBlog(SECOND_BLOG, p2);

		List<Post> blogsForOne = restController.getAllPostsForBlog(FIRST_BLOG);
		assertThat(blogsForOne).isNotNull();
		assertThat(blogsForOne.size()).isEqualTo(1);
		assertThat(blogsForOne.get(0).getId()).isEqualTo(1L);

		List<Post> blogsForTwo = restController.getAllPostsForBlog(SECOND_BLOG);
		assertThat(blogsForTwo).isNotNull();
		assertThat(blogsForTwo.size()).isEqualTo(1);
		assertThat(blogsForTwo.get(0).getId()).isEqualTo(2L);
	}

	/**
	 * Verify that marking all posts "read" or "unread" for one blog doesn't impact
	 * the other. Two blogs created, two posts each, and both are tested back and
	 * forth.
	 */
	@Test
	public void markReadUnreadCrossBlogs() {
		// blog 1 posts
		Post p1 = new Post();
		p1.setId(1L);
		p1.setTumblelog(FIRST_BLOG);
		p1.setIsRead(false);
		p1 = restController.createPostForBlog(FIRST_BLOG, p1);
		Post p2 = new Post();
		p2.setId(2L);
		p2.setTumblelog(FIRST_BLOG);
		p2.setIsRead(false);
		p2 = restController.createPostForBlog(FIRST_BLOG, p2);

		// blog 2 posts
		Post p3 = new Post();
		p3.setId(3L);
		p3.setTumblelog(SECOND_BLOG);
		p3.setIsRead(false);
		p3 = restController.createPostForBlog(SECOND_BLOG, p3);

		// test blog 1 initial state
		List<Post> b1Posts = restController.getAllPostsForBlog(FIRST_BLOG);
		assertThat(b1Posts).isNotNull();
		assertThat(b1Posts.size()).isEqualTo(2);
		for (Post p : b1Posts) {
			assertThat(p.getIsRead()).isEqualTo(false);
		}

		// test blog 2 initial state
		List<Post> b2Posts = restController.getAllPostsForBlog(SECOND_BLOG);
		assertThat(b2Posts).isNotNull();
		assertThat(b2Posts.size()).isEqualTo(1);
		for (Post p : b2Posts) {
			assertThat(p.getIsRead()).isEqualTo(false);
		}

		// mark all posts read for b1
		adminController.markAllPostsReadForBlog(FIRST_BLOG);

		// b1 should all be read; b2 should all be unread
		b1Posts = restController.getAllPostsForBlog(FIRST_BLOG);
		assertThat(b1Posts).isNotNull();
		assertThat(b1Posts.size()).isEqualTo(2);
		for (Post p : b1Posts) {
			assertThat(p.getIsRead()).isEqualTo(true);
		}
		b2Posts = restController.getAllPostsForBlog(SECOND_BLOG);
		assertThat(b2Posts).isNotNull();
		assertThat(b2Posts.size()).isEqualTo(1);
		for (Post p : b2Posts) {
			assertThat(p.getIsRead()).isEqualTo(false);
		}

		// mark all undread for b1; read for b2
		adminController.markAllPostsUnreadForBlog(FIRST_BLOG);
		adminController.markAllPostsReadForBlog(SECOND_BLOG);

		// b1 should all be unread; b2 should all be read
		b1Posts = restController.getAllPostsForBlog(FIRST_BLOG);
		assertThat(b1Posts).isNotNull();
		assertThat(b1Posts.size()).isEqualTo(2);
		for (Post p : b1Posts) {
			assertThat(p.getIsRead()).isEqualTo(false);
		}
		b2Posts = restController.getAllPostsForBlog(SECOND_BLOG);
		assertThat(b2Posts).isNotNull();
		assertThat(b2Posts.size()).isEqualTo(1);
		for (Post p : b2Posts) {
			assertThat(p.getIsRead()).isEqualTo(true);
		}
	}

	/**
	 * Ensure that calling the API with one blog name but setting a post to a
	 * different blog name cause an error to be thrown.
	 */
	@Test(expected = BlogPostMismatchException.class)
	public void createPostWithInvalidBlogName() {
		Post p = new Post();
		p.setId(1L);
		p.setTumblelog(FIRST_BLOG);

		restController.createPostForBlog(SECOND_BLOG, p);
	}

	/**
	 * Simple test that multiple Metadata objects can be created for multiple blogs.
	 */
	@Test
	public void multipleMetadata() {
		Metadata md1 = mdRestController.getMetadataForBlogOrDefault(FIRST_BLOG);
		md1.setBaseMediaPath("media path for blog 1");
		md1 = mdRestController.updateMetadata(md1.getId(), md1);

		Metadata md2 = mdRestController.getMetadataForBlogOrDefault(SECOND_BLOG);
		md2.setBaseMediaPath("media path for blog 2");
		md2 = mdRestController.updateMetadata(md2.getId(), md2);

		Metadata response = mdRestController.getMetadataForBlog(FIRST_BLOG);
		assertThat(response).isNotNull();
		assertThat(response.getBaseMediaPath()).isEqualTo("media path for blog 1");

		response = mdRestController.getMetadataForBlog(SECOND_BLOG);
		assertThat(response).isNotNull();
		assertThat(response.getBaseMediaPath()).isEqualTo("media path for blog 2");
	}

}
