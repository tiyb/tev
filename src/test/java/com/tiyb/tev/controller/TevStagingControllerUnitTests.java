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
 * Unit tests for the Staging Controller
 * 
 * @author tiyb
 *
 */
public class TevStagingControllerUnitTests extends HtmlTestingClass {

    private static final String BLOGNAME = "blogname";

    @Autowired
    private TEVStagingController controller;
    @Autowired
    private TestRestTemplate restTemplate;

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
     * tests creation of a staged post, via REST
     */
    @Test
    public void createPostForStagingRest() {
        restTemplate.postForObject(String.format("%s/staging-api/posts/%s/%s", baseUri(), BLOGNAME, "1"), "",
                StagingPost.class);

        ResponseEntity<String[]> responseEntity = restTemplate
                .getForEntity(String.format("%s/staging-api/posts/%s", baseUri(), BLOGNAME), String[].class);
        String[] posts = responseEntity.getBody();
        assertThat(posts).isNotNull();
        assertThat(posts.length).isEqualTo(1);
        assertThat(posts[0]).isEqualTo("1");
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
     * Tests creation of multiple staged posts, via REST
     */
    @Test
    public void createMultiplePostsForStagingRest() {
        restTemplate.postForObject(String.format("%s/staging-api/posts/%s/%s", baseUri(), BLOGNAME, "1"), "",
                StagingPost.class);
        restTemplate.postForObject(String.format("%s/staging-api/posts/%s/%s", baseUri(), BLOGNAME, "2"), "",
                StagingPost.class);

        ResponseEntity<String[]> responseEntity = restTemplate
                .getForEntity(String.format("%s/staging-api/posts/%s", baseUri(), BLOGNAME), String[].class);
        String[] posts = responseEntity.getBody();
        assertThat(posts).isNotNull();
        assertThat(posts.length).isEqualTo(2);
        assertThat(posts[0]).isEqualTo("1");
        assertThat(posts[1]).isEqualTo("2");
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
     * Tests deletion of a staged post, via REST
     */
    @Test
    public void deleteAStagedPostRest() {
        restTemplate.postForObject(String.format("%s/staging-api/posts/%s/%s", baseUri(), BLOGNAME, "1"), "",
                StagingPost.class);
        restTemplate.postForObject(String.format("%s/staging-api/posts/%s/%s", baseUri(), BLOGNAME, "2"), "",
                StagingPost.class);

        restTemplate.delete(String.format("%s/staging-api/posts/%s/%s", baseUri(), BLOGNAME, "2"));

        ResponseEntity<String[]> responseEntity = restTemplate
                .getForEntity(String.format("%s/staging-api/posts/%s", baseUri(), BLOGNAME), String[].class);
        String[] posts = responseEntity.getBody();
        assertThat(posts).isNotNull();
        assertThat(posts.length).isEqualTo(1);
        assertThat(posts[0]).isEqualTo("1");
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

    /**
     * Tests deletion of all staged posts, via REST
     */
    @Test
    public void deleteAllStagedPostsRest() {
        restTemplate.postForObject(String.format("%s/staging-api/posts/%s/%s", baseUri(), BLOGNAME, "1"), "",
                StagingPost.class);
        restTemplate.postForObject(String.format("%s/staging-api/posts/%s/%s", baseUri(), BLOGNAME, "2"), "",
                StagingPost.class);

        restTemplate.delete(String.format("%s/staging-api/posts/%s", baseUri(), BLOGNAME));

        ResponseEntity<String[]> responseEntity = restTemplate
                .getForEntity(String.format("%s/staging-api/posts/%s", baseUri(), BLOGNAME), String[].class);
        String[] posts = responseEntity.getBody();
        assertThat(posts).isNotNull();
        assertThat(posts.length).isEqualTo(0);
    }
}
