package com.tiyb.tev.controller;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import com.tiyb.tev.datamodel.Answer;
import com.tiyb.tev.datamodel.Hashtag;
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.datamodel.Video;
import com.tiyb.tev.exception.NoParentPostException;
import com.tiyb.tev.exception.ResourceNotFoundException;
import com.tiyb.tev.repository.AnswerRepository;
import com.tiyb.tev.repository.HashtagRepository;
import com.tiyb.tev.repository.LinkRepository;
import com.tiyb.tev.repository.PhotoRepository;
import com.tiyb.tev.repository.PostRepository;
import com.tiyb.tev.repository.RegularRepository;
import com.tiyb.tev.repository.VideoRepository;

/**
 * <p>
 * This is the REST controller for working with Post data, including special
 * <i>kinds</i> of posts such as "regular" and "photo" posts.
 * </p>
 * 
 * <p>
 * <b>Note:</b> The APIs listed here are all blog-specific, even in cases where
 * the blog name shouldn't be necessary. e.g. retrieving a Post by ID shouldn't
 * require a blog name to be specified (there is no reason to assume IDs are not
 * unique across blogs), but the APIs require blog names even in these cases,
 * for consistency.
 * </p>
 * 
 * @author tiyb
 */
@RestController
@RequestMapping("/api")
public class TEVPostRestController {

	Logger logger = LoggerFactory.getLogger(TEVPostRestController.class);

	/**
	 * Repo for working with Post data
	 */
	@Autowired
	PostRepository postRepo;

	/**
	 * Repo for working with hashtag data
	 */
	@Autowired
	HashtagRepository hashtagRepo;

	/**
	 * Repo for working with "regular" post data
	 */
	@Autowired
	RegularRepository regularRepo;

	/**
	 * Repo for working with answer post data
	 */
	@Autowired
	AnswerRepository answerRepo;

	/**
	 * Repo for working with link post data
	 */
	@Autowired
	LinkRepository linkRepo;

	/**
	 * Repo for working with photo post data
	 */
	@Autowired
	PhotoRepository photoRepo;

	/**
	 * Repo for working with video post data
	 */
	@Autowired
	VideoRepository videoRepo;

	/**
	 * REST controller for working with metadata
	 */
	@Autowired
	private TEVMetadataRestController mdController;

	/**
	 * GET request for listing all posts for a given blog
	 * 
	 * @param blog Blog for which posts should be retrieved
	 * @return {@link java.util.List List} of all posts in the database
	 */
	@GetMapping("/posts/{blog}")
	public List<Post> getAllPostsForBlog(@PathVariable("blog") String blog) {
		return postRepo.findByTumblelog(blog);
	}

	/**
	 * POST request to submit a Tumblr post into the system for a given blog
	 * 
	 * @param blog Name of the blog for which the Post should be inserted
	 * @param post The Post object (in JSON format) to be saved into the database
	 * @return The same {@link com.tiyb.tev.datamodel.Post Post} object that was
	 *         saved (including the ID)
	 */
	@PostMapping("/posts/{blog}")
	public Post createPostForBlog(@PathVariable("blog") String blog, @Valid @RequestBody Post post) {
		assert blog.equals(post.getTumblelog());
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
	public Post getPostForBlogById(@PathVariable("blog") String blog, @PathVariable("id") Long postId) {
		return postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
	}

	/**
	 * PUT to update a Post for a given blog
	 * 
	 * @param blog        The blog for which this post should be updated (not used,
	 *                    but needs to be consistent with blog specified in Post)
	 * @param postId      The ID of the post to be updated
	 * @param postDetails The data to be updated
	 * @return The same {@link com.tiyb.tev.datamodel.Post Post} object that was
	 *         submitted
	 */
	@PutMapping("/posts/{blog}/{id}")
	public Post updatePostForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postId,
			@RequestBody Post postDetails) {
		Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

		assert blog.equals(post.getTumblelog());

		post.updateData(postDetails);

		Post updatedPost = postRepo.save(post);

		return updatedPost;
	}

