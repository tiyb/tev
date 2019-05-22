package com.tiyb.tev.controller;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URL;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tiyb.tev.datamodel.Answer;
import com.tiyb.tev.datamodel.Conversation;
import com.tiyb.tev.datamodel.ConversationMessage;
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.datamodel.Type;
import com.tiyb.tev.datamodel.Video;
import com.tiyb.tev.datamodel.helpers.StaticListData;
import com.tiyb.tev.exception.ResourceNotFoundException;
import com.tiyb.tev.repository.AnswerRepository;
import com.tiyb.tev.repository.ConversationMessageRepository;
import com.tiyb.tev.repository.ConversationRepository;
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
	@Autowired
	ConversationRepository conversationRepo;
	@Autowired
	ConversationMessageRepository convoMsgRepo;

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
	 * GET request to mark all posts in the database as read. Not intended to be
	 * used by the UI, but helpful as a "helper" API.
	 * 
	 * @return Success indicator
	 */
	@GetMapping("/posts/markAllRead")
	public String markAllPostsRead() {
		List<Post> posts = getAllPosts();
		
		for(Post post : posts) {
			markPostRead(post.getId());
		}
		
		return "Success";
	}
	
	/**
	 * GET request to mark all posts in the database as unread. Not intended to be
	 * used by the UI, but helpful as a "helper" API.
	 * 
	 * @return Success indicator
	 */
	@GetMapping("/posts/markAllUnread")
	public String markAllPostsUnread() {
		List<Post> posts = getAllPosts();

		for (Post post : posts) {
			markPostUnread(post.getId());
		}

		return "Success";
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
		post.setIsReblog(postDetails.getIsReblog());
		post.setReblogKey(postDetails.getReblogKey());
		post.setSlug(postDetails.getSlug());
		post.setTumblelog(postDetails.getTumblelog());
		// post.setType(); not being implemented
		post.setUnixtimestamp(postDetails.getUnixtimestamp());
		post.setUrl(postDetails.getUrl());
		post.setUrlWithSlug(postDetails.getUrlWithSlug());
		post.setIsRead(postDetails.getIsRead());
		post.setTags(postDetails.getTags());
		post.setIsFavourite(postDetails.getIsFavourite());
		post.setState(postDetails.getState());

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
		String imageDirectory = getMetadata().getBaseMediaPath();
		if(imageDirectory == null || imageDirectory.equals("")) {
			return false;
		}
		
		if(imageDirectory.charAt(imageDirectory.length()-1) != '/') {
			imageDirectory = imageDirectory + "/";
		}
		
		List<Photo> photos = photoRepo.findByPostIdOrderByOffset(postId);
		boolean response = true;
		
		for(int i = 0; i < photos.size(); i++) {
			Photo photo = photos.get(i);
			String url = photo.getUrl1280();
			String ext = url.substring(url.lastIndexOf('.'));
			try {
				BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
				FileOutputStream out = new FileOutputStream(imageDirectory + photo.getPostId() + "_" + i + ext);
				byte dataBuffer[] = new byte[1024];
				int bytesRead;
				while((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
					out.write(dataBuffer, 0, bytesRead);
				}
				out.close();
			}
			catch(Exception e) {
				response = false;
			} 
		}
		return response;
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

		if (types.size() == 0) {
			Type type1 = new Type();
			type1.setId(1L);
			type1.setType("answer");
			typeRepo.save(type1);
			types.add(type1);

			Type type2 = new Type();
			type2.setId(2L);
			type2.setType("link");
			typeRepo.save(type2);
			types.add(type2);

			Type type3 = new Type();
			type3.setId(3L);
			type3.setType("photo");
			typeRepo.save(type3);
			types.add(type3);

			Type type4 = new Type();
			type4.setId(4L);
			type4.setType("regular");
			typeRepo.save(type4);
			types.add(type4);

			Type type5 = new Type();
			type5.setId(5L);
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
			Metadata md = Metadata.newDefaultMetadata();
			return md;
		}
	}

	/**
	 * GET to return static data used to populate drop-down lists, for the user to
	 * set things like preferred sort order, filters, etc.
	 * 
	 * @return object containing the different lists of strings available
	 */
	@GetMapping("/metadata/staticListData")
	public StaticListData getStaticListData() {
		StaticListData sld = new StaticListData();

		for (String s : Metadata.FILTER_TYPES) {
			sld.getFilterTypes().add(s);
		}
		for (String s : Metadata.SORT_COLUMNS) {
			sld.getSortColumns().add(s);
		}
		for (String s : Metadata.SORT_ORDERS) {
			sld.getSortOrders().add(s);
		}
		for(String s : Metadata.FAV_FILTERS) {
			sld.getFavFilters().add(s);
		}
		for(Integer i : Metadata.PAGE_LENGTHS) {
			sld.getPageLengths().add(i);
		}

		return sld;
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
		md.setFilter(metadataDetails.getFilter());
		md.setSortColumn(metadataDetails.getSortColumn());
		md.setSortOrder(metadataDetails.getSortOrder());
		md.setMainTumblrUser(metadataDetails.getMainTumblrUser());
		md.setMainTumblrUserAvatarUrl(metadataDetails.getMainTumblrUserAvatarUrl());
		md.setFavFilter(metadataDetails.getFavFilter());
		md.setPageLength(metadataDetails.getPageLength());
		md.setShowReadingPane(metadataDetails.getShowReadingPane());
		md.setOverwritePostData(metadataDetails.getOverwritePostData());
		md.setOverwriteConvoData(metadataDetails.getOverwriteConvoData());
		Metadata returnValue = metadataRepo.save(md);

		return returnValue;
	}

	/**
	 * GET request for listing all conversations
	 * 
	 * @return <code>List</code> of all conversations in the database
	 */
	@GetMapping("/conversations")
	public List<Conversation> getAllConversations() {
		return conversationRepo.findAll();
	}

	/**
	 * GET to return a single conversation, by ID
	 * 
	 * @param conversationID the conversation ID
	 * @return The Conversation details
	 */
	@GetMapping("/conversations/{id}")
	public Conversation getConversationById(@PathVariable(value = "id") Long conversationID) {
		return conversationRepo.findById(conversationID)
				.orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationID));
	}
	
	/**
	 * GET to return a single conversation, by participant name
	 * 
	 * @param participantName Name of the participant
	 * @return Single Conversation
	 */
	@GetMapping("/conversation")
	public Conversation getConversationByParticipant(@RequestParam("participant") String participantName) {
		Conversation convo = conversationRepo.findByParticipant(participantName);
		
		if(convo == null) {
			throw new ResourceNotFoundException("Conversation", "id", participantName);
		}
		
		return convo;
	}
	
	/**
	 * Returns all conversations that are not set to "hidden" status
	 * 
	 * @return <code>List</code> of conversations
	 */
	@GetMapping("/conversations/unhidden")
	public List<Conversation> getUnhiddenConversations() {
		return conversationRepo.findByHideConversationFalse();
	}

	/**
	 * POST request to submit a conversation into the system
	 * 
	 * @param conversation The conversation object (in JSON format) to be saved into
	 *                     the database
	 * @return The same object that was saved (including the generated ID)
	 */
	@PostMapping("/conversations")
	public Conversation createConversation(@Valid @RequestBody Conversation conversation) {
		return conversationRepo.save(conversation);
	}

	/**
	 * PUT to update a conversation
	 * 
	 * @param conversationId ID of the convo to be updated
	 * @param convoDetails   Details to be inserted into the DB
	 * @return The updated Conversation details
	 */
	@PutMapping("/conversations/{id}")
	public Conversation updateConversation(@PathVariable(value = "id") Long conversationId,
			@RequestBody Conversation convoDetails) {
		Conversation convo = conversationRepo.findById(conversationId)
				.orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

		convo.setParticipant(convoDetails.getParticipant());
		convo.setParticipantAvatarUrl(convoDetails.getParticipantAvatarUrl());
		convo.setNumMessages(convoDetails.getNumMessages());
		convo.setHideConversation(convoDetails.getHideConversation());

		Conversation updatedConvo = conversationRepo.save(convo);

		return updatedConvo;
	}

	/**
	 * Sets a conversation to "ignored" status
	 * 
	 * @param participantName Name of the participant in the conversation to be
	 *                        ignored
	 * @return Updated Conversation
	 */
	@PutMapping("/conversations/{participant}/ignoreConvo")
	public Conversation ignoreConversation(@PathVariable(value = "participant") String participantName) {
		Conversation convo = conversationRepo.findByParticipant(participantName);

		convo.setHideConversation(true);

		convo = conversationRepo.save(convo);

		return convo;
	}
	
	/**
	 * Sets a conversation to "un-ignored" status
	 * 
	 * @param participantName Name of the participant in the conversation to be
	 *                        ignored
	 * @return Updated Conversation
	 */
	@PutMapping("/conversations/{participant}/unignoreConvo")
	public Conversation unignoreConversation(@PathVariable(value = "participant") String participantName) {
		Conversation convo = conversationRepo.findByParticipant(participantName);

		convo.setHideConversation(false);

		convo = conversationRepo.save(convo);

		return convo;
	}
	
	/**
	 * Used to reset all conversations back to an un-hidden state
	 */
	@GetMapping("/conversations/unignoreAllConversations")
	public void unignoreAllConversations() {
		List<Conversation> hiddenConvos = conversationRepo.findByHideConversationTrue();

		if (hiddenConvos.size() < 1) {
			return;
		}

		for (Conversation convo : hiddenConvos) {
			convo.setHideConversation(false);
			conversationRepo.save(convo);
		}

		return;
	}

	/**
	 * DEL to delete a single conversation, by ID
	 * 
	 * @param convoId ID of convo to be deleted
	 * @return <code>ResponseEntity</code> with the response details
	 */
	@DeleteMapping("/conversations/{id}")
	public ResponseEntity<?> deleteConversation(@PathVariable(value = "id") Long convoId) {
		Conversation convo = conversationRepo.findById(convoId)
				.orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", convoId));

		conversationRepo.delete(convo);

		return ResponseEntity.ok().build();
	}

	/**
	 * DEL to delete all conversations in the DB
	 * 
	 * @return <code>ResponseEntity</code> with the response details
	 */
	@DeleteMapping("/conversations")
	public ResponseEntity<?> deleteAllConversations() {
		conversationRepo.deleteAll();

		return ResponseEntity.ok().build();
	}

	/**
	 * GET request for listing all messages (regardless of conversation)
	 * 
	 * @return list of all messages in the database
	 */
	@GetMapping("/conversations/messages")
	public List<ConversationMessage> getAllConversationMessages() {
		return convoMsgRepo.findAll();
	}

	/**
	 * POST request to submit a new conversation message into the system
	 * 
	 * @param convoMsg The data to be submitted
	 * @return The object that was just submitted (with ID)
	 */
	@PostMapping("/conversations/messages")
	public ConversationMessage createConvoMessage(@Valid @RequestBody ConversationMessage convoMsg) {
		return convoMsgRepo.save(convoMsg);
	}

	/**
	 * GET to return all messages for a particular conversation
	 * 
	 * @param convoId The conversation ID
	 * @return The list of messages
	 */
	@GetMapping("/conversations/{id}/messages")
	public List<ConversationMessage> getConvoMsgByConvoID(@PathVariable(value = "id") Long convoId) {
		return convoMsgRepo.findByConversationIdOrderByTimestamp(convoId);
	}

	/**
	 * PUT to update a conversation message
	 * 
	 * @param msgId    The ID of the message to be updated
	 * @param convoMsg The updated data
	 * @return The same data that was just submitted
	 */
	@PutMapping("/conversations/messages/{id}")
	public ConversationMessage updateConvoMsg(@PathVariable(value = "id") Long msgId,
			@RequestBody ConversationMessage convoMsg) {
		ConversationMessage cm = convoMsgRepo.findById(msgId)
				.orElseThrow(() -> new ResourceNotFoundException("ConversationMessage", "id", msgId));

		cm.setConversationId(convoMsg.getConversationId());
		cm.setMessage(convoMsg.getMessage());
		cm.setReceived(convoMsg.getReceived());
		cm.setType(convoMsg.getType());
		cm.setTimestamp(convoMsg.getTimestamp());

		ConversationMessage updatedCM = convoMsgRepo.save(cm);

		return updatedCM;
	}

	/**
	 * DEL to delete all conversation messages in the DB
	 * 
	 * @return <code>ResponseEntity</code> with the response details
	 */
	@DeleteMapping("/conversations/messages")
	public ResponseEntity<?> deleteAllConvoMsgs() {
		convoMsgRepo.deleteAll();

		return ResponseEntity.ok().build();
	}

	/**
	 * DEL to delete a single conversation message, by ID
	 * 
	 * @param msgId ID of the message to be deleted
	 * @return <code>ResponseEntity</code> with the response details
	 */
	@DeleteMapping("/conversations/messages/{id}")
	public ResponseEntity<?> deleteConversationMessage(@PathVariable(name = "id") Long msgId) {
		ConversationMessage cm = convoMsgRepo.findById(msgId)
				.orElseThrow(() -> new ResourceNotFoundException("ConversationMessage", "id", msgId));

		convoMsgRepo.delete(cm);

		return ResponseEntity.ok().build();
	}

}
