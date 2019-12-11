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
		controller.createStagedPost(1L);
		
		List<Long> posts = controller.getAllPosts();
		assertThat(posts).isNotNull();
		assertThat(posts.size()).isEqualTo(1);
		assertThat(posts.get(0)).isEqualTo(1L);
	}
	
	/**
	 * Tests creation of multiple staged posts
	 */
	@Test
	public void testCreatingMultiplePostsForStaging() {
		controller.createStagedPost(1L);
		controller.createStagedPost(2L);
		
		List<Long> posts = controller.getAllPosts();
		assertThat(posts).isNotNull();
		assertThat(posts.size()).isEqualTo(2);
		assertThat(posts.get(0)).isEqualTo(1L);
		assertThat(posts.get(1)).isEqualTo(2L);
	}
	
	/**
	 * Tests deletion of a staged post
	 */
	@Test
	public void testDeletingAStagedost() {
		controller.createStagedPost(1L);
		controller.createStagedPost(2L);
		
		controller.deleteStagedPost(2L);
		
		List<Long> posts = controller.getAllPosts();
		assertThat(posts).isNotNull();
		assertThat(posts.size()).isEqualTo(1);
		assertThat(posts.get(0)).isEqualTo(1L);
	}
	
	/**
	 * Tests deletion of all staged posts
	 */
	@Test
	public void testDeletingAllStagedPosts() {
		controller.createStagedPost(1L);
		controller.createStagedPost(2L);
		
		controller.deleteAllStagedPosts();
		
		List<Long> posts = controller.getAllPosts();
		assertThat(posts).isNotNull();
		assertThat(posts.size()).isEqualTo(0);
	}
}
