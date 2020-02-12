package com.tiyb.tev.controller;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiyb.tev.datamodel.staging.StagingPost;
import com.tiyb.tev.exception.InvalidConvoParentException;
import com.tiyb.tev.exception.ResourceNotFoundException;
import com.tiyb.tev.repository.staging.StagingPostRepository;

/**
 * This is the REST controller for working with items to be staged for eventual
 * export. All APIs have the blog set, even in cases where it wouldn't
 * technically be needed.
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
	 * REST controller for posts
	 */
	@Autowired
	TEVPostRestController postController;

	/**
	 * REST controller for metadata
	 */
	@Autowired
	TEVMetadataRestController mdController;

	/**
	 * Message used for an invalid target directory for image export
	 */
	@Value("${controller.staging.invalidTargetDir}")
	private String INVALID_TARGET_DIRECTORY;

	/**
	 * Message used for an error copying files
	 */
	@Value("${controller.staging.fileCopyError}")
	private String ERROR_COPYING_FILE;

	/**
	 * Returns all "staged posts" that have been added to the staging area for a
	 * given blog
	 * 
	 * @param blog Blog for which to return posts
	 * @return {@link java.util.List List} of all "staged post" IDs in the repo
	 */
	@GetMapping("/posts/{blog}")
	public List<Long> getAllPostsForBlog(@PathVariable("blog") String blog) {
		List<StagingPost> posts = stagingRepo.findByBlog(blog);

		List<Long> listOfIDs = new ArrayList<Long>();

		for (StagingPost post : posts) {
			listOfIDs.add(post.getId());
		}

		return listOfIDs;
	}

	/**
	 * Adds a "staged post" to the staging area for a given blog
	 * 
	 * @param blog   The blog for which the staged post should be added
	 * @param postID The ID of the StagingPost to be saved into the staging area
	 * @return The same {@link com.tiyb.tev.datamodel.staging.StagingPost
	 *         StagingPost} object
	 */
	@PostMapping("/posts/{blog}/{id}")
	public StagingPost createStagedPostForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postID) {
		StagingPost post = new StagingPost();
		post.setId(postID);
		post.setBlog(blog);

		return stagingRepo.save(post);
	}

	/**
	 * Removes a "staged post" from the staging area for a given blog
	 * 
	 * @param blog Used for validation
	 * @param id   The ID of the "staged post" to be removed
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the details
	 */
	@DeleteMapping("/posts/{blog}/{id}")
	public ResponseEntity<?> deleteStagedPostForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long id) {
		StagingPost post = stagingRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("StagedPost", "id", id));

		if (!blog.equals(post.getBlog())) {
			logger.error("Invalid staged post/blog combination; post={}, blog={}", id, blog);
			throw new InvalidConvoParentException();
		}
		stagingRepo.delete(post);

		return ResponseEntity.ok().build();
	}

	/**
	 * Removes all "staged posts" from the staging area for a given blog
	 * 
	 * @param blog Blog for which to remove the staged posts
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the details
	 */
	@Transactional
	@DeleteMapping("/posts/{blog}")
	public ResponseEntity<?> deleteAllStagedPostsForBlog(@PathVariable("blog") String blog) {
		stagingRepo.deleteByBlog(blog);

		return ResponseEntity.ok().build();
	}

	/**
	 * <p>
	 * Exports images for a given post to an external directory.
	 * </p>
	 * 
	 * <p>
	 * First calls the
	 * {@link com.tiyb.tev.controller.TEVPostRestController#fixPhotosForBlogForPost(String, Long)
	 * fixPhotosForPost()} to ensure that the photos for the given post are already
	 * on disk, then copies them to the target directory. Because of this call to
	 * <code>fixPhotosForPost()</code>, there is no sanity check to ensure that the
	 * metadata's images directory is correct; that check is already done by the
	 * other method.
	 * </p>
	 * 
	 * <p>
	 * If a file already exists, the method simply keeps on going to process further
	 * files; no error is thrown (though a warning is logged).
	 * </p>
	 * 
	 * @param blog               Blog to which the post exists
	 * @param postID             The ID of the post for which images should be
	 *                           exported
	 * @param pathForDestination The path to which the images should be copied
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the details
	 */
	@PostMapping("/posts/{blog}/{id}/exportImages")
	public ResponseEntity<?> exportImagesForBlogForPost(@PathVariable("blog") String blog,
			@PathVariable("id") Long postID, @RequestBody String pathForDestination) {
		if (!postController.fixPhotosForBlogForPost(blog, postID)) {
			return new ResponseEntity<String>("Error getting images for post", null, HttpStatus.FAILED_DEPENDENCY);
		}

		String imageDirectory = mdController.getMetadataForBlog(blog).getBaseMediaPath();
		if (imageDirectory.charAt(imageDirectory.length() - 1) != '/') {
			imageDirectory = imageDirectory.concat("/");
		}

		File sourceDirectory = new File(imageDirectory);

		File targetFolder = new File(pathForDestination);
		if (!targetFolder.isDirectory()) {
			logger.error("Invalid target directory passed to export images: {}", pathForDestination);
			return new ResponseEntity<String>(INVALID_TARGET_DIRECTORY, null, HttpStatus.BAD_REQUEST);
		}
		Path destinationPath = targetFolder.toPath();

		File[] imagesForExport = sourceDirectory.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(String.valueOf(postID));
			}
		});

		for (File sourceFile : imagesForExport) {
			Path sourcePath = sourceFile.toPath();
			try {
				Files.copy(sourcePath, destinationPath.resolve(sourcePath.getFileName()));
			} catch (FileAlreadyExistsException e) {
				logger.warn("File already exists: {}", sourceFile.getName());
			} catch (IOException e) {
				logger.error("Error copying file to destination: {}", sourceFile.getName());
				logger.debug("Exception for file error: ", e);
				return new ResponseEntity<String>(ERROR_COPYING_FILE, null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return ResponseEntity.ok().build();
	}
}
