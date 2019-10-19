package com.tiyb.tev.controller;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;

/**
 * <p>
 * A non-advertised REST controller with some administration tools for working
 * with the database behind the scenes.
 * </p>
 * 
 * <p>
 * If TEV were ever to become anything more than just a local application,
 * proper security would be needed around these operations, but since that's not
 * the case the security is lax. None of these APIs are ever intended to be used
 * by the UI.
 * </p>
 * 
 * @author tiyb
 */
@RestController
@RequestMapping("/admintools")
public class TEVAdminToolsController {

	Logger logger = LoggerFactory.getLogger(TEVAdminToolsController.class);

	/**
	 * The JDBC template to be used for operations within the controller
	 */
	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * The REST controller used for working with posts
	 */
	@Autowired
	private TEVPostRestController postController;

	/**
	 * The REST controller used for working with settings/metadata
	 */
	@Autowired
	private TEVMetadataRestController mdController;
	
	/**
	 * Success message returned from the API. Not actually a "constant," it's
	 * injected at runtime by Spring Boot, but named like one because it's being
	 * treated like one in the code.
	 */
	@Value("${controllers.admintools.success}")
	private String SUCCESS_MESSAGE;
	
	/**
	 * Message returned from the API when the images directory in metadata is
	 * invalid. Not actually a "constant," it's injected at runtime by Spring Boot,
	 * but named like one because it's being treated like one in the code.
	 */
	@Value("${controllers.admintools.invalidImagesDirectory}")
	private String INVALID_IMAGES_MESSAGE;
	
	@Value("${controllers.admintools.invalidSourceImagesDirectory}")
	private String INVALID_SOURCEIMAGES_MESSAGE;
	
	@Value("${controllers.admintools.errorCopyingFiles}")
	private String ERROR_COPYINGFILES_MESSAGE;

	/**
	 * Used to compact the database upon shutdown. This causes shutdown to take
	 * longer, but it's not very noticeable for an application of this size.
	 */
	@PreDestroy
	public void preDestroy() {
		try {
			jdbcTemplate.execute("SHUTDOWN COMPACT");
		} catch (DataAccessException e) {
			logger.error("Error encountered in preDestroy method: ", e);
		}
	}

	/**
	 * GET request to mark all posts in the database as read
	 * 
	 * @return Success indicator
	 */
	@GetMapping("/posts/markAllRead")
	public ResponseEntity<String> markAllPostsRead() {
		List<Post> posts = postController.getAllPosts();

		for (Post post : posts) {
			postController.markPostRead(post.getId());
		}

		return new ResponseEntity<String>(SUCCESS_MESSAGE, null, HttpStatus.OK);
	}

	/**
	 * GET request to mark all posts in the database as unread
	 * 
	 * @return Success indicator
	 */
	@GetMapping("/posts/markAllUnread")
	public ResponseEntity<String> markAllPostsUnread() {
		List<Post> posts = postController.getAllPosts();

		for (Post post : posts) {
			postController.markPostUnread(post.getId());
		}

		return new ResponseEntity<String>(SUCCESS_MESSAGE, null, HttpStatus.OK);
	}

