package com.tiyb.tev.controller;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.List;

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

import com.tiyb.tev.datamodel.Answer;
import com.tiyb.tev.datamodel.Hashtag;
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.datamodel.Video;
import com.tiyb.tev.exception.InvalidTypeException;
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
 * @author tiyb
 * @apiviz.landmark
 * @apiviz.uses com.tiyb.tev.repository.AnswerRepository
 * @apiviz.uses com.tiyb.tev.repository.LinkRepository
 * @apiviz.uses com.tiyb.tev.repository.PhotoRepository
 * @apiviz.uses com.tiyb.tev.repository.PostRepository
 * @apiviz.uses com.tiyb.tev.repository.RegularRepository
 * @apiviz.uses com.tiyb.tev.repository.VideoRepository
 *
 */
@RestController
@RequestMapping("/api")
public class TEVPostRestController {

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
	 * GET request for listing all posts
	 * 
	 * @return {@link java.util.List List} of all posts in the database
	 */
	@GetMapping("/posts")
	public List<Post> getAllPosts() {
		return postRepo.findAll();
	}

	/**
	 * GET request for listing all hashtags stored in the system
	 * 
	 * @return {@link java.util.List List} of all hashtags in the database
	 */
	@GetMapping("/hashtags")
	public List<Hashtag> getAllHashtags() {
		return hashtagRepo.findAll();
	}

	/**
	 * POST request to submit a Tumblr post into the system
	 * 
	 * @param post The Post object (in JSON format) to be saved into the database
	 * @return The same {@link com.tiyb.tev.datamodel.Post Post} object that was
	 *         saved (including the ID)
	 */
	@PostMapping("/posts")
	public Post createPost(@Valid @RequestBody Post post) {
		return postRepo.save(post);
	}

	/**
	 * POST request to insert a new hashtag into the system. If it already exists
	 * the existing hashtag is simply returned (no error is thrown).
	 * 
	 * @param hashtag The hashtag to be entered into the system
	 * @return The new/existing hashtag object (with ID)
	 */
	@PostMapping("/hashtags")
	public Hashtag createHashtag(@Valid @RequestBody String hashtag) {
		Hashtag existingTag = hashtagRepo.findByTag(hashtag);

		if (existingTag != null) {
			existingTag.setCount(existingTag.getCount() + 1);
			existingTag = hashtagRepo.save(existingTag);
			return existingTag;
		}

		Hashtag newTag = new Hashtag();
		newTag.setTag(hashtag);
		newTag.setCount(1);

		newTag = hashtagRepo.save(newTag);
		return newTag;
	}

	/**
	 * GET to return a single post, by ID
	 * 
	 * @param postId The Post ID
	 * @return The {@link com.tiyb.tev.datamodel.Post Post} details
	 */
	@GetMapping("/posts/{id}")
	public Post getPostById(@PathVariable(value = "id") Long postId) {
		return postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
	}

	/**
	 * Returns all posts of a particular type
	 * 
	 * @param postType One of the 5 post types supported by Tumblr/TEV
	 * @return List of posts of that type
	 */
	@GetMapping("/posts/type/{type}")
	public List<Post> getPostsByType(@PathVariable(value = "type") String postType) {
		List<String> allTypeNames = mdController.getAllTypes();

		if (!isValidType(postType, allTypeNames)) {
			throw new InvalidTypeException();
		}

		return postRepo.findByType(postType);
	}

