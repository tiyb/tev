package com.tiyb.tev.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.tiyb.tev.TevTestingClass;
import com.tiyb.tev.datamodel.Conversation;
import com.tiyb.tev.datamodel.ConversationMessage;

/**
 * <p>
 * Test cases for unit testing the REST controller for Conversations. Not
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
 */
public class TevConvoRestControllerUnitTests extends TevTestingClass {

    @Autowired
    private TEVConvoRestController restController;

    /**
     * Clean out conversations before each test
     */
    @Before
    public void cleanConversations() {
        restController.deleteAllConvoMsgsForBlog(MAIN_BLOG_NAME);
        restController.deleteAllConversationsForBlog(MAIN_BLOG_NAME);
    }

    /**
     * Verifies that updating a Conversation properly updates all fields
     */
    @Test
    public void updateConversation() {
        Conversation original = new Conversation();

        Conversation modified = restController.createConversationForBlog(MAIN_BLOG_NAME, original);
        assertThat(modified).isNotNull();

        modified.setNumMessages(5);
        modified.setParticipant("new participant");
        modified.setParticipantAvatarUrl("avatar URL");
        modified.setParticipantId("pid123");
        modified.setHideConversation(true);

        restController.updateConversationForBlog(MAIN_BLOG_NAME, modified.getId(), modified);

        Conversation finalFromServer = restController.getConversationForBlogByParticipant(MAIN_BLOG_NAME,
                "new participant");

        assertThat(finalFromServer).isNotNull();
        assertThat(finalFromServer).isEqualToComparingFieldByField(modified);
    }

    /**
     * Verify that marking a Conversation "hidden" in the DB really does mark it
     * hidden
     */
    @Test
    public void markConversationHidden() {
        String participantName = "participant1";

        Conversation original = new Conversation();
        original.setId(1L);
        original.setHideConversation(false);
        original.setParticipant(participantName);
        original.setBlog(MAIN_BLOG_NAME);

        original = restController.createConversationForBlog(MAIN_BLOG_NAME, original);
        assertThat(original).isNotNull();

        Conversation newConvo = restController.ignoreConversationForBlog(MAIN_BLOG_NAME, participantName);
        assertThat(newConvo).isNotNull();
        assertThat(newConvo.getHideConversation()).isEqualTo(true);

        Conversation finalConvo = restController.getConversationForBlogByParticipant(MAIN_BLOG_NAME, participantName);
        assertThat(finalConvo).isNotNull();
        assertThat(finalConvo.getHideConversation()).isEqualTo(true);
    }

    /**
     * Verify that marking a Conversation "un-hidden" in the DB really does un-hide
     * it
     */
    @Test
    public void markConversationUnhidden() {
        String participantName = "participant1";

        Conversation original = new Conversation();
        original.setId(1L);
        original.setHideConversation(true);
        original.setParticipant(participantName);
        original.setBlog(MAIN_BLOG_NAME);

        original = restController.createConversationForBlog(MAIN_BLOG_NAME, original);
        assertThat(original).isNotNull();

        Conversation newConvo = restController.unignoreConversationForBlog(MAIN_BLOG_NAME, participantName);
        assertThat(newConvo).isNotNull();
        assertThat(newConvo.getHideConversation()).isEqualTo(false);

        Conversation finalConvo = restController.getConversationForBlogByParticipant(MAIN_BLOG_NAME, participantName);
        assertThat(finalConvo).isNotNull();
        assertThat(finalConvo.getHideConversation()).isEqualTo(false);
    }

    /**
     * Verifies that the "mark all conversations un-hidden" feature really un-hides
     * all conversations in the DB: 1) create a couple of hidden conversations; 2)
     * verify they're actually hidden in the DB; 3) call the API to un-hide them
     * all; call the API to get all un-hidden conversations; 4) verify that all
     * conversations have been returned, since they should all be un-hidden.
     */
    @Test
    public void markAllConversationsUnhidden() {
        String participant1 = "participant1";
        String participant2 = "participant2";

        Conversation convo1 = new Conversation();
        convo1.setId(1L);
        convo1.setParticipant(participant1);
        convo1.setHideConversation(true);
        restController.createConversationForBlog(MAIN_BLOG_NAME, convo1);
        Conversation convo2 = new Conversation();
        convo2.setId(2L);
        convo2.setParticipant(participant2);
        convo2.setHideConversation(true);
        restController.createConversationForBlog(MAIN_BLOG_NAME, convo2);

        List<Conversation> allConvos = restController.getAllConversationsForBlog(MAIN_BLOG_NAME);
        assertThat(allConvos).isNotNull();
        assertThat(allConvos.size()).isEqualTo(2);

        List<Conversation> allUnHiddenConvos = restController.getUnhiddenConversationsForBlog(MAIN_BLOG_NAME);
        assertThat(allUnHiddenConvos).isNotNull();
        assertThat(allUnHiddenConvos.size()).isEqualTo(0);

        restController.unignoreAllConversationsForBlog(MAIN_BLOG_NAME);

        List<Conversation> finalList = restController.getUnhiddenConversationsForBlog(MAIN_BLOG_NAME);
        assertThat(finalList).isNotNull();
        assertThat(finalList.size()).isEqualTo(2);
    }

    /**
     * Verifies that updating a Conversation Message properly updates all fields
     */
    @Test
    public void updateConvoMsg() {
        Conversation convo = new Conversation();
        convo.setBlog(MAIN_BLOG_NAME);
        convo = restController.createConversationForBlog(MAIN_BLOG_NAME, convo);
        ConversationMessage original = new ConversationMessage();
        original.setConversationId(convo.getId());

        ConversationMessage modified = restController.createConvoMessageForBlog(MAIN_BLOG_NAME, original);
        assertThat(modified).isNotNull();

        modified.setMessage("message");
        modified.setReceived(true);
        modified.setTimestamp(25L);
        modified.setType("IMAGE");

        restController.updateConvoMsgForBlog(MAIN_BLOG_NAME, modified.getId(), modified);

        List<ConversationMessage> finalFromServer = restController.getConvoMsgForBlogByConvoID(MAIN_BLOG_NAME,
                convo.getId());

        assertThat(finalFromServer).isNotNull();
        assertThat(finalFromServer.size()).isEqualTo(1);
        assertThat(finalFromServer.get(0)).isEqualToComparingFieldByField(modified);
    }

}
