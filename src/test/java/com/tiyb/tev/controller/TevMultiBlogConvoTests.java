package com.tiyb.tev.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import com.tiyb.tev.datamodel.Conversation;
import com.tiyb.tev.html.HtmlTestingClass;

/**
 * Some Conversation-related tests specifically for multi-blog scenarios
 * 
 * @author tiyb
 *
 */
public class TevMultiBlogConvoTests extends HtmlTestingClass {
    @Autowired
    private TEVConvoRestController restController;
    @Autowired
    private TestRestTemplate restTemplate;

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
     * Tests that conversations can be created in different blogs, and retrieved
     * appropriately, via REST
     */
    @Test
    public void convosInMultiBlogsRest() {
        Conversation c1 = new Conversation();
        c1.setBlog(MAIN_BLOG_NAME);
        c1.setParticipant("participant1");
        restTemplate.postForObject(String.format("%s/api/conversations/%s", baseUri(), MAIN_BLOG_NAME), c1, Conversation.class);
        Conversation c2 = new Conversation();
        c2.setBlog(MAIN_BLOG_NAME);
        c2.setParticipant("participant2");
        restTemplate.postForObject(String.format("%s/api/conversations/%s", baseUri(), MAIN_BLOG_NAME), c2, Conversation.class);
        Conversation c3 = new Conversation();
        c3.setBlog(SECOND_BLOG_NAME);
        c3.setParticipant("participant3");
        restTemplate.postForObject(String.format("%s/api/conversations/%s", baseUri(), SECOND_BLOG_NAME), c3, Conversation.class);

        ResponseEntity<Conversation[]> responseEntity = restTemplate.getForEntity(String.format("%s/api/conversations/%s", baseUri(), MAIN_BLOG_NAME), Conversation[].class);
        Conversation[] convos = responseEntity.getBody();
        assertThat(convos).isNotEmpty();
        assertThat(convos.length).isEqualTo(2);
        responseEntity = restTemplate.getForEntity(String.format("%s/api/conversations/%s", baseUri(), SECOND_BLOG_NAME), Conversation[].class);
        convos = responseEntity.getBody();
        assertThat(convos).isNotEmpty();
        assertThat(convos.length).isEqualTo(1);
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
     * Tests that the "un-ignore all" functionality doesn't accidentally
     * cross-pollute other blogs, via REST
     */
    @Test
    public void hideUnhideForMultiBlogsRest() {
        Conversation c1 = new Conversation();
        c1.setBlog(MAIN_BLOG_NAME);
        c1.setParticipant("participant1");
        c1.setHideConversation(false);
        restTemplate.postForObject(String.format("%s/api/conversations/%s", baseUri(), MAIN_BLOG_NAME), c1, Conversation.class);
        Conversation c2 = new Conversation();
        c2.setBlog(MAIN_BLOG_NAME);
        c2.setParticipant("participant2");
        c2.setHideConversation(false);
        restTemplate.postForObject(String.format("%s/api/conversations/%s", baseUri(), MAIN_BLOG_NAME), c2, Conversation.class);
        Conversation c3 = new Conversation();
        c3.setBlog(SECOND_BLOG_NAME);
        c3.setParticipant("participant3");
        c3.setHideConversation(false);
        restTemplate.postForObject(String.format("%s/api/conversations/%s", baseUri(), SECOND_BLOG_NAME), c3, Conversation.class);
        Conversation c4 = new Conversation();
        c4.setBlog(SECOND_BLOG_NAME);
        c4.setParticipant("participant4");
        c4.setHideConversation(false);
        restTemplate.postForObject(String.format("%s/api/conversations/%s", baseUri(), SECOND_BLOG_NAME), c4, Conversation.class);

        // verify everything is initially correct -- and set it to hidden, right after
        // it's verified
        ResponseEntity<Conversation[]> responseEntity = restTemplate.getForEntity(String.format("%s/api/conversations/%s", baseUri(), MAIN_BLOG_NAME), Conversation[].class);
        Conversation[] convos = responseEntity.getBody();
        assertThat(convos).isNotEmpty();
        assertThat(convos.length).isEqualTo(2);
        for (Conversation c : convos) {
            assertThat(c.getHideConversation()).isEqualTo(false);
            c.setHideConversation(true);
            restTemplate.put(String.format("%s/api/conversations/%s/%d", baseUri(), MAIN_BLOG_NAME, c.getId()), c);
        }
        responseEntity = restTemplate.getForEntity(String.format("%s/api/conversations/%s", baseUri(), SECOND_BLOG_NAME), Conversation[].class);
        convos = responseEntity.getBody();
        assertThat(convos).isNotEmpty();
        assertThat(convos.length).isEqualTo(2);
        for (Conversation c : convos) {
            assertThat(c.getHideConversation()).isEqualTo(false);
            c.setHideConversation(true);
            restTemplate.put(String.format("%s/api/conversations/%s/%d", baseUri(), SECOND_BLOG_NAME, c.getId()), c);
        }

        // everything should now be hidden, after the previous step; verify it
        responseEntity = restTemplate.getForEntity(String.format("%s/api/conversations/%s", baseUri(), MAIN_BLOG_NAME), Conversation[].class);
        convos = responseEntity.getBody();
        assertThat(convos).isNotEmpty();
        assertThat(convos.length).isEqualTo(2);
        for (Conversation c : convos) {
            assertThat(c.getHideConversation()).isEqualTo(true);
        }
        responseEntity = restTemplate.getForEntity(String.format("%s/api/conversations/%s", baseUri(), SECOND_BLOG_NAME), Conversation[].class);
        convos = responseEntity.getBody();
        assertThat(convos).isNotEmpty();
        assertThat(convos.length).isEqualTo(2);
        for (Conversation c : convos) {
            assertThat(c.getHideConversation()).isEqualTo(true);
        }

        // unignore all blog1 conversations
        restTemplate.getForObject(String.format("%s/api/conversations/%s/unignoreAllConversations", baseUri(), MAIN_BLOG_NAME), String.class);

        // verify everything in blog1 is hidden (and unhide it)
        responseEntity = restTemplate.getForEntity(String.format("%s/api/conversations/%s", baseUri(), MAIN_BLOG_NAME), Conversation[].class);
        convos = responseEntity.getBody();
        assertThat(convos).isNotEmpty();
        assertThat(convos.length).isEqualTo(2);
        for (Conversation c : convos) {
            assertThat(c.getHideConversation()).isEqualTo(false);
            c.setHideConversation(true);
            restTemplate.put(String.format("%s/api/conversations/%s/%d", baseUri(), MAIN_BLOG_NAME, c.getId()), c);
        }
        // verify everything in blog2 is hidden
        responseEntity = restTemplate.getForEntity(String.format("%s/api/conversations/%s", baseUri(), SECOND_BLOG_NAME), Conversation[].class);
        convos = responseEntity.getBody();
        assertThat(convos).isNotEmpty();
        assertThat(convos.length).isEqualTo(2);
        for (Conversation c : convos) {
            assertThat(c.getHideConversation()).isEqualTo(true);
        }

        // unignore all blog2 conversations
        restTemplate.getForObject(String.format("%s/api/conversations/%s/unignoreAllConversations", baseUri(), SECOND_BLOG_NAME), String.class);

        // verify everything is good
        responseEntity = restTemplate.getForEntity(String.format("%s/api/conversations/%s", baseUri(), MAIN_BLOG_NAME), Conversation[].class);
        convos = responseEntity.getBody();
        assertThat(convos).isNotEmpty();
        assertThat(convos.length).isEqualTo(2);
        for (Conversation c : convos) {
            assertThat(c.getHideConversation()).isEqualTo(true);
        }
        responseEntity = restTemplate.getForEntity(String.format("%s/api/conversations/%s", baseUri(), SECOND_BLOG_NAME), Conversation[].class);
        convos = responseEntity.getBody();
        assertThat(convos).isNotEmpty();
        assertThat(convos.length).isEqualTo(2);
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

    /**
     * Tests that conversations with the same participant can be stored across
     * different blogs, via REST.
     */
    @Test
    public void twoBlogsOneParticipantRest() {
        Conversation c1 = new Conversation();
        c1.setBlog(MAIN_BLOG_NAME);
        c1.setParticipant("participant1");
        c1.setParticipantAvatarUrl("this is blog1");
        restTemplate.postForObject(String.format("%s/api/conversations/%s", baseUri(), MAIN_BLOG_NAME), c1, Conversation.class);
        Conversation c3 = new Conversation();
        c3.setBlog(SECOND_BLOG_NAME);
        c3.setParticipant("participant1");
        c3.setParticipantAvatarUrl("this is blog2");
        restTemplate.postForObject(String.format("%s/api/conversations/%s", baseUri(), SECOND_BLOG_NAME), c3, Conversation.class);

        Conversation result = restTemplate.getForObject(String.format("%s/api/conversations/%s/byParticipant/%s", baseUri(), MAIN_BLOG_NAME, "participant1"), Conversation.class);
        assertThat(result).isNotNull();
        assertThat(result.getParticipantAvatarUrl()).isEqualTo("this is blog1");

        result = restTemplate.getForObject(String.format("%s/api/conversations/%s/byParticipant/%s", baseUri(), SECOND_BLOG_NAME, "participant1"), Conversation.class);
        assertThat(result).isNotNull();
        assertThat(result.getParticipantAvatarUrl()).isEqualTo("this is blog2");
    }

}
