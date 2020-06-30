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

import com.tiyb.tev.TevTestingHelpers;

/**
 * Tests for staging posts, ensuring that they are separated by blog
 *
 * @author tiyb
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TevStagingMultiBlog {

    @Autowired
    private TEVStagingController controller;

    /**
     * Clean out all staged posts before each test
     */
    @Before
    public void cleanStagedPosts() {
        controller.deleteAllStagedPostsForBlog(TevTestingHelpers.MAIN_BLOG_NAME);
        controller.deleteAllStagedPostsForBlog(TevTestingHelpers.SECOND_BLOG_NAME);
    }

    /**
     * Creation of staged posts across multiple blogs
     */
    @Test
    public void multiBlogStagingPosts() {
        controller.createStagedPostForBlog(TevTestingHelpers.MAIN_BLOG_NAME, "1");
        controller.createStagedPostForBlog(TevTestingHelpers.SECOND_BLOG_NAME, "2");
        controller.createStagedPostForBlog(TevTestingHelpers.SECOND_BLOG_NAME, "3");

        List<String> posts = controller.getAllPostsForBlog(TevTestingHelpers.MAIN_BLOG_NAME);
        assertThat(posts).isNotNull();
        assertThat(posts.size()).isEqualTo(1);
        assertThat(posts.get(0)).isEqualTo("1");
        posts = controller.getAllPostsForBlog(TevTestingHelpers.SECOND_BLOG_NAME);
        assertThat(posts).isNotNull();
        assertThat(posts.size()).isEqualTo(2);
        assertThat(posts.get(0)).isEqualTo("2");
        assertThat(posts.get(1)).isEqualTo("3");
    }

    /**
     * Deletion of a staged post doesn't impact others
     */
    @Test
    public void testDeletingAStagedost() {
        controller.createStagedPostForBlog(TevTestingHelpers.MAIN_BLOG_NAME, "1");
        controller.createStagedPostForBlog(TevTestingHelpers.SECOND_BLOG_NAME, "2");
        controller.createStagedPostForBlog(TevTestingHelpers.SECOND_BLOG_NAME, "3");

        controller.deleteStagedPostForBlog(TevTestingHelpers.SECOND_BLOG_NAME, "2");

        List<String> posts = controller.getAllPostsForBlog(TevTestingHelpers.MAIN_BLOG_NAME);
        assertThat(posts).isNotNull();
        assertThat(posts.size()).isEqualTo(1);
        assertThat(posts.get(0)).isEqualTo("1");
        posts = controller.getAllPostsForBlog(TevTestingHelpers.SECOND_BLOG_NAME);
        assertThat(posts).isNotNull();
        assertThat(posts.size()).isEqualTo(1);
        assertThat(posts.get(0)).isEqualTo("3");
    }

    /**
     * Deleting all staged posts for a blog doesn't impact other blogs
     */
    @Test
    public void testDeletingAllStagedPostsFromABlog() {
        controller.createStagedPostForBlog(TevTestingHelpers.MAIN_BLOG_NAME, "1");
        controller.createStagedPostForBlog(TevTestingHelpers.SECOND_BLOG_NAME, "2");
        controller.createStagedPostForBlog(TevTestingHelpers.SECOND_BLOG_NAME, "3");

        controller.deleteAllStagedPostsForBlog(TevTestingHelpers.SECOND_BLOG_NAME);

        List<String> posts = controller.getAllPostsForBlog(TevTestingHelpers.MAIN_BLOG_NAME);
        assertThat(posts).isNotNull();
        assertThat(posts.size()).isEqualTo(1);
        assertThat(posts.get(0)).isEqualTo("1");
        posts = controller.getAllPostsForBlog(TevTestingHelpers.SECOND_BLOG_NAME);
        assertThat(posts).isNotNull();
        assertThat(posts.size()).isEqualTo(0);
    }

}
