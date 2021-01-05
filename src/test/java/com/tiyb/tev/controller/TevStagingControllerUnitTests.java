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
 * Unit tests for the Staging Controller
 * @author tiyb
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TevStagingControllerUnitTests {

	private static final String BLOGNAME = "blogname";
    @Autowired
	private TEVStagingController controller;
    
    @Before
    public void setup() {
        controller.deleteAllStagedPostsForBlog(BLOGNAME);
    }

	/**
	 * tests creation of a staged post
	 */
	@Test
	public void createPostForStaging() {
		controller.createStagedPostForBlog(BLOGNAME, "1");

		List<String> posts = controller.getAllPostsForBlog(BLOGNAME);
		assertThat(posts).isNotNull();
		assertThat(posts.size()).isEqualTo(1);
		assertThat(posts.get(0)).isEqualTo("1");
	}

	/**
	 * Tests creation of multiple staged posts
	 */
	@Test
	public void createMultiplePostsForStaging() {
		controller.createStagedPostForBlog(BLOGNAME, "1");
		controller.createStagedPostForBlog(BLOGNAME, "2");

		List<String> posts = controller.getAllPostsForBlog(BLOGNAME);
		assertThat(posts).isNotNull();
		assertThat(posts.size()).isEqualTo(2);
		assertThat(posts.get(0)).isEqualTo("1");
		assertThat(posts.get(1)).isEqualTo("2");
	}

	/**
	 * Tests deletion of a staged post
	 */
	@Test
	public void deleteAStagedPost() {
		controller.createStagedPostForBlog(BLOGNAME, "1");
		controller.createStagedPostForBlog(BLOGNAME, "2");

		controller.deleteStagedPostForBlog(BLOGNAME, "2");

		List<String> posts = controller.getAllPostsForBlog(BLOGNAME);
		assertThat(posts).isNotNull();
		assertThat(posts.size()).isEqualTo(1);
		assertThat(posts.get(0)).isEqualTo("1");
	}

	/**
	 * Tests deletion of all staged posts
	 */
	@Test
	public void deleteAllStagedPosts() {
		controller.createStagedPostForBlog(BLOGNAME, "1");
		controller.createStagedPostForBlog(BLOGNAME, "2");

		controller.deleteAllStagedPostsForBlog(BLOGNAME);

		List<String> posts = controller.getAllPostsForBlog(BLOGNAME);
		assertThat(posts).isNotNull();
		assertThat(posts.size()).isEqualTo(0);
	}
}
