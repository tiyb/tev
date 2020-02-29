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
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.repository.PostRepository;
import com.tiyb.tev.repository.RegularRepository;

/**
 * REST controller for working with Regular posts. Heavy lifting done by the
 * {@link com.tiyb.tev.controller.helper.RepoAbstractor RepoAbstractor} generic class.
 *
 * @author tiyb
 *
 */
@RestController
@RequestMapping("/api")
public class TEVRegularController {

    /**
     * Repo for working with Post data
     */
    @Autowired
    private PostRepository postRepo;

    /**
     * Repo for working with "regular" post data
     */
    @Autowired
    private RegularRepository regularRepo;

    /**
     * Abstractor class for working with Regular posts
     */
    private RepoAbstractor<Regular> repoAbstractor;

    /**
     * Used to instantiate the repoAbstractor, since <code>@Autowired</code> members aren't
     * available until after the constructor is called
     */
    @PostConstruct
    private void instantiateAbstractor() {
        repoAbstractor = new RepoAbstractor<Regular>(regularRepo, Post.POST_TYPE_REGULAR, postRepo);
    }

    /**
     * GET request for listing all regular posts for a given blog
     *
     * @param blog Blog for which posts should be retrieved
     * @return {@link java.util.List List} of all regular posts in the database
     */
    @GetMapping("/posts/{blog}/regulars")
    public List<Regular> getAllRegularsForBlog(@PathVariable("blog") final String blog) {
        return repoAbstractor.getAllForBlog(blog);
    }

    /**
     * POST request to submit a "regular" post into the system. (The naming is awkward, but
     * "regular" is one type of Tumblr post.)
     *
     * @param blog    Used for validation purposes
     * @param postId  The ID of the post to which this "regular" post content refers
     * @param regular The data to be submitted
     * @return The same {@link com.tiyb.tev.datamodel.Regular Regular} object that was submitted.
     */
    @PostMapping("/posts/{blog}/{id}/regular")
    public Regular createRegularForBlog(@PathVariable("blog") final String blog,
            @PathVariable("id") final String postId, @Valid @RequestBody final Regular regular) {
        return repoAbstractor.createForBlog(blog, postId, regular);
    }

    /**
     * GET to return a single regular post by ID for a given blog
     *
     * @param blog   Not used
     * @param postId The Post ID
     * @return The {@link com.tiyb.tev.datamodel.Regular Regular} details
     */
    @GetMapping("/posts/{blog}/{id}/regular")
    public Regular getRegularForBlogById(@PathVariable("blog") final String blog,
            @PathVariable("id") final String postId) {
        return repoAbstractor.getItemById(postId);
    }

    /**
     * PUT to update a "Regular"
     *
     * @param blog           Not used
     * @param postId         The ID of the post to be updated
     * @param regularDetails The data to be updated
     * @return The same {@link com.tiyb.tev.datamodel.Regular Regular} object that was submitted
     */
    @PutMapping("/posts/{blog}/{id}/regular")
    public Regular updateRegularForBlog(@PathVariable("blog") final String blog,
            @PathVariable("id") final String postId, @RequestBody final Regular regularDetails) {
        return repoAbstractor.updateItem(postId, regularDetails);
    }

    /**
     * DEL to delete all "regular" posts in the DB for a given blog
     *
     * @param blog Blog for which posts should be deleted
     * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with the response
     *         details
     */
    @DeleteMapping("/posts/{blog}/regulars")
    public ResponseEntity<?> deleteAllRegularsForBlog(@PathVariable("blog") final String blog) {
        return repoAbstractor.deleteAllItemsForBlog(blog);
    }

    /**
     * DEL to delete a single regular post by ID for a given blog
     *
     * @param blog   Not used
     * @param postId the ID of the post to be deleted
     * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with the response
     *         details
     */
    @DeleteMapping("/posts/{id}/regular")
    public ResponseEntity<?> deleteRegularForBlog(@PathVariable("blog") final String blog,
            @PathVariable("id") final String postId) {
        return repoAbstractor.deleteItem(postId);
    }

}
