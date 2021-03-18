package com.tiyb.tev.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.exception.BlogPostMismatchException;
import com.tiyb.tev.html.HtmlTestingClass;

/**
 * Unit tests for Posts that are focused on use cases where there are multiple
 * blogs in the DB, not just one. Includes some tests of the Admin Controller
 * and some of the Post controller.
 *
 * @author tiyb
 *
 */
public class TevMultiBlogPostUnitTests extends HtmlTestingClass {

    @Autowired
    private TEVPostRestController restController;
    @Autowired
    private TEVMetadataRestController mdRestController;
    @Autowired
    private TEVAdminToolsController adminController;
    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void cleanPosts() {
        restController.deleteAllPostsForBlog(MAIN_BLOG_NAME);
        restController.deleteAllPostsForBlog(SECOND_BLOG_NAME);
    }

    /**
     * Simple test that posts can be created in different blogs, and not mixed up
     * with each other.
     */
    @Test
    public void twoPostsTwoBlogs() {
        Post p1 = new Post();
        p1.setId("1");
        p1.setTumblelog(MAIN_BLOG_NAME);
        p1 = restController.createPostForBlog(MAIN_BLOG_NAME, p1);

        Post p2 = new Post();
        p2.setId("2");
        p2.setTumblelog(SECOND_BLOG_NAME);
        p2 = restController.createPostForBlog(SECOND_BLOG_NAME, p2);

        List<Post> blogsForOne = restController.getAllPostsForBlog(MAIN_BLOG_NAME);
        assertThat(blogsForOne).isNotNull();
        assertThat(blogsForOne.size()).isEqualTo(1);
        assertThat(blogsForOne.get(0).getId()).isEqualTo("1");

        List<Post> blogsForTwo = restController.getAllPostsForBlog(SECOND_BLOG_NAME);
        assertThat(blogsForTwo).isNotNull();
        assertThat(blogsForTwo.size()).isEqualTo(1);
        assertThat(blogsForTwo.get(0).getId()).isEqualTo("2");
    }

    /**
     * Simple test that posts can be created in different blogs, and not mixed up
     * with each other, via REST.
     */
    @Test
    public void twoPostsTwoBlogsRest() {
        Post p1 = new Post();
        p1.setId("1");
        p1.setTumblelog(MAIN_BLOG_NAME);
        p1 = restTemplate.postForObject(String.format("%s/api/posts/%s", baseUri(), MAIN_BLOG_NAME), p1, Post.class);

        Post p2 = new Post();
        p2.setId("2");
        p2.setTumblelog(SECOND_BLOG_NAME);
        p2 = restTemplate.postForObject(String.format("%s/api/posts/%s", baseUri(), SECOND_BLOG_NAME), p2, Post.class);

        ResponseEntity<Post[]> responseEntity = restTemplate
                .getForEntity(String.format("%s/api/posts/%s", baseUri(), MAIN_BLOG_NAME), Post[].class);
        Post[] blogsForOne = responseEntity.getBody();
        assertThat(blogsForOne).isNotEmpty();
        assertThat(blogsForOne.length).isEqualTo(1);
        assertThat(blogsForOne[0].getId()).isEqualTo("1");

        responseEntity = restTemplate.getForEntity(String.format("%s/api/posts/%s", baseUri(), SECOND_BLOG_NAME),
                Post[].class);
        Post[] blogsForTwo = responseEntity.getBody();
        assertThat(blogsForTwo).isNotEmpty();
        assertThat(blogsForTwo.length).isEqualTo(1);
        assertThat(blogsForTwo[0].getId()).isEqualTo("2");
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
        p1.setId("1");
        p1.setTumblelog(MAIN_BLOG_NAME);
        p1.setIsRead(false);
        p1 = restController.createPostForBlog(MAIN_BLOG_NAME, p1);
        Post p2 = new Post();
        p2.setId("2");
        p2.setTumblelog(MAIN_BLOG_NAME);
        p2.setIsRead(false);
        p2 = restController.createPostForBlog(MAIN_BLOG_NAME, p2);

        // blog 2 posts
        Post p3 = new Post();
        p3.setId("3");
        p3.setTumblelog(SECOND_BLOG_NAME);
        p3.setIsRead(false);
        p3 = restController.createPostForBlog(SECOND_BLOG_NAME, p3);

        // test blog 1 initial state
        List<Post> b1Posts = restController.getAllPostsForBlog(MAIN_BLOG_NAME);
        assertThat(b1Posts).isNotNull();
        assertThat(b1Posts.size()).isEqualTo(2);
        for (Post p : b1Posts) {
            assertThat(p.getIsRead()).isEqualTo(false);
        }

        // test blog 2 initial state
        List<Post> b2Posts = restController.getAllPostsForBlog(SECOND_BLOG_NAME);
        assertThat(b2Posts).isNotNull();
        assertThat(b2Posts.size()).isEqualTo(1);
        for (Post p : b2Posts) {
            assertThat(p.getIsRead()).isEqualTo(false);
        }

        // mark all posts read for b1
        adminController.markAllPostsReadForBlog(MAIN_BLOG_NAME);

        // b1 should all be read; b2 should all be unread
        b1Posts = restController.getAllPostsForBlog(MAIN_BLOG_NAME);
        assertThat(b1Posts).isNotNull();
        assertThat(b1Posts.size()).isEqualTo(2);
        for (Post p : b1Posts) {
            assertThat(p.getIsRead()).isEqualTo(true);
        }
        b2Posts = restController.getAllPostsForBlog(SECOND_BLOG_NAME);
        assertThat(b2Posts).isNotNull();
        assertThat(b2Posts.size()).isEqualTo(1);
        for (Post p : b2Posts) {
            assertThat(p.getIsRead()).isEqualTo(false);
        }

        // mark all undread for b1; read for b2
        adminController.markAllPostsUnreadForBlog(MAIN_BLOG_NAME);
        adminController.markAllPostsReadForBlog(SECOND_BLOG_NAME);

        // b1 should all be unread; b2 should all be read
        b1Posts = restController.getAllPostsForBlog(MAIN_BLOG_NAME);
        assertThat(b1Posts).isNotNull();
        assertThat(b1Posts.size()).isEqualTo(2);
        for (Post p : b1Posts) {
            assertThat(p.getIsRead()).isEqualTo(false);
        }
        b2Posts = restController.getAllPostsForBlog(SECOND_BLOG_NAME);
        assertThat(b2Posts).isNotNull();
        assertThat(b2Posts.size()).isEqualTo(1);
        for (Post p : b2Posts) {
            assertThat(p.getIsRead()).isEqualTo(true);
        }
    }

