package com.tiyb.tev.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.exception.BlogPostMismatchException;
import com.tiyb.tev.exception.ResourceNotFoundException;
import com.tiyb.tev.repository.PostRepository;

/**
 * <p>
 * This is the REST controller for working with Post data, including special <i>kinds</i> of posts
 * such as "regular" and "photo" posts.
 * </p>
 *
 * <p>
 * <b>Note:</b> The APIs listed here are all blog-specific, even in cases where the blog name
 * shouldn't be necessary. e.g. retrieving a Post by ID shouldn't require a blog name to be
 * specified (there is no reason to assume IDs are not unique across blogs), but the APIs require
 * blog names even in these cases, for consistency.
 * </p>
 *
 * @author tiyb
 */
@RestController
@RequestMapping("/api")
public class TEVPostRestController {

    private Logger logger = LoggerFactory.getLogger(TEVPostRestController.class);

    /**
     * Repo for working with Post data
     */
    @Autowired
    private PostRepository postRepo;

    /**
     * REST controller for working with metadata
     */
    @Autowired
    private TEVMetadataRestController mdController;

    /**
     * REST controller for working with regular posts
     */
    @Autowired
    private TEVRegularController regController;

    /**
     * REST controller for working with Answer posts
     */
    @Autowired
    private TEVAnswerController answerController;

    /**
     * REST Controller for working with Link posts
     */
    @Autowired
    private TEVLinkController linkController;

    /**
     * REST controller for working with video posts
     */
    @Autowired
    private TEVVideoController videoController;

    /**
     * REST controller for working with photo posts
     */
    @Autowired
    private TEVPhotoController photoController;

    /**
     * REST controller for working with hashtags
     */
    @Autowired
    private TEVHashtagController hashtagController;

    /**
     * GET request for listing all posts for a given blog
     *
     * @param blog Blog for which posts should be retrieved
     * @return {@link java.util.List List} of all posts in the database
     */
    @GetMapping("/posts/{blog}")
    public List<Post> getAllPostsForBlog(@PathVariable("blog") final String blog) {
        return postRepo.findByTumblelog(blog);
    }

    /**
     * POST request to submit a Tumblr post into the system for a given blog
     *
     * @param blog Name of the blog for which the Post should be inserted
     * @param post The Post object (in JSON format) to be saved into the database
     * @return The same {@link com.tiyb.tev.datamodel.Post Post} object that was saved (including
     *         the ID)
     */
    @PostMapping("/posts/{blog}")
    public Post createPostForBlog(@PathVariable("blog") final String blog, @Valid @RequestBody final Post post) {
        if (!blog.equals(post.getTumblelog())) {
            logger.error("Post blog and API blog don't match; post blog={}, API blog={}", post.getTumblelog(), blog);
            throw new BlogPostMismatchException();
        }
        return postRepo.save(post);
    }

    /**
     * GET to return a single post by ID for a given Blog.
     *
     * @param blog   Name of the blog for the post (not used)
     * @param postId The Post ID
     * @return The {@link com.tiyb.tev.datamodel.Post Post} details
     */
    @GetMapping("/posts/{blog}/{id}")
    public Post getPostForBlogById(@PathVariable("blog") final String blog, @PathVariable("id") final String postId) {
        return postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
    }

    /**
     * PUT to update a Post for a given blog
     *
     * @param blog        The blog for which this post should be updated (not used, but needs to be
     *                    consistent with blog specified in Post)
     * @param postId      The ID of the post to be updated
     * @param postDetails The data to be updated
     * @return The same {@link com.tiyb.tev.datamodel.Post Post} object that was submitted
     */
    @PutMapping("/posts/{blog}/{id}")
    public Post updatePostForBlog(@PathVariable("blog") final String blog, @PathVariable("id") final String postId,
            @RequestBody final Post postDetails) {
        final Post post =
                postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        assert blog.equals(post.getTumblelog());

        post.updateData(postDetails);

        final Post updatedPost = postRepo.save(post);

        return updatedPost;
    }

    /**
     * PUT API for marking a post read for a given blog
     *
     * @param blog   Blog for which this post should be marked read (must be consistent with the
     *               post from the DB)
     * @param postId The ID of the post to be marked read
     * @return The modified Post
     */
    @PutMapping("/posts/{blog}/{id}/markRead")
    public Post markPostReadForBlog(@PathVariable("blog") final String blog, @PathVariable("id") final String postId) {
        Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        assert blog.equals(post.getTumblelog());

        post.setIsRead(true);

        post = postRepo.save(post);

        return post;
    }

    /**
     * PUT API to mark a post as a favourite for a given blog
     *
     * @param blog   Validated against post in DB
     * @param postId The ID of the post to be marked as a favourite
     * @return The modified Post
     */
    @PutMapping("/posts/{blog}/{id}/markFavourite")
    public Post markPostFavouriteForBlog(@PathVariable("blog") final String blog,
            @PathVariable("id") final String postId) {
        Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        assert blog.equals(post.getTumblelog());

        post.setIsFavourite(true);

        post = postRepo.save(post);

        return post;
    }

    /**
     * PUT API for marking a post unread for a given blog
     *
     * @param blog   Validated against post in DB
     * @param postId The ID of the post to be marked unread
     * @return The modified Post
     */
    @PutMapping("/posts/{blog}/{id}/markUnread")
    public Post markPostUnreadForBlog(@PathVariable("blog") final String blog,
            @PathVariable("id") final String postId) {
        Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        assert blog.equals(post.getTumblelog());

        post.setIsRead(false);

        post = postRepo.save(post);

        return post;
    }

    /**
     * PUT API for marking a post as not a favourite for a given blog
     *
     * @param blog   Validated against post in DB
     * @param postId The ID of the post to be marked as not a favourite
     * @return The modified Post
     */
    @PutMapping("/posts/{blog}/{id}/markNonFavourite")
    public Post markPostNonFavouriteForBlog(@PathVariable("blog") final String blog,
            @PathVariable("id") final String postId) {
        Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        assert blog.equals(post.getTumblelog());

        post.setIsFavourite(false);

        post = postRepo.save(post);

        return post;
    }

    /**
     * DEL to delete a single post by ID for a given blog
     *
     * @param blog   Validated against DB
     * @param postId the ID of the post to be deleted
     * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with the response
     *         details
     */
    @DeleteMapping("/posts/{blog}/{id}")
    public ResponseEntity<?> deletePostForBlog(@PathVariable("blog") final String blog,
            @PathVariable("id") final String postId) {
        final Post post =
                postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        assert blog.equals(post.getTumblelog());

        postRepo.delete(post);

        return ResponseEntity.ok().build();
    }

    /**
     * DEL to delete all posts in the DB for a given blog
     *
     * @param blog Blog for which posts should be deleted
     * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with the response
     *         details
     */
    @Transactional
    @DeleteMapping("/posts/{blog}")
    public ResponseEntity<?> deleteAllPostsForBlog(@PathVariable("blog") final String blog) {
        postRepo.deleteByTumblelog(blog);

        return ResponseEntity.ok().build();
    }

    public TEVMetadataRestController getMdController() {
        return mdController;
    }

    public TEVRegularController getRegController() {
        return regController;
    }

    public TEVAnswerController getAnswerController() {
        return answerController;
    }

    public TEVLinkController getLinkController() {
        return linkController;
    }

    public TEVVideoController getVideoController() {
        return videoController;
    }

    public TEVPhotoController getPhotoController() {
        return photoController;
    }

    public TEVHashtagController getHashtagController() {
        return hashtagController;
    }

}
