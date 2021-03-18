package com.tiyb.tev.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import com.tiyb.tev.datamodel.staging.StagingPost;
import com.tiyb.tev.html.HtmlTestingClass;

/**
 * Tests for staging posts, ensuring that they are separated by blog
 *
 * @author tiyb
 *
 */
public class TevStagingMultiBlog extends HtmlTestingClass {

    @Autowired
    private TEVStagingController controller;
    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Clean out all staged posts before each test
     */
    @Before
    public void cleanStagedPosts() {
        controller.deleteAllStagedPostsForBlog(MAIN_BLOG_NAME);
        controller.deleteAllStagedPostsForBlog(SECOND_BLOG_NAME);
    }

    /**
     * Creation of staged posts across multiple blogs
     */
    @Test
    public void multiBlogStagingPosts() {
        controller.createStagedPostForBlog(MAIN_BLOG_NAME, "1");
        controller.createStagedPostForBlog(SECOND_BLOG_NAME, "2");
        controller.createStagedPostForBlog(SECOND_BLOG_NAME, "3");

        List<String> posts = controller.getAllPostsForBlog(MAIN_BLOG_NAME);
        assertThat(posts).isNotNull();
        assertThat(posts.size()).isEqualTo(1);
        assertThat(posts.get(0)).isEqualTo("1");
        posts = controller.getAllPostsForBlog(SECOND_BLOG_NAME);
        assertThat(posts).isNotNull();
        assertThat(posts.size()).isEqualTo(2);
        assertThat(posts.get(0)).isEqualTo("2");
        assertThat(posts.get(1)).isEqualTo("3");
    }

    /**
     * Creation of staged posts across multiple blogs, via REST
     */
    @Test
    public void multiBlogStagingPostsRest() {
        restTemplate.postForObject(String.format("%s/staging-api/posts/%s/%s", baseUri(), MAIN_BLOG_NAME, "1"), "",
                StagingPost.class);
        restTemplate.postForObject(String.format("%s/staging-api/posts/%s/%s", baseUri(), SECOND_BLOG_NAME, "2"), "",
                StagingPost.class);
        restTemplate.postForObject(String.format("%s/staging-api/posts/%s/%s", baseUri(), SECOND_BLOG_NAME, "3"), "",
                StagingPost.class);

        ResponseEntity<String[]> responseEntity = restTemplate
                .getForEntity(String.format("%s/staging-api/posts/%s", baseUri(), MAIN_BLOG_NAME), String[].class);
        String[] posts = responseEntity.getBody();
        assertThat(posts).isNotNull();
        assertThat(posts.length).isEqualTo(1);
        assertThat(posts[0]).isEqualTo("1");
        responseEntity = restTemplate
                .getForEntity(String.format("%s/staging-api/posts/%s", baseUri(), SECOND_BLOG_NAME), String[].class);
        posts = responseEntity.getBody();
        assertThat(posts).isNotNull();
        assertThat(posts.length).isEqualTo(2);
        assertThat(posts[0]).isEqualTo("2");
        assertThat(posts[1]).isEqualTo("3");
    }

    /**
     * Deletion of a staged post doesn't impact others
     */
    @Test
    public void deleteAStagedost() {
        controller.createStagedPostForBlog(MAIN_BLOG_NAME, "1");
        controller.createStagedPostForBlog(SECOND_BLOG_NAME, "2");
        controller.createStagedPostForBlog(SECOND_BLOG_NAME, "3");

        controller.deleteStagedPostForBlog(SECOND_BLOG_NAME, "2");

        List<String> posts = controller.getAllPostsForBlog(MAIN_BLOG_NAME);
        assertThat(posts).isNotNull();
        assertThat(posts.size()).isEqualTo(1);
        assertThat(posts.get(0)).isEqualTo("1");
        posts = controller.getAllPostsForBlog(SECOND_BLOG_NAME);
        assertThat(posts).isNotNull();
        assertThat(posts.size()).isEqualTo(1);
        assertThat(posts.get(0)).isEqualTo("3");
    }