    /**
     * Verify that marking all posts "read" or "unread" for one blog doesn't impact
     * the other. Two blogs created, two posts each, and both are tested back and
     * forth, via REST.
     */
    @Test
    public void markReadUnreadCrossBlogsRest() {
        // blog 1 posts
        Post p1 = new Post();
        p1.setId("1");
        p1.setTumblelog(MAIN_BLOG_NAME);
        p1.setIsRead(false);
        p1 = restTemplate.postForObject(String.format("%s/api/posts/%s", baseUri(), MAIN_BLOG_NAME), p1, Post.class);
        Post p2 = new Post();
        p2.setId("2");
        p2.setTumblelog(MAIN_BLOG_NAME);
        p2.setIsRead(false);
        p2 = restTemplate.postForObject(String.format("%s/api/posts/%s", baseUri(), MAIN_BLOG_NAME), p2, Post.class);

        // blog 2 posts
        Post p3 = new Post();
        p3.setId("3");
        p3.setTumblelog(SECOND_BLOG_NAME);
        p3.setIsRead(false);
        p3 = restTemplate.postForObject(String.format("%s/api/posts/%s", baseUri(), SECOND_BLOG_NAME), p3, Post.class);

        // test blog 1 initial state
        ResponseEntity<Post[]> responseEntity = restTemplate
                .getForEntity(String.format("%s/api/posts/%s", baseUri(), MAIN_BLOG_NAME), Post[].class);
        Post[] b1Posts = responseEntity.getBody();
        assertThat(b1Posts).isNotEmpty();
        assertThat(b1Posts.length).isEqualTo(2);
        for (Post p : b1Posts) {
            assertThat(p.getIsRead()).isEqualTo(false);
        }

        // test blog 2 initial state
        responseEntity = restTemplate.getForEntity(String.format("%s/api/posts/%s", baseUri(), SECOND_BLOG_NAME),
                Post[].class);
        Post[] b2Posts = responseEntity.getBody();
        assertThat(b2Posts).isNotEmpty();
        assertThat(b2Posts.length).isEqualTo(1);
        for (Post p : b2Posts) {
            assertThat(p.getIsRead()).isEqualTo(false);
        }

        // mark all posts read for b1
        restTemplate.getForObject(String.format("%s/admintools/posts/%s/markAllRead", baseUri(), MAIN_BLOG_NAME),
                String.class);

        // b1 should all be read; b2 should all be unread
        responseEntity = restTemplate.getForEntity(String.format("%s/api/posts/%s", baseUri(), MAIN_BLOG_NAME),
                Post[].class);
        b1Posts = responseEntity.getBody();
        assertThat(b1Posts).isNotNull();
        assertThat(b1Posts.length).isEqualTo(2);
        for (Post p : b1Posts) {
            assertThat(p.getIsRead()).isEqualTo(true);
        }
        responseEntity = restTemplate.getForEntity(String.format("%s/api/posts/%s", baseUri(), SECOND_BLOG_NAME),
                Post[].class);
        b2Posts = responseEntity.getBody();
        assertThat(b2Posts).isNotNull();
        assertThat(b2Posts.length).isEqualTo(1);
        for (Post p : b2Posts) {
            assertThat(p.getIsRead()).isEqualTo(false);
        }

        // mark all undread for b1; read for b2
        restTemplate.getForObject(String.format("%s/admintools/posts/%s/markAllUnread", baseUri(), MAIN_BLOG_NAME),
                String.class);
        restTemplate.getForObject(String.format("%s/admintools/posts/%s/markAllRead", baseUri(), SECOND_BLOG_NAME),
                String.class);

        // b1 should all be unread; b2 should all be read
        responseEntity = restTemplate.getForEntity(String.format("%s/api/posts/%s", baseUri(), MAIN_BLOG_NAME),
                Post[].class);
        b1Posts = responseEntity.getBody();
        assertThat(b1Posts).isNotNull();
        assertThat(b1Posts.length).isEqualTo(2);
        for (Post p : b1Posts) {
            assertThat(p.getIsRead()).isEqualTo(false);
        }
        responseEntity = restTemplate.getForEntity(String.format("%s/api/posts/%s", baseUri(), SECOND_BLOG_NAME),
                Post[].class);
        b2Posts = responseEntity.getBody();
        assertThat(b2Posts).isNotNull();
        assertThat(b2Posts.length).isEqualTo(1);
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
        p.setId("1");
        p.setTumblelog(MAIN_BLOG_NAME);

        restController.createPostForBlog(SECOND_BLOG_NAME, p);
    }

