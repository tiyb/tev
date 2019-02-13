package com.tiyb.tev.controller;

import java.util.List;
import java.util.Optional;

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
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.datamodel.Type;
import com.tiyb.tev.datamodel.Video;
import com.tiyb.tev.exception.ResourceNotFoundException;
import com.tiyb.tev.repository.AnswerRepository;
import com.tiyb.tev.repository.LinkRepository;
import com.tiyb.tev.repository.MetadataRepository;
import com.tiyb.tev.repository.PhotoRepository;
import com.tiyb.tev.repository.PostRepository;
import com.tiyb.tev.repository.RegularRepository;
import com.tiyb.tev.repository.TypeRepository;
import com.tiyb.tev.repository.VideoRepository;

/**
 * <p>
 * This is the REST controller for the TEV application. The entire RESTful API
 * is exposed via this Controller, with methods/API calls for working with posts
 * (including special <i>kinds</i> of posts, such as "regular" and "photo"
 * posts), metadata, and "types".
 * </p>
 * 
 * <p>
 * In general, a "more is better than less" approach has been taken -- some APIs
 * have been created even if they aren't used by the TEV application, just in
 * case.
 * </p>
 * 
 * @author tiyb
 * @apiviz.landmark
 * @apiviz.uses com.tiyb.tev.repository.AnswerRepository
 * @apiviz.uses com.tiyb.tev.repository.LinkRepository
 * @apiviz.uses com.tiyb.tev.repository.MetadataRepository
 * @apiviz.uses com.tiyb.tev.repository.PhotoRepository
 * @apiviz.uses com.tiyb.tev.repository.PostRepository
 * @apiviz.uses com.tiyb.tev.repository.RegularRepository
 * @apiviz.uses com.tiyb.tev.repository.TypeRepository
 * @apiviz.uses com.tiyb.tev.repository.VideoRepository
 *
 */
@RestController
@RequestMapping("/api")
public class TEVRestController {

	@Autowired
	PostRepository postRepo;
	@Autowired
	TypeRepository typeRepo;
	@Autowired
	RegularRepository regularRepo;
	@Autowired
	AnswerRepository answerRepo;
	@Autowired
	LinkRepository linkRepo;
	@Autowired
	PhotoRepository photoRepo;
	@Autowired
	VideoRepository videoRepo;
	@Autowired
	MetadataRepository metadataRepo;

	/**
	 * GET request for listing all posts
	 * 
	 * @return <code>List<></code> of all posts in the database
	 */
	@GetMapping("/posts")
	public List<Post> getAllPosts() {
		return postRepo.findAll();
	}

	/**
	 * POST request to submit a Tumblr post into the system
	 * 
	 * @param post The <code>Post</code> object (in JSON format) to be saved into
	 *             the database
	 * @return The same <code>Post</code> object that was saved (including the ID)
	 */
	@PostMapping("/posts")
	public Post createPost(@Valid @RequestBody Post post) {
		return postRepo.save(post);
	}

	/**
	 * GET to return a single post, by ID
	 * 
	 * @param postId The Post ID
	 * @return The <code>Post</code> details
	 */
	@GetMapping("/posts/{id}")
	public Post getPostById(@PathVariable(value = "id") Long postId) {
		return postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
	}

