package com.tiyb.tev.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Unit tests for the Staging Controller
 * @author tiyb
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TevStagingControllerUnitTests {

	@Autowired
	private TEVStagingController controller;

	/**
	 * tests creation of a staged post
	 */
	@Test
	public void testCreatingPostForStaging() {
		controller.createStagedPostForBlog("blogname", "1");

		List<String> posts = controller.getAllPostsForBlog("blogname");
		assertThat(posts).isNotNull();
		assertThat(posts.size()).isEqualTo(1);
		assertThat(posts.get(0)).isEqualTo("1");
	}

	/**
	 * Tests creation of multiple staged posts
	 */
	@Test
	public void testCreatingMultiplePostsForStaging() {
		controller.createStagedPostForBlog("blogname", "1");
		controller.createStagedPostForBlog("blogname", "2");

		List<String> posts = controller.getAllPostsForBlog("blogname");
		assertThat(posts).isNotNull();
		assertThat(posts.size()).isEqualTo(2);
		assertThat(posts.get(0)).isEqualTo("1");
		assertThat(posts.get(1)).isEqualTo("2");
	}

	/**
	 * Tests deletion of a staged post
	 */
	@Test
	public void testDeletingAStagedost() {
		controller.createStagedPostForBlog("blogname", "1");
		controller.createStagedPostForBlog("blogname", "2");

		controller.deleteStagedPostForBlog("blogname", "2");

		List<String> posts = controller.getAllPostsForBlog("blogname");
		assertThat(posts).isNotNull();
		assertThat(posts.size()).isEqualTo(1);
		assertThat(posts.get(0)).isEqualTo("1");
	}

	/**
	 * Tests deletion of all staged posts
	 */
	@Test
	public void testDeletingAllStagedPosts() {
		controller.createStagedPostForBlog("blogname", "1");
		controller.createStagedPostForBlog("blogname", "2");

		controller.deleteAllStagedPostsForBlog("blogname");

		List<String> posts = controller.getAllPostsForBlog("blogname");
		assertThat(posts).isNotNull();
		assertThat(posts.size()).isEqualTo(0);
	}
}
