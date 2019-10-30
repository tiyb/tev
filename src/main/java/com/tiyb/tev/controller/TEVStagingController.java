package com.tiyb.tev.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiyb.tev.datamodel.staging.StagingPost;
import com.tiyb.tev.exception.ResourceNotFoundException;
import com.tiyb.tev.repository.staging.StagingPostRepository;

/**
 * This is the REST controller for working with items to be staged for eventual
 * export.
 * 
 * @author tiyb
 *
 */
@RestController
@RequestMapping("/staging-api")
public class TEVStagingController {

	Logger logger = LoggerFactory.getLogger(TEVStagingController.class);

	/**
	 * Repo for the staged data
	 */
	@Autowired
	StagingPostRepository stagingRepo;

	/**
	 * Returns all "staged posts" that have been added to the staging area
	 * 
	 * @return {@link java.util.List List} of all "staged post" IDs in the repo
	 */
	@GetMapping("/posts")
	public List<Long> getAllPosts() {
		List<StagingPost> posts = stagingRepo.findAll();

		List<Long> listOfIDs = new ArrayList<Long>();

		for (StagingPost post : posts) {
			listOfIDs.add(post.getId());
		}

		return listOfIDs;
	}

	/**
	 * Adds a "staged post" to the staging area
	 * 
	 * @param postID The ID of the StagingPost to be saved into the staging area
	 * @return The same {@link com.tiyb.tev.datamodel.staging.StagingPost
	 *         StagingPost} object
	 */
	@PostMapping("/posts/{id}")
	public StagingPost createStagedPost(@PathVariable(value = "id") Long postID) {
		StagingPost post = new StagingPost();
		post.setId(postID);

		return stagingRepo.save(post);
	}

	/**
	 * Removes a "staged post" from the staging area
	 * 
	 * @param id The ID of the "staged post" to be removed
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the details
	 */
	@DeleteMapping("/posts/{id}")
	public ResponseEntity<?> deleteStagedPost(@PathVariable(value = "id") Long id) {
		StagingPost post = stagingRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("StagedPost", "id", id));

		stagingRepo.delete(post);

		return ResponseEntity.ok().build();
	}

	/**
	 * Removes all "staged posts" from the staging area
	 * 
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the details
	 */
	@DeleteMapping("/posts")
	public ResponseEntity<?> deleteAllStagedPosts() {
		stagingRepo.deleteAll();

		return ResponseEntity.ok().build();
	}
}
