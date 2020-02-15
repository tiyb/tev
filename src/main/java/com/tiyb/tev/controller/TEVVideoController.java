package com.tiyb.tev.controller;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiyb.tev.controller.helper.RepoAbstractor;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Video;
import com.tiyb.tev.repository.PostRepository;
import com.tiyb.tev.repository.VideoRepository;

/**
 * REST controller for working with Videos. {@link com.tiyb.tev.controller.helper.RepoAbstractor
 * RepoAbstractor} class does the heavy lifting.
 *
 * @author tiyb
 *
 */
@RestController
@RequestMapping("/api")
public class TEVVideoController {

    /**
     * Autowired post repo
     */
    @Autowired
    private PostRepository postRepo;

    /**
     * Autowired video repo
     */
    @Autowired
    private VideoRepository videoRepo;

    /**
     * Abstractor which does the heavy lifting
     */
    private RepoAbstractor<Video> repoAbstractor;

    /**
     * Sets up Repo Abstractor after the autowired members have been wired
     */
    @PostConstruct
    private void instantiateAbstractor() {
        repoAbstractor = new RepoAbstractor<Video>(videoRepo, Post.POST_TYPE_VIDEO, postRepo);
    }

    /**
     * GET request for listing all videos for a given blog
     *
     * @param blog Blog for which to return the video posts
     * @return {@link java.util.List List} of all videos in the database
     */
    @GetMapping("/posts/{blog}/videos")
    public List<Video> getAllVideosForBlog(@PathVariable("blog") final String blog) {
        return repoAbstractor.getAllForBlog(blog);
    }

    /**
     * POST request to submit a Tumblr "video post" into the system for a given blog
     *
     * @param blog   The blog for which this video is being created
     * @param postId The ID of the post to which this video content refers
     * @param video  The data to be submitted
     * @return The same {@link com.tiyb.tev.datamodel.Video Video} object that was submitted
     */
    @PostMapping("/posts/{blog}/{id}/video")
    public Video createVideoForBlog(@PathVariable("blog") final String blog, @PathVariable("id") final Long postId,
            @Valid @RequestBody final Video video) {
        return repoAbstractor.createForBlog(blog, postId, video);
    }

    /**
     * GET to return a single video post by ID for a given blog
     *
     * @param blog   Not used
     * @param postId The Post ID
     * @return The {@link com.tiyb.tev.datamodel.Video Video} details
     */
    @GetMapping("/posts/{blog}/{id}/video")
    public Video getVideoForBlogById(@PathVariable("blog") final String blog, @PathVariable("id") final Long postId) {
        return repoAbstractor.getItemById(postId);
    }

    /**
     * PUT to update a Video
     *
     * @param blog         Not used
     * @param postId       The ID of the post to be updated
     * @param videoDetails The data to be updated
     * @return The same {@link com.tiyb.tev.datamodel.Video Video} object that was submitted
     */
    @PutMapping("/posts/{blog}/{id}/video")
    public Video updateVideoForBlog(@PathVariable("blog") final String blog, @PathVariable("id") final Long postId,
            @RequestBody final Video videoDetails) {
        return repoAbstractor.updateItem(postId, videoDetails);
    }

    /**
     * DEL to delete all "video" posts in the DB for a given blog
     *
     * @param blog Blog for which videos should be deleted
     * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with the response
     *         details
     */
    @DeleteMapping("/posts/{blog}/videos")
    public ResponseEntity<?> deleteAllVideosForBlog(@PathVariable("blog") final String blog) {
        return repoAbstractor.deleteAllItemsForBlog(blog);
    }

    /**
     * DEL to delete a single video by ID for a given blog
     *
     * @param blog   Not used
     * @param postId the ID of the post to be deleted
     * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with the response
     *         details
     */
    @DeleteMapping("/posts/{blog}/{id}/video")
    public ResponseEntity<?> deleteVideoForBlog(@PathVariable("blog") final String blog,
            @PathVariable("id") final Long postId) {
        return repoAbstractor.deleteItem(postId);
    }
}