	/**
	 * Helper function to determine if a type (based on String) is one of the valid
	 * Tumblr post types
	 * 
	 * @param typeName       String containing the type
	 * @param validTypeNames {@link java.util.List List} of all valid type names
	 *                       (would be retrieved from the Metadata REST controller)
	 * @return True if it's one of the 5 valid types; false otherwise
	 */
	private boolean isValidType(String typeName, List<String> validTypeNames) {
		for (String validType : validTypeNames) {
			if (typeName.equals(validType)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * PUT to update a Post
	 * 
	 * @param postId      The ID of the post to be updated
	 * @param postDetails The data to be updated
	 * @return The same {@link com.tiyb.tev.datamodel.Post Post} object that was
	 *         submitted
	 */
	@PutMapping("/posts/{id}")
	public Post updatePost(@PathVariable(value = "id") Long postId, @RequestBody Post postDetails) {
		Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

		post.updateData(postDetails);

		Post updatedPost = postRepo.save(post);

		return updatedPost;
	}

	/**
	 * PUT API for marking a post read
	 * 
	 * @param postId The ID of the post to be marked read
	 * @return The modified Post
	 */
	@PutMapping("/posts/{id}/markRead")
	public Post markPostRead(@PathVariable(value = "id") Long postId) {
		Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

		post.setIsRead(true);

		post = postRepo.save(post);

		return post;
	}

	/**
	 * PUT API to mark a post as a favourite
	 * 
	 * @param postId The ID of the post to be marked as a favourite
	 * @return The modified Post
	 */
	@PutMapping("/posts/{id}/markFavourite")
	public Post markPostFavourite(@PathVariable(value = "id") Long postId) {
		Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

		post.setIsFavourite(true);

		post = postRepo.save(post);

		return post;
	}

	/**
	 * PUT API for marking a post unread
	 * 
	 * @param postId The ID of the post to be marked unread
	 * @return The modified Post
	 */
	@PutMapping("/posts/{id}/markUnread")
	public Post markPostUnread(@PathVariable(value = "id") Long postId) {
		Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

		post.setIsRead(false);

		post = postRepo.save(post);

		return post;
	}

	/**
	 * PUT API for marking a post as not a favourite
	 * 
	 * @param postId The ID of the post to be marked as not a favourite
	 * @return The modified Post
	 */
	@PutMapping("/posts/{id}/markNonFavourite")
	public Post markPostNonFavourite(@PathVariable(value = "id") Long postId) {
		Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

		post.setIsFavourite(false);

		post = postRepo.save(post);

		return post;
	}

	/**
	 * DEL to delete a single post, by ID
	 * 
	 * @param postId the ID of the post to be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/{id}")
	public ResponseEntity<?> deletePost(@PathVariable(value = "id") Long postId) {
		Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

		postRepo.delete(post);

		return ResponseEntity.ok().build();
	}

	/**
	 * DEL to delete all posts in the DB
	 * 
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts")
	public ResponseEntity<?> deleteAllPosts() {
		postRepo.deleteAll();

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
	 * @param postId The ID of the post to be "fixed"
	 * @return bool indicating whether the action completed successfully or not.
	 */
	@GetMapping("/posts/{id}/fixPhotos")
	public Boolean fixPhotosForPost(@PathVariable(value = "id") Long postId) {
		String imageDirectory = mdController.getMetadata().getBaseMediaPath();
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
	 * GET request for listing all answers
	 * 
	 * @return {@link java.util.List List} of all answers in the database
	 */
	@GetMapping("/posts/answers")
	public List<Answer> getAllAnswers() {
		return answerRepo.findAll();
	}

	/**
	 * POST request to submit a Tumblr "answer" into the system
	 * 
	 * @param postId The ID of the post to which this answer refers
	 * @param answer The data to be submitted
	 * @return The same {@link com.tiyb.tev.datamodel.Answer Answer} object that was
	 *         submitted
	 */
	@PostMapping("/posts/{id}/answer")
	public Answer createAnswer(@PathVariable(value = "id") Long postId, @Valid @RequestBody Answer answer) {
		answer.setPostId(postId);
		return answerRepo.save(answer);
	}

	/**
	 * GET to return a single answer, by ID
	 * 
	 * @param postId The Post ID
	 * @return The {@link com.tiyb.tev.datamodel.Answer Answer} details
	 */
	@GetMapping("/posts/{id}/answer")
	public Answer getAnswerById(@PathVariable(value = "id") Long postId) {
		return answerRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Answer", "id", postId));
	}

	/**
	 * PUT to update an Answer
	 * 
	 * @param postId        The ID of the post to be updated
	 * @param answerDetails The data to be updated
	 * @return The same {@link com.tiyb.tev.datamodel.Answer Answer} object that was
	 *         submitted
	 */
	@PutMapping("/posts/{id}/answer")
	public Answer updateAnswer(@PathVariable(value = "id") Long postId, @RequestBody Answer answerDetails) {
		Answer ans = answerRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Answer", "id", postId));

		ans.updateData(answerDetails);

		Answer updatedAns = answerRepo.save(ans);

		return updatedAns;
	}

	/**
	 * DEL to delete all "answer" posts in the DB
	 * 
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/answers")
	public ResponseEntity<?> deleteAllAnswers() {
		answerRepo.deleteAll();

		return ResponseEntity.ok().build();
	}

	/**
	 * DEL to delete a single answer, by ID
	 * 
	 * @param postId the ID of the post to be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/{id}/answer")
	public ResponseEntity<?> deleteAnswer(@PathVariable(value = "id") Long postId) {
		Answer ans = answerRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Answer", "id", postId));

		answerRepo.delete(ans);

		return ResponseEntity.ok().build();
	}

	/**
	 * GET request for listing all links
	 * 
	 * @return {@link java.util.List List} of all links in the database
	 */
	@GetMapping("/posts/links")
	public List<Link> getAllLinks() {
		return linkRepo.findAll();
	}

	/**
	 * POST request to submit a Tumblr "link" into the system
	 * 
	 * @param postId The ID of the post to which this link refers
	 * @param link   The data to be submitted
	 * @return The same {@link com.tiyb.tev.datamodel.Link Link} object that was
	 *         submitted
	 */
	@PostMapping("/posts/{id}/link")
	public Link createLink(@PathVariable(value = "id") Long postId, @Valid @RequestBody Link link) {
		link.setPostId(postId);
		return linkRepo.save(link);
	}

	/**
	 * GET to return a single link, by ID
	 * 
	 * @param postId The Post ID
	 * @return The {@link com.tiyb.tev.datamodel.Link Link} details
	 */
	@GetMapping("/posts/{id}/link")
	public Link getLinkById(@PathVariable(value = "id") Long postId) {
		return linkRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Link", "id", postId));
	}

	/**
	 * PUT to update a Link
	 * 
	 * @param postId      The ID of the post to be updated
	 * @param linkDetails The data to be updated
	 * @return The same {@link com.tiyb.tev.datamodel.Link Link} object that was
	 *         submitted
	 */
	@PutMapping("/posts/{id}/link")
	public Link updateLink(@PathVariable(value = "id") Long postId, @RequestBody Link linkDetails) {
		Link link = linkRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Link", "id", postId));

		link.updateData(linkDetails);

		Link updatedLink = linkRepo.save(link);

		return updatedLink;
	}

	/**
	 * DEL to delete all "link" posts in the DB
	 * 
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/links")
	public ResponseEntity<?> deleteAllLinks() {
		linkRepo.deleteAll();

		return ResponseEntity.ok().build();
	}

	/**
	 * DEL to delete a single link, by ID
	 * 
	 * @param postId the ID of the post to be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/{id}/link")
	public ResponseEntity<?> deleteLink(@PathVariable(value = "id") Long postId) {
		Link link = linkRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Link", "id", postId));

		linkRepo.delete(link);

		return ResponseEntity.ok().build();
	}

	/**
	 * GET request for listing all photos
	 * 
	 * @return {@link java.util.List List} of all photos in the database
	 */
	@GetMapping("/posts/photos")
	public List<Photo> getAllPhotos() {
		return photoRepo.findAll();
	}

	/**
	 * POST request to submit a Tumblr "photo post" into the system
	 * 
	 * @param photo The data to be submitted
	 * @return The same {@link com.tiyb.tev.datamodel.Photo Photo} object that was
	 *         submitted
	 */
	@PostMapping("/posts/photo")
	public Photo createPhoto(@Valid @RequestBody Photo photo) {
		return photoRepo.save(photo);
	}

	/**
	 * GET to return a single Photo post, by ID
	 * 
	 * @param postId The Post ID
	 * @return The {@link com.tiyb.tev.datamodel.Photo Photo} details
	 */
	@GetMapping("/posts/{id}/photo")
	public List<Photo> getPhotoById(@PathVariable(value = "id") Long postId) {
		return photoRepo.findByPostIdOrderByOffset(postId);
	}

	/**
	 * PUT to update a Photo
	 * 
	 * @param postId       The ID of the post to be updated
	 * @param photoDetails The data to be updated
	 * @return The same {@link com.tiyb.tev.datamodel.Photo Photo} object that was
	 *         submitted
	 */
	@PutMapping("/posts/{id}/photo")
	public Photo updatePhoto(@PathVariable(value = "id") Long postId, @RequestBody Photo photoDetails) {
		Photo photo = photoRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Photo", "id", postId));

		photo.updateData(photoDetails);

		Photo updatedPhoto = photoRepo.save(photo);

		return updatedPhoto;
	}

	/**
	 * DEL to delete all "photo" posts in the DB
	 * 
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/photos")
	public ResponseEntity<?> deleteAllPhotos() {
		photoRepo.deleteAll();

		return ResponseEntity.ok().build();
	}

	/**
	 * DEL to delete a single photo, by ID
	 * 
	 * @param postId the ID of the post to be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/{id}/photo")
	public ResponseEntity<?> deletePhoto(@PathVariable(value = "id") Long postId) {
		Photo photo = photoRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Photo", "id", postId));

		photoRepo.delete(photo);

		return ResponseEntity.ok().build();
	}

	/**
	 * GET request for listing all regular posts
	 * 
	 * @return {@link java.util.List List} of all regular posts in the database
	 */
	@GetMapping("/posts/regulars")
	public List<Regular> getAllRegulars() {
		return regularRepo.findAll();
	}

	/**
	 * POST request to submit a "regular" post into the system. (The naming is
	 * awkward, but "regular" is one type of Tumblr post.)
	 * 
	 * @param postId  The ID of the post to which this "regular" post content refers
	 * @param regular The data to be submitted
	 * @return The same {@link com.tiyb.tev.datamodel.Regular Regular} object that
	 *         was submitted.
	 */
	@PostMapping("/posts/{id}/regular")
	public Regular createRegular(@PathVariable(value = "id") Long postId, @Valid @RequestBody Regular regular) {
		regular.setPostId(postId);
		return regularRepo.save(regular);
	}

	/**
	 * GET to return a single regular post, by ID
	 * 
	 * @param postId The Post ID
	 * @return The {@link com.tiyb.tev.datamodel.Regular Regular} details
	 */
	@GetMapping("/posts/{id}/regular")
	public Regular getRegularById(@PathVariable(value = "id") Long postId) {
		return regularRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Regular", "id", postId));
	}

	/**
	 * PUT to update a "Regular"
	 * 
	 * @param postId         The ID of the post to be updated
	 * @param regularDetails The data to be updated
	 * @return The same {@link com.tiyb.tev.datamodel.Regular Regular} object that
	 *         was submitted
	 */
	@PutMapping("/posts/{id}/regular")
	public Regular updateRegular(@PathVariable(value = "id") Long postId, @RequestBody Regular regularDetails) {
		Regular reg = regularRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Regular", "id", postId));

		reg.updateData(regularDetails);

		Regular updatedReg = regularRepo.save(reg);

		return updatedReg;
	}

	/**
	 * DEL to delete all "regular" posts in the DB
	 * 
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/regulars")
	public ResponseEntity<?> deleteAllRegulars() {
		regularRepo.deleteAll();

		return ResponseEntity.ok().build();
	}

	/**
	 * DEL to delete a single regular post, by ID
	 * 
	 * @param postId the ID of the post to be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/{id}/regular")
	public ResponseEntity<?> deleteRegular(@PathVariable(value = "id") Long postId) {
		Regular reg = regularRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

		regularRepo.delete(reg);

		return ResponseEntity.ok().build();
	}

	/**
	 * GET request for listing all videos
	 * 
	 * @return {@link java.util.List List} of all videos in the database
	 */
	@GetMapping("/posts/videos")
	public List<Video> getAllVideos() {
		return videoRepo.findAll();
	}

	/**
	 * POST request to submit a Tumblr "video post" into the system
	 * 
	 * @param postId The ID of the post to which this video content refers
	 * @param video  The data to be submitted
	 * @return The same {@link com.tiyb.tev.datamodel.Video Video} object that was
	 *         submitted
	 */
	@PostMapping("/posts/{id}/video")
	public Video createVideo(@PathVariable(value = "id") Long postId, @Valid @RequestBody Video video) {
		video.setPostId(postId);
		return videoRepo.save(video);
	}

	/**
	 * GET to return a single video post, by ID
	 * 
	 * @param postId The Post ID
	 * @return The {@link com.tiyb.tev.datamodel.Video Video} details
	 */
	@GetMapping("/posts/{id}/video")
	public Video getVideoById(@PathVariable(value = "id") Long postId) {
		return videoRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Video", "id", postId));
	}

	/**
	 * PUT to update a Video
	 * 
	 * @param postId       The ID of the post to be updated
	 * @param videoDetails The data to be updated
	 * @return The same {@link com.tiyb.tev.datamodel.Video Video} object that was
	 *         submitted
	 */
	@PutMapping("/posts/{id}/video")
	public Video updateVideo(@PathVariable(value = "id") Long postId, @RequestBody Video videoDetails) {
		Video video = videoRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Video", "id", postId));

		video.updateData(videoDetails);

		Video updatedVideo = videoRepo.save(video);

		return updatedVideo;
	}

	/**
	 * DEL to delete all "video" posts in the DB
	 * 
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/videos")
	public ResponseEntity<?> deleteAllVideos() {
		videoRepo.deleteAll();

		return ResponseEntity.ok().build();
	}

	/**
	 * DEL to delete a single video, by ID
	 * 
	 * @param postId the ID of the post to be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/posts/{id}/video")
	public ResponseEntity<?> deleteVideo(@PathVariable(value = "id") Long postId) {
		Video video = videoRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Video", "id", postId));

		videoRepo.delete(video);

		return ResponseEntity.ok().build();
	}

}
