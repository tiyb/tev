package com.tiyb.tev.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.tiyb.tev.TevTestingClass;
import com.tiyb.tev.datamodel.Conversation;

/**
 * Some Conversation-related tests specifically for multi-blog scenarios
 * 
 * @author tiyb
 *
 */
public class TevMultiBlogConvoTests extends TevTestingClass {
    @Autowired
    private TEVConvoRestController restController;

    /**
     * Clean out conversations before each test
     */
    @Before
    public void cleanConversations() {
        restController.deleteAllConvoMsgsForBlog(MAIN_BLOG_NAME);
        restController.deleteAllConversationsForBlog(MAIN_BLOG_NAME);
        restController.deleteAllConvoMsgsForBlog(SECOND_BLOG_NAME);
        restController.deleteAllConversationsForBlog(SECOND_BLOG_NAME);
    }

    /**
     * Tests that conversations can be created in different blogs, and retrieved
     * appropriately
     */
    @Test
    public void convosInMultiBlogs() {
        Conversation c1 = new Conversation();
        c1.setBlog(MAIN_BLOG_NAME);
        c1.setParticipant("participant1");
        restController.createConversationForBlog(MAIN_BLOG_NAME, c1);
        Conversation c2 = new Conversation();
        c2.setBlog(MAIN_BLOG_NAME);
        c2.setParticipant("participant2");
        restController.createConversationForBlog(MAIN_BLOG_NAME, c2);
        Conversation c3 = new Conversation();
        c3.setBlog(SECOND_BLOG_NAME);
        c3.setParticipant("participant3");
        restController.createConversationForBlog(SECOND_BLOG_NAME, c3);

        List<Conversation> convos = restController.getAllConversationsForBlog(MAIN_BLOG_NAME);
        assertThat(convos).isNotNull();
        assertThat(convos.size()).isEqualTo(2);
        convos = restController.getAllConversationsForBlog(SECOND_BLOG_NAME);
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
        c1.setBlog(MAIN_BLOG_NAME);
        c1.setParticipant("participant1");
        c1.setHideConversation(false);
        restController.createConversationForBlog(MAIN_BLOG_NAME, c1);
        Conversation c2 = new Conversation();
        c2.setBlog(MAIN_BLOG_NAME);
        c2.setParticipant("participant2");
        c2.setHideConversation(false);
        restController.createConversationForBlog(MAIN_BLOG_NAME, c2);
        Conversation c3 = new Conversation();
        c3.setBlog(SECOND_BLOG_NAME);
        c3.setParticipant("participant3");
        c3.setHideConversation(false);
        restController.createConversationForBlog(SECOND_BLOG_NAME, c3);
        Conversation c4 = new Conversation();
        c4.setBlog(SECOND_BLOG_NAME);
        c4.setParticipant("participant4");
        c4.setHideConversation(false);
        restController.createConversationForBlog(SECOND_BLOG_NAME, c4);

        // verify everything is initially correct -- and set it to hidden, right after
        // it's verified
        List<Conversation> convos = restController.getAllConversationsForBlog(MAIN_BLOG_NAME);
        assertThat(convos).isNotNull();
        assertThat(convos.size()).isEqualTo(2);
        for (Conversation c : convos) {
            assertThat(c.getHideConversation()).isEqualTo(false);
            c.setHideConversation(true);
            restController.updateConversationForBlog(MAIN_BLOG_NAME, c.getId(), c);
        }
        convos = restController.getAllConversationsForBlog(SECOND_BLOG_NAME);
        assertThat(convos).isNotNull();
        assertThat(convos.size()).isEqualTo(2);
        for (Conversation c : convos) {
            assertThat(c.getHideConversation()).isEqualTo(false);
            c.setHideConversation(true);
            restController.updateConversationForBlog(SECOND_BLOG_NAME, c.getId(), c);
        }

        // everything should now be hidden, after the previous step; verify it
        convos = restController.getAllConversationsForBlog(MAIN_BLOG_NAME);
        assertThat(convos).isNotNull();
        assertThat(convos.size()).isEqualTo(2);
        for (Conversation c : convos) {
            assertThat(c.getHideConversation()).isEqualTo(true);
        }
        convos = restController.getAllConversationsForBlog(SECOND_BLOG_NAME);
        assertThat(convos).isNotNull();
        assertThat(convos.size()).isEqualTo(2);
        for (Conversation c : convos) {
            assertThat(c.getHideConversation()).isEqualTo(true);
        }

        // unignore all blog1 conversations
        restController.unignoreAllConversationsForBlog(MAIN_BLOG_NAME);

        // verify everything in blog1 is hidden (and unhide it)
        convos = restController.getAllConversationsForBlog(MAIN_BLOG_NAME);
        assertThat(convos).isNotNull();
        assertThat(convos.size()).isEqualTo(2);
        for (Conversation c : convos) {
            assertThat(c.getHideConversation()).isEqualTo(false);
            c.setHideConversation(true);
            restController.updateConversationForBlog(MAIN_BLOG_NAME, c.getId(), c);
        }
        // verify everything in blog2 is hidden
        convos = restController.getAllConversationsForBlog(SECOND_BLOG_NAME);
        assertThat(convos).isNotNull();
        assertThat(convos.size()).isEqualTo(2);
        for (Conversation c : convos) {
            assertThat(c.getHideConversation()).isEqualTo(true);
        }

        // ignore all blog2 conversations
        restController.unignoreAllConversationsForBlog(SECOND_BLOG_NAME);

        // verify everything is good
        convos = restController.getAllConversationsForBlog(MAIN_BLOG_NAME);
        assertThat(convos).isNotNull();
        assertThat(convos.size()).isEqualTo(2);
        for (Conversation c : convos) {
            assertThat(c.getHideConversation()).isEqualTo(true);
        }
        convos = restController.getAllConversationsForBlog(SECOND_BLOG_NAME);
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
        c1.setBlog(MAIN_BLOG_NAME);
        c1.setParticipant("participant1");
        c1.setParticipantAvatarUrl("this is blog1");
        restController.createConversationForBlog(MAIN_BLOG_NAME, c1);
        Conversation c3 = new Conversation();
        c3.setBlog(SECOND_BLOG_NAME);
        c3.setParticipant("participant1");
        c3.setParticipantAvatarUrl("this is blog2");
        restController.createConversationForBlog(SECOND_BLOG_NAME, c3);

        Conversation result = restController.getConversationForBlogByParticipant(MAIN_BLOG_NAME, "participant1");
        assertThat(result).isNotNull();
        assertThat(result.getParticipantAvatarUrl()).isEqualTo("this is blog1");

        result = restController.getConversationForBlogByParticipant(SECOND_BLOG_NAME, "participant1");
        assertThat(result).isNotNull();
        assertThat(result.getParticipantAvatarUrl()).isEqualTo("this is blog2");
    }

}
