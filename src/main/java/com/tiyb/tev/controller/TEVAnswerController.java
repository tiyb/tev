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
import com.tiyb.tev.datamodel.Answer;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.repository.AnswerRepository;
import com.tiyb.tev.repository.PostRepository;

/**
 * REST controller for working with Answers
 *
 * @author tiyb
 *
 */
@RestController
@RequestMapping("/api")
public class TEVAnswerController {

    /**
     * Post Repo
     */
    @Autowired
    private PostRepository postRepo;

    /**
     * Answer Repo
     */
    @Autowired
    private AnswerRepository answerRepo;

    /**
     * Performs the heavy lifting
     */
    private RepoAbstractor<Answer> repoAbstractor;

    /**
     * Sets up the repoAbstractor object, after the autowired items have been filled in
     */
    @PostConstruct
    private void instantiateAbstractor() {
        repoAbstractor = new RepoAbstractor<Answer>(answerRepo, Post.POST_TYPE_ANSWER, postRepo);
    }

    /**
     * GET request for listing all answers for a given blog
     *
     * @param blog Blog for which answers should be returned
     * @return {@link java.util.List List} of all answers in the database
     */
    @GetMapping("/posts/{blog}/answers")
    public List<Answer> getAllAnswersForBlog(@PathVariable("blog") final String blog) {
        return repoAbstractor.getAllForBlog(blog);
    }

    /**
     * POST request to submit a Tumblr "answer" into the system for a given blog
     *
     * @param blog   Validated against content
     * @param postId The ID of the post to which this answer refers
     * @param answer The data to be submitted
     * @return The same {@link com.tiyb.tev.datamodel.Answer Answer} object that was submitted
     */
    @PostMapping("/posts/{blog}/{id}/answer")
    public Answer createAnswerForBlog(@PathVariable("blog") final String blog, @PathVariable("id") final String postId,
            @Valid @RequestBody final Answer answer) {
        return repoAbstractor.createForBlog(blog, postId, answer);
    }

    /**
     * GET to return a single answer, by ID
     *
     * @param blog   not used
     * @param postId The Post ID
     * @return The {@link com.tiyb.tev.datamodel.Answer Answer} details
     */
    @GetMapping("/posts/{blog}/{id}/answer")
    public Answer getAnswerForBlogById(@PathVariable("blog") final String blog,
            @PathVariable("id") final String postId) {
        return repoAbstractor.getItemById(postId);
    }

    /**
     * PUT to update an Answer
     *
     * @param blog          Not used
     * @param postId        The ID of the post to be updated
     * @param answerDetails The data to be updated
     * @return The same {@link com.tiyb.tev.datamodel.Answer Answer} object that was submitted
     */
    @PutMapping("/posts/{blog}/{id}/answer")
    public Answer updateAnswerForBlog(@PathVariable("blog") final String blog, @PathVariable("id") final String postId,
            @RequestBody final Answer answerDetails) {
        return repoAbstractor.updateItem(postId, answerDetails);
    }

    /**
     * DEL to delete all "answer" posts in the DB for a given blog
     *
     * @param blog Blog for which answers should be deleted
     * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with the response
     *         details
     */
    @DeleteMapping("/posts/{blog}/answers")
    public ResponseEntity<?> deleteAllAnswersForBlog(@PathVariable("blog") final String blog) {
        return repoAbstractor.deleteAllItemsForBlog(blog);
    }

    /**
     * DEL to delete a single answer by ID for a given blog
     *
     * @param blog   Not used
     * @param postId the ID of the post to be deleted
     * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with the response
     *         details
     */
    @DeleteMapping("/posts/{blog}/{id}/answer")
    public ResponseEntity<?> deleteAnswerForBlog(@PathVariable("blog") final String blog,
            @PathVariable("id") final String postId) {
        return repoAbstractor.deleteItem(postId);
    }
}
