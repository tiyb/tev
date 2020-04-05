package com.tiyb.tev.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiyb.tev.datamodel.Hashtag;
import com.tiyb.tev.exception.ExistingTagException;
import com.tiyb.tev.repository.HashtagRepository;

/**
 * REST controller for working with hashtags. Doesn't use
 * {@link com.tiyb.tev.controller.helper.RepoAbstractor RepoAbstractor}, since the code is slightly
 * different from working with posts.
 *
 * @author tiyb
 *
 */
@RestController
@RequestMapping("/api")
public class TEVHashtagController {

    /**
     * String used to separate multiple blog names, when a hashtag shows up in multiple blogs
     */
    private static final String BLOG_SEPARATOR = ", ";

    /**
     * Repo for working with hashtags
     */
    @Autowired
    private HashtagRepository hashtagRepo;

    /**
     * GET request for listing <i>all</i> hashtags in the system, regardless of blog. Because
     * hashtags might be duplicated, logic is included to combine them together.
     *
     * @return List of hashtags with their counts.
     */
    @GetMapping("/hashtags")
    public List<Hashtag> getAllHashtags() {
        final List<Hashtag> allHT = hashtagRepo.findAll();
        final HashMap<String, Hashtag> filtered = new HashMap<String, Hashtag>();

        for (Hashtag h : allHT) {
            if (filtered.containsKey(h.getTag())) {
                final Hashtag current = filtered.get(h.getTag());
                current.setCount(current.getCount() + h.getCount());
                current.setBlog(current.getBlog() + BLOG_SEPARATOR + h.getBlog());
            } else {
                filtered.put(h.getTag(), h);
            }
        }

        return new ArrayList<Hashtag>(filtered.values());
    }

    /**
     * GET request for listing all hashtags stored in the system for a given blog
     *
     * @param blog Blog for which hashtags should be returned
     * @return {@link java.util.List List} of all hashtags in the database
     */
    @GetMapping("/hashtags/{blog}")
    public List<Hashtag> getAllHashtagsForBlog(@PathVariable("blog") final String blog) {
        return hashtagRepo.findByBlog(blog);
    }

    /**
     * POST request to insert a new hashtag into the system for a given blog. If it already exists
     * the existing hashtag is simply returned (no error is thrown).
     *
     * @param blog    Blog for which the hashtag should be inserted
     * @param hashtag The hashtag to be entered into the system
     * @return The new/existing hashtag object (with ID)
     */
    @PostMapping("/hashtags/{blog}")
    public Hashtag createHashtagForBlog(@PathVariable("blog") final String blog,
            @Valid @RequestBody final String hashtag) {
        Hashtag existingTag = hashtagRepo.findByTagAndBlog(hashtag, blog);

        if (existingTag != null) {
            existingTag.setCount(existingTag.getCount() + 1);
            existingTag = hashtagRepo.save(existingTag);
            return existingTag;
        }

        Hashtag newTag = new Hashtag();
        newTag.setTag(hashtag);
        newTag.setBlog(blog);
        newTag.setCount(1);

        newTag = hashtagRepo.save(newTag);
        return newTag;
    }

    /**
     * <p>
     * POST request to insert a new hashtag into the system with no blog associated with it. Simply
     * calls through to the {@link #createHashtagForBlog(String, String) createHashtagForBlog()}
     * method, passing an empty string for the blog name.
     * </p>
     *
     * <p>
     * Throws an error if the hashtag already exists in the system, for any other blog
     * </p>
     *
     * @param hashtag Hashtag to be created
     * @return The new/existing hashtag object (with ID)
     */
    @PostMapping("/hashtags/")
    public Hashtag createHashtagForNoBlog(@Valid @RequestBody final String hashtag) {
        final List<Hashtag> existingTags = hashtagRepo.findByTag(hashtag);
        if (existingTags.size() > 0) {
            throw new ExistingTagException();
        }
        return createHashtagForBlog(StringUtils.EMPTY, hashtag);
    }

    /**
     * DEL to delete all hashtags in the DB for a given blog
     *
     * @param blog Blog for which tags should be deleted
     * @return {@link org.springframework.http.ResponseEntity ResponseEntity<>} with the response
     *         details
     */
    @Transactional
    @DeleteMapping("/hashtags/{blog}")
    public ResponseEntity<?> deleteAllHashtagsForBlog(@PathVariable("blog") final String blog) {
        hashtagRepo.deleteByBlog(blog);

        return ResponseEntity.ok().build();
    }

    /**
     * DEL to delete a particular hashtag from the system, for a given blog
     *
     * @param hashtagToDelete Hashtag to be deleted
     * @return {@link org.springframework.http.ResponseEntity ResponseEntity<>} with the response
     *         details
     */
    @DeleteMapping("/hashtags")
    public ResponseEntity<?> deleteHashTag(@RequestBody final Hashtag hashtagToDelete) {
        final Hashtag htToDelete = hashtagRepo.findByTagAndBlog(hashtagToDelete.getTag(), hashtagToDelete.getBlog());
        hashtagRepo.delete(htToDelete);

        return ResponseEntity.ok().build();
    }

    /**
     * Package-public method to delete all hashtags in the system, regardless of blog. Used only in
     * JUnit tests; no API associated with the method.
     */
    public void deleteAllHTs() {
        hashtagRepo.deleteAll();
    }
}
