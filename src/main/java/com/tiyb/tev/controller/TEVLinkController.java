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
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.repository.LinkRepository;
import com.tiyb.tev.repository.PostRepository;

/**
 * REST controller for working with Link items. Heavy lifting performec by the
 * {@link com.tiyb.tev.controller.helper.RepoAbstractor RepoAbstractor} class.
 *
 * @author tiyb
 *
 */
@RestController
@RequestMapping("/api")
public class TEVLinkController {

    /**
     * Post repo
     */
    @Autowired
    private PostRepository postRepo;

    /**
     * Link Repo
     */
    @Autowired
    private LinkRepository linkRepo;

    /**
     * Abstractor which does the heavy lifting
     */
    private RepoAbstractor<Link> repoAbstractor;

    /**
     * Instantiates the abstractor class after the autowired members have been wired
     */
    @PostConstruct
    private void instantiateAbstractor() {
        repoAbstractor = new RepoAbstractor<Link>(linkRepo, Post.POST_TYPE_LINK, postRepo);
    }

    /**
     * GET request for listing all links for a given blog
     *
     * @param blog Blog for which to retrieve links
     * @return {@link java.util.List List} of all links in the database
     */
    @GetMapping("/posts/{blog}/links")
    public List<Link> getAllLinksForBlog(@PathVariable("blog") final String blog) {
        return repoAbstractor.getAllForBlog(blog);
    }

    /**
     * POST request to submit a Tumblr "link" into the system for a given blog
     *
     * @param blog   Used for validation purposes
     * @param postId The ID of the post to which this link refers
     * @param link   The data to be submitted
     * @return The same {@link com.tiyb.tev.datamodel.Link Link} object that was submitted
     */
    @PostMapping("/posts/{blog}/{id}/link")
    public Link createLinkForBlog(@PathVariable("blog") final String blog, @PathVariable("id") final Long postId,
            @Valid @RequestBody final Link link) {
        return repoAbstractor.createForBlog(blog, postId, link);
    }

    /**
     * GET to return a single link by ID for a given blog
     *
     * @param blog   not used
     * @param postId The Post ID
     * @return The {@link com.tiyb.tev.datamodel.Link Link} details
     */
    @GetMapping("/posts/{blog}/{id}/link")
    public Link getLinkForBlogById(@PathVariable("blog") final String blog, @PathVariable("id") final Long postId) {
        return repoAbstractor.getItemById(postId);
    }

    /**
     * PUT to update a Link for a given blog
     *
     * @param blog        Not used
     * @param postId      The ID of the post to be updated
     * @param linkDetails The data to be updated
     * @return The same {@link com.tiyb.tev.datamodel.Link Link} object that was submitted
     */
    @PutMapping("/posts/{blog}/{id}/link")
    public Link updateLinkForBlog(@PathVariable("blog") final String blog, @PathVariable("id") final Long postId,
            @RequestBody final Link linkDetails) {
        return repoAbstractor.updateItem(postId, linkDetails);
    }

    /**
     * DEL to delete all "link" posts in the DB for a given blog
     *
     * @param blog Blog for which to delete the links
     * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with the response
     *         details
     */
    @DeleteMapping("/posts/{blog}/links")
    public ResponseEntity<?> deleteAllLinksForBlog(@PathVariable("blog") final String blog) {
        return repoAbstractor.deleteAllItemsForBlog(blog);
    }

    /**
     * DEL to delete a single link by ID for a given blog
     *
     * @param blog   Not used
     * @param postId the ID of the post to be deleted
     * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with the response
     *         details
     */
    @DeleteMapping("/posts/{blog}/{id}/link")
    public ResponseEntity<?> deleteLinkForBlog(@PathVariable("blog") final String blog,
            @PathVariable("id") final Long postId) {
        return repoAbstractor.deleteItem(postId);
    }
}