    /**
     * Deletion of a staged post doesn't impact others, via REST
     */
    @Test
    public void deleteAStagedostRest() {
        restTemplate.postForObject(String.format("%s/staging-api/posts/%s/%s", baseUri(), MAIN_BLOG_NAME, "1"), "",
                StagingPost.class);
        restTemplate.postForObject(String.format("%s/staging-api/posts/%s/%s", baseUri(), SECOND_BLOG_NAME, "2"), "",
                StagingPost.class);
        restTemplate.postForObject(String.format("%s/staging-api/posts/%s/%s", baseUri(), SECOND_BLOG_NAME, "3"), "",
                StagingPost.class);

        restTemplate.delete(String.format("%s/staging-api/posts/%s/%s", baseUri(), SECOND_BLOG_NAME, "2"));

        ResponseEntity<String[]> responseEntity = restTemplate
                .getForEntity(String.format("%s/staging-api/posts/%s", baseUri(), MAIN_BLOG_NAME), String[].class);
        String[] posts = responseEntity.getBody();
        assertThat(posts).isNotNull();
        assertThat(posts.length).isEqualTo(1);
        assertThat(posts[0]).isEqualTo("1");
        responseEntity = restTemplate
                .getForEntity(String.format("%s/staging-api/posts/%s", baseUri(), SECOND_BLOG_NAME), String[].class);
        posts = responseEntity.getBody();
        assertThat(posts).isNotNull();
        assertThat(posts.length).isEqualTo(1);
        assertThat(posts[0]).isEqualTo("3");
    }

    /**
     * Deleting all staged posts for a blog doesn't impact other blogs
     */
    @Test
    public void deleteAllStagedPostsFromABlog() {
        controller.createStagedPostForBlog(MAIN_BLOG_NAME, "1");
        controller.createStagedPostForBlog(SECOND_BLOG_NAME, "2");
        controller.createStagedPostForBlog(SECOND_BLOG_NAME, "3");

        controller.deleteAllStagedPostsForBlog(SECOND_BLOG_NAME);

        List<String> posts = controller.getAllPostsForBlog(MAIN_BLOG_NAME);
        assertThat(posts).isNotNull();
        assertThat(posts.size()).isEqualTo(1);
        assertThat(posts.get(0)).isEqualTo("1");
        posts = controller.getAllPostsForBlog(SECOND_BLOG_NAME);
        assertThat(posts).isNotNull();
        assertThat(posts.size()).isEqualTo(0);
    }

    /**
     * Deleting all staged posts for a blog doesn't impact other blogs, via REST
     */
    @Test
    public void deleteAllStagedPostsFromABlogRest() {
        restTemplate.postForObject(String.format("%s/staging-api/posts/%s/%s", baseUri(), MAIN_BLOG_NAME, "1"), "",
                StagingPost.class);
        restTemplate.postForObject(String.format("%s/staging-api/posts/%s/%s", baseUri(), SECOND_BLOG_NAME, "2"), "",
                StagingPost.class);
        restTemplate.postForObject(String.format("%s/staging-api/posts/%s/%s", baseUri(), SECOND_BLOG_NAME, "3"), "",
                StagingPost.class);

        restTemplate.delete(String.format("%s/staging-api/posts/%s", baseUri(), SECOND_BLOG_NAME));

        ResponseEntity<String[]> responseEntity = restTemplate
                .getForEntity(String.format("%s/staging-api/posts/%s", baseUri(), MAIN_BLOG_NAME), String[].class);
        String[] posts = responseEntity.getBody();
        assertThat(posts).isNotNull();
        assertThat(posts.length).isEqualTo(1);
        assertThat(posts[0]).isEqualTo("1");
        responseEntity = restTemplate
                .getForEntity(String.format("%s/staging-api/posts/%s", baseUri(), SECOND_BLOG_NAME), String[].class);
        posts = responseEntity.getBody();
        assertThat(posts).isNotNull();
        assertThat(posts.length).isEqualTo(0);
    }

}
