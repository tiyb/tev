package com.tiyb.tev.controller;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
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
 * @apiviz.landmark
 * @apiviz.uses org.springframework.jdbc.core.JdbcTemplate
 * @apiviz.uses com.tiyb.tev.controller.TEVPostRestController
 */
@RestController
@RequestMapping("/admintools")
public class TEVAdminToolsController {

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
	 * Used to compact the database upon shutdown. This causes shutdown to take
	 * longer, but it's not very noticeable for an application of this size.
	 */
	@PreDestroy
	public void preDestroy() {
		try {
			jdbcTemplate.execute("SHUTDOWN COMPACT");
		} catch (DataAccessException e) {
			// do nothing
		}
	}

	/**
	 * GET request to mark all posts in the database as read
	 * 
	 * @return Success indicator
	 */
	@GetMapping("/posts/markAllRead")
	public String markAllPostsRead() {
		List<Post> posts = postController.getAllPosts();

		for (Post post : posts) {
			postController.markPostRead(post.getId());
		}

		return "Success";
	}

	/**
	 * GET request to mark all posts in the database as unread
	 * 
	 * @return Success indicator
	 */
	@GetMapping("/posts/markAllUnread")
	public String markAllPostsUnread() {
		List<Post> posts = postController.getAllPosts();

		for (Post post : posts) {
			postController.markPostUnread(post.getId());
		}

		return "Success";
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
	public String cleanImagesOnHD() {
		String imageDirectory = mdController.getMetadata().getBaseMediaPath();
		if (imageDirectory == null || imageDirectory.equals("")) {
			return "Invalid image directory";
		}

		if (imageDirectory.charAt(imageDirectory.length() - 1) != '/') {
			imageDirectory = imageDirectory + "/";
		}

		List<Post> photoPosts = postController.getPostsByType("photo");
		for (Post post : photoPosts) {
			List<Photo> photosForPost = postController.getPhotoById(post.getId());
			int numPhotos = photosForPost.size();

			File folder = new File(imageDirectory);
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
		}
		return "success";
	}

}
