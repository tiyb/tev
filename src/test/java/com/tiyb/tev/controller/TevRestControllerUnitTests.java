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
import com.tiyb.tev.datamodel.Conversation;
import com.tiyb.tev.datamodel.ConversationMessage;
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.datamodel.Type;
import com.tiyb.tev.datamodel.Video;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TevRestControllerUnitTests {

	@Autowired
	private TEVRestController restController;
	
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
		modifiedPost.setHeight(1);
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
		modifiedPost.setWidth(5);
		
		restController.updatePost(1L, modifiedPost);
		
		Post finalPostFromServer = restController.getPostById(1L);
		
		assertThat(finalPostFromServer).isNotNull();
		assertThat(finalPostFromServer).isEqualToComparingFieldByField(modifiedPost);
	}
	
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
	
	@Test
	public void getTypes() {
		List<Type> originalTypes = restController.getAllTypes();
		assertThat(originalTypes).isNotNull();
		assertThat(originalTypes.size()).isEqualTo(5);
	}
	
	@Test
	public void updateMetadata() {
		Metadata md = restController.getMetadata();
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
		
		restController.updateMetadata(md);
		
		Metadata finalFromServer = restController.getMetadata();
		
		assertThat(finalFromServer).isNotNull();
		assertThat(finalFromServer).isEqualToComparingFieldByField(md);
	}
	
	@Test
	public void updateConversation() {
		Conversation original = new Conversation();
		
		Conversation modified = restController.createConversation(original);
		assertThat(modified).isNotNull();
		
		modified.setNumMessages(5);
		modified.setParticipant("new participant");
		modified.setParticipantAvatarUrl("avatar URL");
		
		restController.updateConversation(modified.getId(), modified);
		
		Conversation finalFromServer = restController.getConversationByParticipant("new participant");
		
		assertThat(finalFromServer).isNotNull();
		assertThat(finalFromServer).isEqualToComparingFieldByField(modified);
	}
	
	@Test
	public void updateConvoMsg() {
		ConversationMessage original = new ConversationMessage();
		
		ConversationMessage modified = restController.createConvoMessage(original);
		assertThat(modified).isNotNull();
		
		modified.setConversationId(1L);
		modified.setMessage("message");
		modified.setReceived(true);
		modified.setTimestamp(25L);
		modified.setType("IMAGE");
		
		restController.updateConvoMsg(modified.getId(), modified);
		
		List<ConversationMessage> finalFromServer = restController.getConvoMsgByConvoID(1L);
		
		assertThat(finalFromServer).isNotNull();
		assertThat(finalFromServer.size()).isEqualTo(1);
		assertThat(finalFromServer.get(0)).isEqualToComparingFieldByField(modified);
	}
	
}