	/**
	 * PUT to update a Post
	 * 
	 * @param postId      The ID of the post to be updated
	 * @param postDetails The data to be updated
	 * @return The same <code>Post</code> object that was submitted
	 */
	@PutMapping("/posts/{id}")
	public Post updatePost(@PathVariable(value = "id") Long postId, @RequestBody Post postDetails) {
		Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

		post.setDate(postDetails.getDate());
		post.setDateGmt(postDetails.getDateGmt());
		post.setHeight(postDetails.getHeight());
		post.setIsReblog(postDetails.getIsReblog());
		post.setReblogKey(postDetails.getReblogKey());
		post.setSlug(postDetails.getSlug());
		post.setTumblelog(postDetails.getTumblelog());
		// post.setType(); not being implemented
		post.setUnixtimestamp(postDetails.getUnixtimestamp());
		post.setUrl(postDetails.getUrl());
		post.setUrlWithSlug(postDetails.getUrlWithSlug());
		post.setWidth(postDetails.getWidth());

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
	 * DEL to delete a single post, by ID
	 * 
	 * @param postId the ID of the post to be deleted
	 * @return <code>ResponseEntity</code> with the response details
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
	 * @return <code>ResponseEntity</code> with the response details
	 */
	@DeleteMapping("/posts")
	public ResponseEntity<?> deleteAllPosts() {
		postRepo.deleteAll();

		return ResponseEntity.ok().build();
	}

	/**
	 * GET request for listing all answers
	 * 
	 * @return <code>List<></code> of all answers in the database
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
	 * @return The same <code>Answer</code> object that was submitted
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
	 * @return The <code>Answer</code> details
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
	 * @return The same <code>Answer</code> object that was submitted
	 */
	@PutMapping("/posts/{id}/answer")
	public Answer updateAnswer(@PathVariable(value = "id") Long postId, @RequestBody Answer answerDetails) {
		Answer ans = answerRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Answer", "id", postId));

		ans.setAnswer(answerDetails.getAnswer());
		ans.setQuestion(answerDetails.getQuestion());

		Answer updatedAns = answerRepo.save(ans);

		return updatedAns;
	}

	/**
	 * DEL to delete all "answer" posts in the DB
	 * 
	 * @return <code>ResponseEntity</code> with the response details
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
	 * @return <code>ResponseEntity</code> with the response details
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
	 * @return <code>List<></code> of all links in the database
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
	 * @return The same <code>Link</code> object that was submitted
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
	 * @return The <code>Link</code> details
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
	 * @return The same <code>Link</code> object that was submitted
	 */
	@PutMapping("/posts/{id}/link")
	public Link updateLink(@PathVariable(value = "id") Long postId, @RequestBody Link linkDetails) {
		Link link = linkRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Link", "id", postId));

		link.setDescription(linkDetails.getDescription());
		link.setText(linkDetails.getText());
		link.setUrl(linkDetails.getUrl());

		Link updatedLink = linkRepo.save(link);

		return updatedLink;
	}

	/**
	 * DEL to delete all "link" posts in the DB
	 * 
	 * @return <code>ResponseEntity</code> with the response details
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
	 * @return <code>ResponseEntity</code> with the response details
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
	 * @return <code>List<></code> of all photos in the database
	 */
	@GetMapping("/posts/photos")
	public List<Photo> getAllPhotos() {
		return photoRepo.findAll();
	}

	/**
	 * POST request to submit a Tumblr "photo post" into the system
	 * 
	 * @param photo The data to be submitted
	 * @return The same <code>Photo</code> object that was submitted
	 */
	@PostMapping("/posts/photo")
	public Photo createPhoto(@Valid @RequestBody Photo photo) {
		return photoRepo.save(photo);
	}

	/**
	 * GET to return a single Photo post, by ID
	 * 
	 * @param postId The Post ID
	 * @return The <code>Photo</code> details
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
	 * @return The same <code>Photo</code> object that was submitted
	 */
	@PutMapping("/posts/{id}/photo")
	public Photo updatePhoto(@PathVariable(value = "id") Long postId, @RequestBody Photo photoDetails) {
		Photo photo = photoRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Photo", "id", postId));

		photo.setCaption(photoDetails.getCaption());
		photo.setHeight(photoDetails.getHeight());
		photo.setOffset(photoDetails.getOffset());
		photo.setPhotoLinkUrl(photoDetails.getPhotoLinkUrl());
		photo.setUrl100(photoDetails.getUrl100());
		photo.setUrl1280(photoDetails.getUrl1280());
		photo.setUrl250(photoDetails.getUrl250());
		photo.setUrl400(photoDetails.getUrl400());
		photo.setUrl500(photoDetails.getUrl500());
		photo.setUrl75(photoDetails.getUrl75());
		photo.setWidth(photoDetails.getWidth());

		Photo updatedPhoto = photoRepo.save(photo);

		return updatedPhoto;
	}

	/**
	 * DEL to delete all "photo" posts in the DB
	 * 
	 * @return <code>ResponseEntity</code> with the response details
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
	 * @return <code>ResponseEntity</code> with the response details
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
	 * @return <code>List<></code> of all regular posts in the database
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
	 * @return The same <code>Regular</code> object that was submitted.
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
	 * @return The <code>Regular</code> details
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
	 * @return The same <code>Regular</code> object that was submitted
	 */
	@PutMapping("/posts/{id}/regular")
	public Regular updateRegular(@PathVariable(value = "id") Long postId, @RequestBody Regular regularDetails) {
		Regular reg = regularRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Regular", "id", postId));

		reg.setBody(regularDetails.getBody());
		reg.setTitle(regularDetails.getTitle());

		Regular updatedReg = regularRepo.save(reg);

		return updatedReg;
	}

	/**
	 * DEL to delete all "regular" posts in the DB
	 * 
	 * @return <code>ResponseEntity</code> with the response details
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
	 * @return <code>ResponseEntity</code> with the response details
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
	 * @return <code>List<></code> of all videos in the database
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
	 * @return The same <code>Video</code> object that was submitted
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
	 * @return The <code>Video</code> details
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
	 * @return The same <code>Video</code> object that was submitted
	 */
	@PutMapping("/posts/{id}/video")
	public Video updateVideo(@PathVariable(value = "id") Long postId, @RequestBody Video videoDetails) {
		Video video = videoRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Video", "id", postId));

		video.setContentType(videoDetails.getContentType());
		video.setDuration(videoDetails.getDuration());
		video.setExtension(videoDetails.getExtension());
		video.setHeight(videoDetails.getHeight());
		video.setRevision(videoDetails.getRevision());
		video.setVideoCaption(videoDetails.getVideoCaption());
		video.setWidth(videoDetails.getWidth());

		Video updatedVideo = videoRepo.save(video);

		return updatedVideo;
	}

	/**
	 * DEL to delete all "video" posts in the DB
	 * 
	 * @return <code>ResponseEntity</code> with the response details
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
	 * @return <code>ResponseEntity</code> with the response details
	 */
	@DeleteMapping("/posts/{id}/video")
	public ResponseEntity<?> deleteVideo(@PathVariable(value = "id") Long postId) {
		Video video = videoRepo.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Video", "id", postId));

		videoRepo.delete(video);

		return ResponseEntity.ok().build();
	}

	/**
	 * GET to return all Types stored in the system. If there are no types, a
	 * hard-coded list of types is created. (Was originally done in
	 * <code>data.sql</code>, but that no longer works in this implementation.)
	 * 
	 * @return <code>List<></code> of <code>Type</code> objects
	 */
	@GetMapping("/types")
	public List<Type> getAllTypes() {
		List<Type> types = typeRepo.findAll();
		
		if(types.size() == 0) {
			Type type1 = new Type();
			type1.setId((long)1);
			type1.setType("answer");
			typeRepo.save(type1);
			types.add(type1);
			
			Type type2 = new Type();
			type2.setId((long)2);
			type2.setType("link");
			typeRepo.save(type2);
			types.add(type2);
			
			Type type3 = new Type();
			type3.setId((long)3);
			type3.setType("photo");
			typeRepo.save(type3);
			types.add(type3);
			
			Type type4 = new Type();
			type4.setId((long)4);
			type4.setType("regular");
			typeRepo.save(type4);
			types.add(type4);
			
			Type type5 = new Type();
			type5.setId((long)5);
			type5.setType("video");
			typeRepo.save(type5);
			types.add(type5);
		}
		
		return types;
	}

	/**
	 * GET to return the metadata stored in the system. Only one record will ever be
	 * stored in the <code>metadata</code> table, so the API assumes one and only
	 * oen <code>Metadata</code> object.
	 * 
	 * @return The <code>Metadata</code> object stored in the database, or an empty
	 *         object if the table has no data.
	 */
	@GetMapping("/metadata")
	public Metadata getMetadata() {
		List<Metadata> list = metadataRepo.findAll();

		if (list.size() > 0) {
			return list.get(0);
		} else {
			return new Metadata();
		}
	}

	/**
	 * PUT to update metadata details in the database. Code always acts as if there
	 * is one (and only one) record in the DB, even if it's empty; if no record
	 * exists to be updated, a new one is created instead. For this reason, the ID
	 * is always 1.
	 * 
	 * @param metadataDetails The data to be updated in the DB
	 * @return The updated <code>Metadata</code> object
	 */
	@PutMapping("/metadata")
	public Metadata updateMetadata(@RequestBody Metadata metadataDetails) {
		Metadata md;
		Optional<Metadata> omd = metadataRepo.findById(1);
		if (omd.isPresent()) {
			md = omd.get();
		} else {
			md = new Metadata();
			md.setId(1);
		}

		md.setBaseMediaPath(metadataDetails.getBaseMediaPath());
		Metadata returnValue = metadataRepo.save(md);

		return returnValue;
	}

}
