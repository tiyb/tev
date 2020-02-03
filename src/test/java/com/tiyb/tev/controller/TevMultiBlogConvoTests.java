package com.tiyb.tev.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.tiyb.tev.datamodel.Conversation;

/**
 * Some Conversation-related tests specifically for multi-blog scenarios
 * 
 * @author tiyb
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TevMultiBlogConvoTests {
	@Autowired
	private TEVConvoRestController restController;

	private static final String BLOG1_NAME = "blog1";
	private static final String BLOG2_NAME = "blog2";

	/**
	 * Clean out conversations before each test
	 */
	@Before
	public void cleanConversations() {
		restController.deleteAllConvoMsgsForBlog(BLOG1_NAME);
		restController.deleteAllConversationsForBlog(BLOG1_NAME);
		restController.deleteAllConvoMsgsForBlog(BLOG2_NAME);
		restController.deleteAllConversationsForBlog(BLOG2_NAME);
	}

	/**
	 * Tests that conversations can be created in different blogs, and retrieved
	 * appropriately
	 */
	@Test
	public void convosInMultiBlogs() {
		Conversation c1 = new Conversation();
		c1.setBlog(BLOG1_NAME);
		c1.setParticipant("participant1");
		restController.createConversationForBlog(BLOG1_NAME, c1);
		Conversation c2 = new Conversation();
		c2.setBlog(BLOG1_NAME);
		c2.setParticipant("participant2");
		restController.createConversationForBlog(BLOG1_NAME, c2);
		Conversation c3 = new Conversation();
		c3.setBlog(BLOG2_NAME);
		c3.setParticipant("participant3");
		restController.createConversationForBlog(BLOG2_NAME, c3);

		List<Conversation> convos = restController.getAllConversationsForBlog(BLOG1_NAME);
		assertThat(convos).isNotNull();
		assertThat(convos.size()).isEqualTo(2);
		convos = restController.getAllConversationsForBlog(BLOG2_NAME);
		assertThat(convos).isNotNull();
		assertThat(convos.size()).isEqualTo(1);
	}

	/**
	 * Tests that the "un-ignore all" functionality doesn't accidentally
	 * cross-pollute other blogs
	 */
	@Test
	public void hideUnhideForMultiBlogs() {
		Conversation c1 = new Conversation();
		c1.setBlog(BLOG1_NAME);
		c1.setParticipant("participant1");
		c1.setHideConversation(false);
		restController.createConversationForBlog(BLOG1_NAME, c1);
		Conversation c2 = new Conversation();
		c2.setBlog(BLOG1_NAME);
		c2.setParticipant("participant2");
		c2.setHideConversation(false);
		restController.createConversationForBlog(BLOG1_NAME, c2);
		Conversation c3 = new Conversation();
		c3.setBlog(BLOG2_NAME);
		c3.setParticipant("participant3");
		c3.setHideConversation(false);
		restController.createConversationForBlog(BLOG2_NAME, c3);
		Conversation c4 = new Conversation();
		c4.setBlog(BLOG2_NAME);
		c4.setParticipant("participant4");
		c4.setHideConversation(false);
		restController.createConversationForBlog(BLOG2_NAME, c4);

		// verify everything is initially correct -- and set it to hidden, right after
		// it's verified
		List<Conversation> convos = restController.getAllConversationsForBlog(BLOG1_NAME);
		assertThat(convos).isNotNull();
		assertThat(convos.size()).isEqualTo(2);
		for (Conversation c : convos) {
			assertThat(c.getHideConversation()).isEqualTo(false);
			c.setHideConversation(true);
			restController.updateConversationForBlog(BLOG1_NAME, c.getId(), c);
		}
		convos = restController.getAllConversationsForBlog(BLOG2_NAME);
		assertThat(convos).isNotNull();
		assertThat(convos.size()).isEqualTo(2);
		for (Conversation c : convos) {
			assertThat(c.getHideConversation()).isEqualTo(false);
			c.setHideConversation(true);
			restController.updateConversationForBlog(BLOG2_NAME, c.getId(), c);
		}

		// everything should now be hidden, after the previous step; verify it
		convos = restController.getAllConversationsForBlog(BLOG1_NAME);
		assertThat(convos).isNotNull();
		assertThat(convos.size()).isEqualTo(2);
		for (Conversation c : convos) {
			assertThat(c.getHideConversation()).isEqualTo(true);
		}
		convos = restController.getAllConversationsForBlog(BLOG2_NAME);
		assertThat(convos).isNotNull();
		assertThat(convos.size()).isEqualTo(2);
		for (Conversation c : convos) {
			assertThat(c.getHideConversation()).isEqualTo(true);
		}

		// unignore all blog1 conversations
		restController.unignoreAllConversationsForBlog(BLOG1_NAME);

		// verify everything in blog1 is hidden (and unhide it)
		convos = restController.getAllConversationsForBlog(BLOG1_NAME);
		assertThat(convos).isNotNull();
		assertThat(convos.size()).isEqualTo(2);
		for (Conversation c : convos) {
			assertThat(c.getHideConversation()).isEqualTo(false);
			c.setHideConversation(true);
			restController.updateConversationForBlog(BLOG1_NAME, c.getId(), c);
		}
		// verify everything in blog2 is hidden
		convos = restController.getAllConversationsForBlog(BLOG2_NAME);
		assertThat(convos).isNotNull();
		assertThat(convos.size()).isEqualTo(2);
		for (Conversation c : convos) {
			assertThat(c.getHideConversation()).isEqualTo(true);
		}

		// ignore all blog2 conversations
		restController.unignoreAllConversationsForBlog(BLOG2_NAME);

		// verify everything is good
		convos = restController.getAllConversationsForBlog(BLOG1_NAME);
		assertThat(convos).isNotNull();
		assertThat(convos.size()).isEqualTo(2);
		for (Conversation c : convos) {
			assertThat(c.getHideConversation()).isEqualTo(true);
		}
		convos = restController.getAllConversationsForBlog(BLOG2_NAME);
		assertThat(convos).isNotNull();
		assertThat(convos.size()).isEqualTo(2);
		for (Conversation c : convos) {
			assertThat(c.getHideConversation()).isEqualTo(false);
		}
	}

	/**
	 * Tests that conversations with the same participant can be stored across
	 * different blogs.
	 */
	@Test
	public void twoBlogsOneParticipant() {
		Conversation c1 = new Conversation();
		c1.setBlog(BLOG1_NAME);
		c1.setParticipant("participant1");
		c1.setParticipantAvatarUrl("this is blog1");
		restController.createConversationForBlog(BLOG1_NAME, c1);
		Conversation c3 = new Conversation();
		c3.setBlog(BLOG2_NAME);
		c3.setParticipant("participant1");
		c3.setParticipantAvatarUrl("this is blog2");
		restController.createConversationForBlog(BLOG2_NAME, c3);

		Conversation result = restController.getConversationForBlogByParticipant(BLOG1_NAME, "participant1");
		assertThat(result).isNotNull();
		assertThat(result.getParticipantAvatarUrl()).isEqualTo("this is blog1");

		result = restController.getConversationForBlogByParticipant(BLOG2_NAME, "participant1");
		assertThat(result).isNotNull();
		assertThat(result.getParticipantAvatarUrl()).isEqualTo("this is blog2");
	}

}
