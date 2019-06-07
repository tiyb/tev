package com.tiyb.tev.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.tiyb.tev.datamodel.Answer;
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.datamodel.Type;
import com.tiyb.tev.datamodel.Video;

/**
 * <p>
 * Test cases for unit testing the REST controller for working with Posts. Not
 * calling every single method/API in the controller, just the ones that involve
 * more logic. (i.e. we're not testing the underlying Spring Boot capabilities,
 * only the application's logic.)
 * </p>
 * 
 * <p>
 * <code>updateXXX()</code> APIs <i>are</i> tested, however, to ensure that all
 * fields are always accounted for, since this is an easy place to make a
 * mistake. The logic is always the same for these tests: 1) Create empty post
 * with hard-coded ID; 2) update the fields on that post; 3) use the REST
 * controller to update the post in the DB; 4) use the REST API to retrieve that
 * same post; 5) verify all of the fields are the same
 * </p>
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TevPostRestControllerUnitTests {

	@Autowired
	private TEVPostRestController restController;
	@Autowired
	private TEVMetadataRestController mdRestController;

	/**
	 * Verifies that updating a Post properly updates all fields
	 */
	@Test
	public void updatePost() {
		Post originalEmptyPost = new Post();
		originalEmptyPost.setId(1L);

		Post modifiedPost = restController.createPost(originalEmptyPost);
		assertThat(modifiedPost).isNotNull();

		modifiedPost.setDate("Jan 1, 2019");
		modifiedPost.setIsReblog(true);
		modifiedPost.setSlug("New Slug");
		modifiedPost.setDateGmt("Date GMT");
		modifiedPost.setIsFavourite(true);
		modifiedPost.setIsRead(true);
		modifiedPost.setIsReblog(true);
		modifiedPost.setReblogKey("reblog Key");
		modifiedPost.setSlug("new slug");
		modifiedPost.setTags("hi, there");
		modifiedPost.setTumblelog("blog");
		modifiedPost.setUnixtimestamp(25L);
		modifiedPost.setUrl("URL");
		modifiedPost.setUrlWithSlug("URL with slug");
		modifiedPost.setState("published");

		restController.updatePost(1L, modifiedPost);

		Post finalPostFromServer = restController.getPostById(1L);

		assertThat(finalPostFromServer).isNotNull();
		assertThat(finalPostFromServer).isEqualToComparingFieldByField(modifiedPost);
	}

	/**
	 * Verify that marking a post "read" in the DB really does mark it read
	 */
	@Test
	public void markPostRead() {
		Post originalPost = new Post();
		originalPost.setId(1L);
		originalPost.setIsRead(false);

		originalPost = restController.createPost(originalPost);
		assertThat(originalPost).isNotNull();

		Post newPost = restController.markPostRead(1L);
		assertThat(newPost).isNotNull();
		assertThat(newPost.getIsRead()).isEqualTo(true);

		Post finalPost = restController.getPostById(1L);
		assertThat(finalPost).isNotNull();
		assertThat(finalPost.getIsRead()).isEqualTo(true);
	}

	/**
	 * Verify that marking a post "unread" in the DB really does mark it unread
	 */
	@Test
	public void markPostUnread() {
		Post originalPost = new Post();
		originalPost.setId(1L);
		originalPost.setIsRead(true);

		originalPost = restController.createPost(originalPost);
		assertThat(originalPost).isNotNull();

		Post newPost = restController.markPostUnread(1L);
		assertThat(newPost).isNotNull();
		assertThat(newPost.getIsRead()).isEqualTo(false);

		Post finalPost = restController.getPostById(1L);
		assertThat(finalPost).isNotNull();
		assertThat(finalPost.getIsRead()).isEqualTo(false);
	}

	/**
	 * Verify that marking a post a "favourite" in the DB really does mark it as a
	 * favourite
	 */
	@Test
	public void markPostFavourite() {
		Post originalPost = new Post();
		originalPost.setId(1L);
		originalPost.setIsFavourite(false);

		originalPost = restController.createPost(originalPost);
		assertThat(originalPost).isNotNull();

		Post newPost = restController.markPostFavourite(1L);
		assertThat(newPost).isNotNull();
		assertThat(newPost.getIsFavourite()).isEqualTo(true);

		Post finalPost = restController.getPostById(1L);
		assertThat(finalPost).isNotNull();
		assertThat(finalPost.getIsFavourite()).isEqualTo(true);
	}

	/**
	 * Verify that marking a post not a "favourite" in the DB really does mark it as
	 * not a favourite
	 */
	@Test
	public void markPostNotFavourite() {
		Post originalPost = new Post();
		originalPost.setId(1L);
		originalPost.setIsFavourite(true);

		originalPost = restController.createPost(originalPost);
		assertThat(originalPost).isNotNull();

		Post newPost = restController.markPostNonFavourite(1L);
		assertThat(newPost).isNotNull();
		assertThat(newPost.getIsFavourite()).isEqualTo(false);

		Post finalPost = restController.getPostById(1L);
		assertThat(finalPost).isNotNull();
		assertThat(finalPost.getIsFavourite()).isEqualTo(false);
	}

	/**
	 * Verifies that updating an Answer properly updates all fields
	 */
	@Test
	public void updateAnswer() {
		Answer originalAns = new Answer();
		originalAns.setPostId(1L);
		originalAns.setQuestion("original question");
		originalAns.setAnswer("original answer");

		Answer modifiedAnswer = restController.createAnswer(1L, originalAns);
		assertThat(modifiedAnswer).isNotNull();

		modifiedAnswer.setAnswer("new answer");
		modifiedAnswer.setQuestion("new question");

		restController.updateAnswer(1L, modifiedAnswer);

		Answer finalFromServer = restController.getAnswerById(1L);

		assertThat(finalFromServer).isNotNull();
		assertThat(finalFromServer).isEqualToComparingFieldByField(modifiedAnswer);
	}

	/**
	 * Verifies that updating a Link properly updates all fields
	 */
	@Test
	public void updateLink() {
		Link original = new Link();
		original.setPostId(1L);
		original.setUrl("original url");

		Link modifiedLink = restController.createLink(1L, original);
		assertThat(modifiedLink).isNotNull();

		modifiedLink.setDescription("new description");
		modifiedLink.setText("new link text");
		modifiedLink.setUrl("new url");

		restController.updateLink(1L, modifiedLink);

		Link finalFromServer = restController.getLinkById(1L);

		assertThat(finalFromServer).isNotNull();
		assertThat(finalFromServer).isEqualToComparingFieldByField(modifiedLink);
	}

	/**
	 * Verifies that updating a Photo properly updates all fields
	 */
	@Test
	public void updatePhoto() {
		Photo original = new Photo();
		original.setPostId(1L);

		Photo modifiedPhoto = restController.createPhoto(original);
		assertThat(modifiedPhoto).isNotNull();

		modifiedPhoto.setCaption("new caption");
		modifiedPhoto.setHeight(2);
		modifiedPhoto.setOffset("new offset");
		modifiedPhoto.setPhotoLinkUrl("new photo link url");
		modifiedPhoto.setUrl100("url 100");
		modifiedPhoto.setUrl1280("url 1280");
		modifiedPhoto.setUrl250("url 250");
		modifiedPhoto.setUrl400("url 400");
		modifiedPhoto.setUrl500("url 500");
		modifiedPhoto.setUrl75("url 75");
		modifiedPhoto.setWidth(3);

		restController.updatePhoto(1L, modifiedPhoto);

		List<Photo> finalFromServer = restController.getPhotoById(1L);
		assertThat(finalFromServer).isNotNull();
		assertThat(finalFromServer.size()).isEqualTo(1);
		assertThat(finalFromServer.get(0)).isEqualToComparingFieldByField(modifiedPhoto);
	}

	/**
	 * Verifies that updating a Regular properly updates all fields
	 */
	@Test
	public void updateRegular() {
		Regular original = new Regular();
		original.setPostId(1L);

		Regular modified = restController.createRegular(1L, original);
		assertThat(modified).isNotNull();

		modified.setBody("new body");
		modified.setTitle("new title");

		restController.updateRegular(1L, modified);

		Regular finalFromServer = restController.getRegularById(1L);

		assertThat(finalFromServer).isNotNull();
		assertThat(finalFromServer).isEqualToComparingFieldByField(modified);
	}

	/**
	 * Verifies that updating a Video properly updates all fields
	 */
	@Test
	public void updateVideo() {
		Video original = new Video();
		original.setPostId(1L);

		Video modified = restController.createVideo(1L, original);
		assertThat(modified).isNotNull();

		modified.setContentType("content Type");
		modified.setDuration(20);
		modified.setExtension("ext");
		modified.setHeight(30);
		modified.setRevision("revision");
		modified.setVideoCaption("video caption");
		modified.setWidth(40);

		restController.updateVideo(1L, modified);

		Video finalFromServer = restController.getVideoById(1L);

		assertThat(finalFromServer).isNotNull();
		assertThat(finalFromServer).isEqualToComparingFieldByField(modified);
	}

	/**
	 * Verifies that retrieving the list of Types from the system returns the proper
	 * number of items
	 */
	@Test
	public void getTypes() {
		List<Type> originalTypes = mdRestController.getAllTypes();
		assertThat(originalTypes).isNotNull();
		assertThat(originalTypes.size()).isEqualTo(5);
	}

	/**
	 * Verifies that updating Metadata properly updates all fields
	 */
	@Test
	public void updateMetadata() {
		Metadata md = mdRestController.getMetadata();
		assertThat(md).isNotNull();
		assertThat(md).isEqualToComparingFieldByField(Metadata.newDefaultMetadata());

		md.setBaseMediaPath("new base media path");
		md.setFavFilter("new fav filter");
		md.setFilter("new filter");
		md.setMainTumblrUser("main tumblr user");
		md.setPageLength(25);
		md.setSortColumn("sort column");
		md.setSortOrder("sort order");
		md.setShowReadingPane(true);
		md.setOverwritePostData(true);
		md.setOverwriteConvoData(true);

		mdRestController.updateMetadata(md);

		Metadata finalFromServer = mdRestController.getMetadata();

		assertThat(finalFromServer).isNotNull();
		assertThat(finalFromServer).isEqualToComparingFieldByField(md);
	}

}