	/**
	 * GET request to delete all of the duplicate images that end up on the hard
	 * drive, coming from the Tumblr export. For photo posts, when the images are
	 * actually in the export, Tumblr includes duplicates of every image, such that
	 * there are twice as many images stored on the HD as necessary. This helper API
	 * retrieves all Photo posts, checks how many images are supposed to be there,
	 * then looks at the HD to see the number of files there. If there are exactly
	 * twice as many images as there should be, the duplicates are deleted.
	 * 
	 * @return String indicating the success or failure of the process
	 */
	@GetMapping("/posts/cleanImagesOnHD")
	public ResponseEntity<String> cleanImagesOnHD() {
		ArrayList<String> allCleanFiles = new ArrayList<String>();
		String imageDirectory = mdController.getMetadata().getBaseMediaPath();
		if (imageDirectory == null || imageDirectory.equals("")) {
			logger.error("Invalid image directory used for cleanImagesOnHD: " + imageDirectory);
			return new ResponseEntity<String>(INVALID_IMAGES_MESSAGE, null, HttpStatus.BAD_REQUEST);
		}

		if (imageDirectory.charAt(imageDirectory.length() - 1) != '/') {
			imageDirectory = imageDirectory + "/";
		}

		File folder = new File(imageDirectory);

		List<Post> photoPosts = postController.getPostsByType("photo");
		for (Post post : photoPosts) {
			List<Photo> photosForPost = postController.getPhotoById(post.getId());
			int numPhotos = photosForPost.size();

			File[] imagesForPost = folder.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					String postID = Long.toString(post.getId());
					return name.startsWith(postID);
				}
			});
			if (imagesForPost.length == (numPhotos * 2)) {
				for (int i = numPhotos; i < imagesForPost.length; i++) {
					imagesForPost[i].delete();
				}
			}
			for (int i = 0; i < imagesForPost.length; i++) {
				allCleanFiles.add(imagesForPost[i].getName());
			}
		}

		File[] remainingImages = folder.listFiles();
		for (int i = 0; i < remainingImages.length; i++) {
			if (!allCleanFiles.contains(remainingImages[i].getName())) {
				remainingImages[i].delete();
			}
		}
		return new ResponseEntity<String>(SUCCESS_MESSAGE, null, HttpStatus.OK);
	}

	/**
	 * Copies all files from a source directory (posted as the body of the request)
	 * to the images directory stored in the metadata. Calls the
	 * {@link #cleanImagesOnHD()} method when it's done, to clean the directory back
	 * up.
	 * 
	 * @param imagePath The path of the source directory, from whence images should
	 *                  be retrieved.
	 * @return Success/failure message
	 */
	@PostMapping("/posts/importImages")
	public ResponseEntity<String> importImages(@RequestBody String imagePath) {
		String imageDirectory = mdController.getMetadata().getBaseMediaPath();
		if (imageDirectory == null || imageDirectory.equals("")) {
			logger.error("Invalid image directory used for importImages: " + imageDirectory);
			return new ResponseEntity<String>(INVALID_IMAGES_MESSAGE, null, HttpStatus.BAD_REQUEST);
		}

		if (imageDirectory.charAt(imageDirectory.length() - 1) != '/') {
			imageDirectory = imageDirectory + "/";
		}

		File destinationFolder = new File(imageDirectory);
		if (!destinationFolder.isDirectory()) {
			logger.error("Images directory in metadata is not a valid directory: " + imageDirectory);
			return new ResponseEntity<String>(INVALID_IMAGES_MESSAGE, null, HttpStatus.BAD_REQUEST);
		}
		Path destinationFolderPath = destinationFolder.toPath();

		File sourceFolder = new File(imagePath);
		if (!sourceFolder.isDirectory()) {
			logger.error("Invalid source directory passed to import images: " + imagePath);
			return new ResponseEntity<String>(INVALID_SOURCEIMAGES_MESSAGE, null, HttpStatus.BAD_REQUEST);
		}

		File[] sourceFiles = sourceFolder.listFiles();
		for (File inputFile : sourceFiles) {
			Path inputFilePath = inputFile.toPath();
			try {
				Files.copy(inputFilePath, destinationFolderPath.resolve(inputFilePath.getFileName()));
			} catch(FileAlreadyExistsException e) {
				logger.debug("File already exists: " + inputFilePath.getFileName());
				// Skip copying files that don't exist
			} catch (IOException e) {
				logger.error("Error copying file from source to destination: " + inputFilePath.getFileName());
				logger.debug("File Copy Error:", e);
				return new ResponseEntity<String>(ERROR_COPYINGFILES_MESSAGE, null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return cleanImagesOnHD();
	}

}
