package com.tiyb.tev.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TevConvoRestControllerUnitTests {

	@Autowired
	private TEVConvoRestController restController;

	/**
	 * Verifies that updating a Conversation properly updates all fields
	 */
	@Test
	public void updateConversation() {
		Conversation original = new Conversation();

		Conversation modified = restController.createConversation(original);
		assertThat(modified).isNotNull();

		modified.setNumMessages(5);
		modified.setParticipant("new participant");
		modified.setParticipantAvatarUrl("avatar URL");
		modified.setParticipantId("pid123");
		modified.setHideConversation(true);

		restController.updateConversation(modified.getId(), modified);

		Conversation finalFromServer = restController.getConversationByParticipant("new participant");

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

		original = restController.createConversation(original);
		assertThat(original).isNotNull();

		Conversation newConvo = restController.ignoreConversation(participantName);
		assertThat(newConvo).isNotNull();
		assertThat(newConvo.getHideConversation()).isEqualTo(true);

		Conversation finalConvo = restController.getConversationByParticipant(participantName);
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

		original = restController.createConversation(original);
		assertThat(original).isNotNull();

		Conversation newConvo = restController.unignoreConversation(participantName);
		assertThat(newConvo).isNotNull();
		assertThat(newConvo.getHideConversation()).isEqualTo(false);

		Conversation finalConvo = restController.getConversationByParticipant(participantName);
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
		restController.createConversation(convo1);
		Conversation convo2 = new Conversation();
		convo2.setId(2L);
		convo2.setParticipant(participant2);
		convo2.setHideConversation(true);
		restController.createConversation(convo2);

		List<Conversation> allConvos = restController.getAllConversations();
		assertThat(allConvos).isNotNull();
		assertThat(allConvos.size()).isEqualTo(2);

		List<Conversation> allUnHiddenConvos = restController.getUnhiddenConversations();
		assertThat(allUnHiddenConvos).isNotNull();
		assertThat(allUnHiddenConvos.size()).isEqualTo(0);

		restController.unignoreAllConversations();

		List<Conversation> finalList = restController.getUnhiddenConversations();
		assertThat(finalList).isNotNull();
		assertThat(finalList.size()).isEqualTo(2);
	}

	/**
	 * Verifies that updating a Conversation Message properly updates all fields
	 */
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