    /**
     * Ensure that calling the API with one blog name but setting a post to a
     * different blog name cause an error to be thrown, via REST.
     */
    @Test
    public void createPostWithInvalidBlogNameRest() throws URISyntaxException {
        Post p = new Post();
        p.setId("1");
        p.setTumblelog(MAIN_BLOG_NAME);

        URI uri = new URI(String.format("%s/api/posts/%s", baseUri(), SECOND_BLOG_NAME));
        RequestEntity<Post> requestEntity = RequestEntity.post(uri).body(p);
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /**
     * Simple test that multiple Metadata objects can be created for multiple blogs.
     */
    @Test
    public void multipleMetadata() {
        Metadata md1 = mdRestController.getMetadataForBlogOrDefault(MAIN_BLOG_NAME);
        md1.setBaseMediaPath("media path for blog 1");
        md1 = mdRestController.updateMetadata(md1.getId(), md1);

        Metadata md2 = mdRestController.getMetadataForBlogOrDefault(SECOND_BLOG_NAME);
        md2.setBaseMediaPath("media path for blog 2");
        md2 = mdRestController.updateMetadata(md2.getId(), md2);

        Metadata response = mdRestController.getMetadataForBlog(MAIN_BLOG_NAME);
        assertThat(response).isNotNull();
        assertThat(response.getBaseMediaPath()).isEqualTo("media path for blog 1");

        response = mdRestController.getMetadataForBlog(SECOND_BLOG_NAME);
        assertThat(response).isNotNull();
        assertThat(response.getBaseMediaPath()).isEqualTo("media path for blog 2");
    }

    /**
     * Simple test that multiple Metadata objects can be created for multiple blogs,
     * via REST.
     */
    @Test
    public void multipleMetadataRest() {
        Metadata md1 = restTemplate.getForObject(
                String.format("%s/api/metadata/byBlog/%s/orDefault", baseUri(), MAIN_BLOG_NAME), Metadata.class);
        md1.setBaseMediaPath("media path for blog 1");
        restTemplate.put(String.format("%s/api/metadata/%d", baseUri(), md1.getId()), md1);

        Metadata md2 = restTemplate.getForObject(
                String.format("%s/api/metadata/byBlog/%s/orDefault", baseUri(), SECOND_BLOG_NAME), Metadata.class);
        md2.setBaseMediaPath("media path for blog 2");
        restTemplate.put(String.format("%s/api/metadata/%d", baseUri(), md2.getId()), md2);

        Metadata response = restTemplate
                .getForObject(String.format("%s/api/metadata/byBlog/%s", baseUri(), MAIN_BLOG_NAME), Metadata.class);
        assertThat(response).isNotNull();
        assertThat(response.getBaseMediaPath()).isEqualTo("media path for blog 1");

        response = restTemplate.getForObject(String.format("%s/api/metadata/byBlog/%s", baseUri(), SECOND_BLOG_NAME),
                Metadata.class);
        assertThat(response).isNotNull();
        assertThat(response.getBaseMediaPath()).isEqualTo("media path for blog 2");
    }

}