	/**
	 * PUT API for marking a post read for a given blog
	 * 
	 * @param blog   Blog for which this post should be marked read (must be
	 *               consistent with the post from the DB)
	 * @param postId The ID of the post to be marked read
	 * @return The modified Post
	 */
	@PutMapping("/posts/{blog}/{id}/markRead")
	public Post markPostReadForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postId) {
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
	public Post markPostFavouriteForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postId) {
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
	public Post markPostUnreadForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postId) {
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
	public Post markPostNonFavouriteForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postId) {
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
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/{blog}/{id}")
	public ResponseEntity<?> deletePostForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postId) {
		Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

		assert blog.equals(post.getTumblelog());

		postRepo.delete(post);

		return ResponseEntity.ok().build();
	}

	/**
	 * DEL to delete all posts in the DB for a given blog
	 * 
	 * @param blog Blog for which posts should be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity<>} with
	 *         the response details
	 */
	@Transactional
	@DeleteMapping("/posts/{blog}")
	public ResponseEntity<?> deleteAllPostsForBlog(@PathVariable("blog") String blog) {
		postRepo.deleteByTumblelog(blog);

		return ResponseEntity.ok().build();
	}

	/**
	 * The Tumblr export doesn't always include every image, for some reason.
	 * However, in many cases the images referred to in the image URLs from Tumblr's
	 * export XML still exist on Tumblr's servers. So, in cases where a post is
	 * missing its images, this API can be used to fetch the images from Tumblr's
	 * servers (according to their URLs in the XML), download them, and save them to
	 * the media directory being used by TEV, using the naming convention Tumblr
	 * would have used, if the images had been included in the export.
	 * 
	 * TODO move to admin tools?
	 * 
	 * @param blog   Not used
	 * @param postId The ID of the post to be "fixed"
	 * @return bool indicating whether the action completed successfully or not.
	 */
	@GetMapping("/posts/{blog}/{id}/fixPhotos")
	public Boolean fixPhotosForBlogForPost(@PathVariable("blog") String blog, @PathVariable("id") Long postId) {
		String imageDirectory = mdController.getMetadataForBlogOrDefault(blog).getBaseMediaPath();
		if (imageDirectory == null || imageDirectory.equals("")) {
			return false;
		}

		if (imageDirectory.charAt(imageDirectory.length() - 1) != '/') {
			imageDirectory = imageDirectory + "/";
		}

		List<Photo> photos = photoRepo.findByPostIdOrderByOffset(postId);
		boolean response = true;

		for (int i = 0; i < photos.size(); i++) {
			Photo photo = photos.get(i);
			String url = photo.getUrl1280();
			String ext = url.substring(url.lastIndexOf('.'));
			try {
				BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
				FileOutputStream out = new FileOutputStream(imageDirectory + photo.getPostId() + "_" + i + ext);
				byte dataBuffer[] = new byte[1024];
				int bytesRead;
				while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
					out.write(dataBuffer, 0, bytesRead);
				}
				out.close();
			} catch (Exception e) {
				response = false;
			}
		}
		return response;
	}

	/**
	 * GET request for listing all hashtags stored in the system for a given blog
	 * 
	 * @param blog Blog for which hashtags should be returned
	 * @return {@link java.util.List List} of all hashtags in the database
	 */
	@GetMapping("/hashtags/{blog}")
	public List<Hashtag> getAllHashtagsForBlog(@PathVariable("blog") String blog) {
		return hashtagRepo.findByBlog(blog);
	}

	/**
	 * POST request to insert a new hashtag into the system for a given blog. If it
	 * already exists the existing hashtag is simply returned (no error is thrown).
	 * 
	 * @param blog    Blog for which the hashtag should be inserted
	 * @param hashtag The hashtag to be entered into the system
	 * @return The new/existing hashtag object (with ID)
	 */
	@PostMapping("/hashtags/{blog}")
	public Hashtag createHashtagForBlog(@PathVariable("blog") String blog, @Valid @RequestBody String hashtag) {
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
	 * DEL to delete all hashtags in the DB for a given blog
	 * 
	 * @param blog Blog for which tags should be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity<>} with
	 *         the response details
	 */
	@Transactional
	@DeleteMapping("/hashtags/{blog}")
	public ResponseEntity<?> deleteAllHashtagsForBlog(@PathVariable("blog") String blog) {
		hashtagRepo.deleteByBlog(blog);

		return ResponseEntity.ok().build();
	}

	/**
	 * GET request for listing all answers for a given blog
	 * 
	 * @param blog Blog for which answers should be returned
	 * @return {@link java.util.List List} of all answers in the database
	 */
	@GetMapping("/posts/{blog}/answers")
	public List<Answer> getAllAnswersForBlog(@PathVariable("blog") String blog) {
		List<Post> posts = postRepo.findByTumblelogAndType(blog, "answer");
		List<Answer> answers = new ArrayList<Answer>();

		for (Post post : posts) {
			Optional<Answer> answerResponse = answerRepo.findById(post.getId());
			if (answerResponse.isPresent()) {
				answers.add(answerResponse.get());
			}
		}

		return answers;
	}

	/**
	 * POST request to submit a Tumblr "answer" into the system for a given blog
	 * 
	 * @param blog   Validated against content
	 * @param postId The ID of the post to which this answer refers
	 * @param answer The data to be submitted
	 * @return The same {@link com.tiyb.tev.datamodel.Answer Answer} object that was
	 *         submitted
	 */
	@PostMapping("/posts/{blog}/{id}/answer")
	public Answer createAnswerForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postId,
			@Valid @RequestBody Answer answer) {
		Optional<Post> post = postRepo.findById(postId);
		if (!post.isPresent()) {
			logger.error("Tried to submit answer for a post that doesn't exist: " + postId);
			throw new NoParentPostException();
		} else {
			assert blog.equals(post.get().getTumblelog());
		}
		answer.setPostId(postId);
		return answerRepo.save(answer);
	}

	/**
	 * GET to return a single answer, by ID
	 * 
	 * @param blog   not used
	 * @param postId The Post ID
	 * @return The {@link com.tiyb.tev.datamodel.Answer Answer} details
	 */
	@GetMapping("/posts/{blog}/{id}/answer")
	public Answer getAnswerForBlogById(@PathVariable("blog") String blog, @PathVariable("id") Long postId) {
		return answerRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Answer", "id", postId));
	}

	/**
	 * PUT to update an Answer
	 * 
	 * @param blog          Not used
	 * @param postId        The ID of the post to be updated
	 * @param answerDetails The data to be updated
	 * @return The same {@link com.tiyb.tev.datamodel.Answer Answer} object that was
	 *         submitted
	 */
	@PutMapping("/posts/{blog}/{id}/answer")
	public Answer updateAnswerForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postId,
			@RequestBody Answer answerDetails) {
		Answer ans = answerRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Answer", "id", postId));

		ans.updateData(answerDetails);

		Answer updatedAns = answerRepo.save(ans);

		return updatedAns;
	}

	/**
	 * DEL to delete all "answer" posts in the DB for a given blog
	 * 
	 * @param blog Blog for which answers should be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/{blog}/answers")
	public ResponseEntity<?> deleteAllAnswersForBlog(@PathVariable("blog") String blog) {
		List<Post> posts = postRepo.findByTumblelogAndType(blog, "answer");

		for (Post post : posts) {
			answerRepo.deleteById(post.getId());
		}

		return ResponseEntity.ok().build();
	}

	/**
	 * DEL to delete a single answer by ID for a given blog
	 * 
	 * @param blog   Not used
	 * @param postId the ID of the post to be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/{blog}/{id}/answer")
	public ResponseEntity<?> deleteAnswerForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postId) {
		Answer ans = answerRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Answer", "id", postId));

		answerRepo.delete(ans);

		return ResponseEntity.ok().build();
	}

	/**
	 * GET request for listing all links for a given blog
	 * 
	 * @param blog Blog for which to retrieve links
	 * @return {@link java.util.List List} of all links in the database
	 */
	@GetMapping("/posts/{blog}/links")
	public List<Link> getAllLinksForBlog(@PathVariable("blog") String blog) {
		List<Post> posts = postRepo.findByTumblelogAndType(blog, "link");
		List<Link> links = new ArrayList<Link>();

		for (Post post : posts) {
			Optional<Link> linkResponse = linkRepo.findById(post.getId());
			if (linkResponse.isPresent()) {
				links.add(linkResponse.get());
			}
		}

		return links;
	}

	/**
	 * POST request to submit a Tumblr "link" into the system for a given blog
	 * 
	 * @param blog   Used for validation purposes
	 * @param postId The ID of the post to which this link refers
	 * @param link   The data to be submitted
	 * @return The same {@link com.tiyb.tev.datamodel.Link Link} object that was
	 *         submitted
	 */
	@PostMapping("/posts/{blog}/{id}/link")
	public Link createLinkForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postId,
			@Valid @RequestBody Link link) {
		Optional<Post> post = postRepo.findById(postId);
		if (!post.isPresent()) {
			logger.error("Tried to submit link for a post that doesn't exist: " + postId);
			throw new NoParentPostException();
		} else {
			assert blog.equals(post.get().getTumblelog());
		}
		link.setPostId(postId);
		return linkRepo.save(link);
	}

	/**
	 * GET to return a single link by ID for a given blog
	 * 
	 * @param blog   not used
	 * @param postId The Post ID
	 * @return The {@link com.tiyb.tev.datamodel.Link Link} details
	 */
	@GetMapping("/posts/{blog}/{id}/link")
	public Link getLinkForBlogById(@PathVariable("blog") String blog, @PathVariable("id") Long postId) {
		return linkRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Link", "id", postId));
	}

	/**
	 * PUT to update a Link for a given blog
	 * 
	 * @param blog        Not used
	 * @param postId      The ID of the post to be updated
	 * @param linkDetails The data to be updated
	 * @return The same {@link com.tiyb.tev.datamodel.Link Link} object that was
	 *         submitted
	 */
	@PutMapping("/posts/{blog}/{id}/link")
	public Link updateLinkForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postId,
			@RequestBody Link linkDetails) {
		Link link = linkRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Link", "id", postId));

		link.updateData(linkDetails);

		Link updatedLink = linkRepo.save(link);

		return updatedLink;
	}

	/**
	 * DEL to delete all "link" posts in the DB for a given blog
	 * 
	 * @param blog Blog for which to delete the links
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/{blog}/links")
	public ResponseEntity<?> deleteAllLinksForBlog(@PathVariable("blog") String blog) {
		List<Post> posts = postRepo.findByTumblelogAndType(blog, "link");

		for (Post post : posts) {
			linkRepo.deleteById(post.getId());
		}

		return ResponseEntity.ok().build();
	}

	/**
	 * DEL to delete a single link by ID for a given blog
	 * 
	 * @param blog   Not used
	 * @param postId the ID of the post to be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/{blog}/{id}/link")
	public ResponseEntity<?> deleteLinkForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postId) {
		Link link = linkRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Link", "id", postId));

		linkRepo.delete(link);

		return ResponseEntity.ok().build();
	}

	/**
	 * GET request for listing all photos for a given blog
	 * 
	 * @param blog Blog for which photos should be retrieved
	 * @return {@link java.util.List List} of all photos in the database
	 */
	@GetMapping("/posts/{blog}/photos")
	public List<Photo> getAllPhotosForBlog(@PathVariable("blog") String blog) {
		List<Post> posts = postRepo.findByTumblelogAndType(blog, "photo");
		List<Photo> photos = new ArrayList<Photo>();

		for (Post post : posts) {
			Optional<Photo> photoResponse = photoRepo.findById(post.getId());
			if (photoResponse.isPresent()) {
				photos.add(photoResponse.get());
			}
		}

		return photos;
	}

	/**
	 * POST request to submit a Tumblr "photo post" into the system for a given blog
	 * 
	 * @param blog  For validation purposes
	 * @param photo The data to be submitted
	 * @return The same {@link com.tiyb.tev.datamodel.Photo Photo} object that was
	 *         submitted
	 */
	@PostMapping("/posts/{blog}/photo")
	public Photo createPhotoForBlog(@PathVariable("blog") String blog, @Valid @RequestBody Photo photo) {
		Optional<Post> post = postRepo.findById(photo.getPostId());
		if (!post.isPresent()) {
			logger.error("Tried to submit link for a post that doesn't exist: " + photo.getPostId());
			throw new NoParentPostException();
		} else {
			assert blog.equals(post.get().getTumblelog());
		}
		return photoRepo.save(photo);
	}

	/**
	 * GET to return a single Photo post by ID for a given blog
	 * 
	 * @param blog   Not used
	 * @param postId The Post ID
	 * @return The {@link com.tiyb.tev.datamodel.Photo Photo} details
	 */
	@GetMapping("/posts/{blog}/{id}/photo")
	public List<Photo> getPhotoForBlogById(@PathVariable("blog") String blog, @PathVariable("id") Long postId) {
		return photoRepo.findByPostId(postId);
	}

	/**
	 * PUT to update a Photo
	 * 
	 * @param blog         not used
	 * @param postId       The ID of the post to be updated
	 * @param photoDetails The data to be updated
	 * @return The same {@link com.tiyb.tev.datamodel.Photo Photo} object that was
	 *         submitted
	 */
	@PutMapping("/posts/{blog}/{id}/photo")
	public Photo updatePhotoForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postId,
			@RequestBody Photo photoDetails) {
		Photo photo = photoRepo.findById(photoDetails.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Photo", "id", postId));

		photo.updateData(photoDetails);

		Photo updatedPhoto = photoRepo.save(photo);

		return updatedPhoto;
	}

	/**
	 * DEL to delete all "photo" posts in the DB for a given blog. Because a given
	 * Photo post can actually have multiple photos in it, the logic is to retrieve
	 * all photo posts for the given blog, then delete each photo for that post.
	 * 
	 * @param blog Blog for which photos should be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/{blog}/photos")
	public ResponseEntity<?> deleteAllPhotosForBlog(@PathVariable("blog") String blog) {
		List<Post> posts = postRepo.findByTumblelogAndType(blog, "photo");

		for (Post post : posts) {
			List<Photo> photos = photoRepo.findByPostId(post.getId());
			for (Photo photo : photos) {
				photoRepo.delete(photo);
			}
		}

		return ResponseEntity.ok().build();
	}

	/**
	 * DEL to delete a single photo by ID for a given blog
	 * 
	 * @param blog   Not used
	 * @param postId the ID of the post to be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/{blog}/{id}/photo")
	public ResponseEntity<?> deletePhotoForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postId) {
		Photo photo = photoRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Photo", "id", postId));

		photoRepo.delete(photo);

		return ResponseEntity.ok().build();
	}

	/**
	 * GET request for listing all regular posts for a given blog
	 * 
	 * @param blog Blog for which posts should be retrieved
	 * @return {@link java.util.List List} of all regular posts in the database
	 */
	@GetMapping("/posts/{blog}/regulars")
	public List<Regular> getAllRegularsForBlog(@PathVariable("blog") String blog) {
		List<Post> posts = postRepo.findByTumblelogAndType(blog, "regular");
		List<Regular> regulars = new ArrayList<Regular>();

		for (Post post : posts) {
			Optional<Regular> regResponse = regularRepo.findById(post.getId());
			if (regResponse.isPresent()) {
				regulars.add(regResponse.get());
			}
		}

		return regulars;
	}

	/**
	 * POST request to submit a "regular" post into the system. (The naming is
	 * awkward, but "regular" is one type of Tumblr post.)
	 * 
	 * @param blog    Used for validation purposes
	 * @param postId  The ID of the post to which this "regular" post content refers
	 * @param regular The data to be submitted
	 * @return The same {@link com.tiyb.tev.datamodel.Regular Regular} object that
	 *         was submitted.
	 */
	@PostMapping("/posts/{blog}/{id}/regular")
	public Regular createRegularForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postId,
			@Valid @RequestBody Regular regular) {
		Optional<Post> post = postRepo.findById(postId);
		if (!post.isPresent()) {
			logger.error("Tried to submit regular for a post that doesn't exist: " + postId);
			throw new NoParentPostException();
		} else {
			assert blog.equals(post.get().getTumblelog());
		}

		regular.setPostId(postId);
		return regularRepo.save(regular);
	}

	/**
	 * GET to return a single regular post by ID for a given blog
	 * 
	 * @param blog   Not used
	 * @param postId The Post ID
	 * @return The {@link com.tiyb.tev.datamodel.Regular Regular} details
	 */
	@GetMapping("/posts/{blog}/{id}/regular")
	public Regular getRegularForBlogById(@PathVariable("blog") String blog, @PathVariable("id") Long postId) {
		return regularRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Regular", "id", postId));
	}

	/**
	 * PUT to update a "Regular"
	 * 
	 * @param blog           Not used
	 * @param postId         The ID of the post to be updated
	 * @param regularDetails The data to be updated
	 * @return The same {@link com.tiyb.tev.datamodel.Regular Regular} object that
	 *         was submitted
	 */
	@PutMapping("/posts/{blog}/{id}/regular")
	public Regular updateRegularForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postId,
			@RequestBody Regular regularDetails) {
		Regular reg = regularRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Regular", "id", postId));

		reg.updateData(regularDetails);

		Regular updatedReg = regularRepo.save(reg);

		return updatedReg;
	}

	/**
	 * DEL to delete all "regular" posts in the DB for a given blog
	 * 
	 * @param blog Blog for which posts should be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/{blog}/regulars")
	public ResponseEntity<?> deleteAllRegularsForBlog(@PathVariable("blog") String blog) {
		List<Post> posts = postRepo.findByTumblelogAndType(blog, "regular");

		for (Post post : posts) {
			Optional<Regular> response = regularRepo.findById(post.getId());
			if (!response.isPresent()) {
				logger.error("Attempting to delete a regular that doesn't exist: " + post.getId());
				return ResponseEntity.badRequest().build();
			}

			regularRepo.delete(response.get());
		}

		return ResponseEntity.ok().build();
	}

	/**
	 * DEL to delete a single regular post by ID for a given blog
	 * 
	 * @param blog   Not used
	 * @param postId the ID of the post to be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/{id}/regular")
	public ResponseEntity<?> deleteRegularForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postId) {
		Regular reg = regularRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

		regularRepo.delete(reg);

		return ResponseEntity.ok().build();
	}

	/**
	 * GET request for listing all videos for a given blog
	 * 
	 * @param blog Blog for which to return the video posts
	 * @return {@link java.util.List List} of all videos in the database
	 */
	@GetMapping("/posts/{blog}/videos")
	public List<Video> getAllVideosForBlog(@PathVariable("blog") String blog) {
		List<Post> posts = postRepo.findByTumblelogAndType(blog, "video");
		List<Video> videos = new ArrayList<Video>();

		for (Post post : posts) {
			Optional<Video> vidResponse = videoRepo.findById(post.getId());
			if (vidResponse.isPresent()) {
				videos.add(vidResponse.get());
			}
		}

		return videos;
	}

	/**
	 * POST request to submit a Tumblr "video post" into the system for a given blog
	 * 
	 * @param blog   The blog for which this video is being created
	 * @param postId The ID of the post to which this video content refers
	 * @param video  The data to be submitted
	 * @return The same {@link com.tiyb.tev.datamodel.Video Video} object that was
	 *         submitted
	 */
	@PostMapping("/posts/{blog}/{id}/video")
	public Video createVideoForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postId,
			@Valid @RequestBody Video video) {
		Optional<Post> post = postRepo.findById(postId);
		if (!post.isPresent()) {
			logger.error("Tried to submit a video for a post that doesn't exist: " + postId);
			throw new NoParentPostException();
		} else {
			assert blog.equals(post.get().getTumblelog());
		}

		video.setPostId(postId);
		return videoRepo.save(video);
	}

	/**
	 * GET to return a single video post by ID for a given blog
	 * 
	 * @param blog   Not used
	 * @param postId The Post ID
	 * @return The {@link com.tiyb.tev.datamodel.Video Video} details
	 */
	@GetMapping("/posts/{blog}/{id}/video")
	public Video getVideoForBlogById(@PathVariable("blog") String blog, @PathVariable("id") Long postId) {
		return videoRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Video", "id", postId));
	}

	/**
	 * PUT to update a Video
	 * 
	 * @param blog         Not used
	 * @param postId       The ID of the post to be updated
	 * @param videoDetails The data to be updated
	 * @return The same {@link com.tiyb.tev.datamodel.Video Video} object that was
	 *         submitted
	 */
	@PutMapping("/posts/{blog}/{id}/video")
	public Video updateVideoForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postId,
			@RequestBody Video videoDetails) {
		Video video = videoRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Video", "id", postId));

		video.updateData(videoDetails);

		Video updatedVideo = videoRepo.save(video);

		return updatedVideo;
	}

	/**
	 * DEL to delete all "video" posts in the DB for a given blog
	 * 
	 * @param blog Blog for which videos should be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/{blog}/videos")
	public ResponseEntity<?> deleteAllVideosForBlog(@PathVariable("blog") String blog) {
		List<Post> posts = postRepo.findByTumblelogAndType(blog, "video");

		for (Post post : posts) {
			Optional<Video> response = videoRepo.findById(post.getId());
			if (!response.isPresent()) {
				logger.error("Attempting to delete a video that doesn't exist: " + post.getId());
				return ResponseEntity.badRequest().build();
			}

			videoRepo.delete(response.get());
		}

		return ResponseEntity.ok().build();
	}

	/**
	 * DEL to delete a single video by ID for a given blog
	 * 
	 * @param blog   Not used
	 * @param postId the ID of the post to be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/{blog}/{id}/video")
	public ResponseEntity<?> deleteVideoForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long postId) {
		Video video = videoRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Video", "id", postId));

		videoRepo.delete(video);

		return ResponseEntity.ok().build();
	}

}
