package com.tiyb.tev.datamodel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import com.tiyb.tev.repository.ConversationMessageRepository;
import com.tiyb.tev.repository.ConversationRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ConversationRepositoryUnitTest {

	@Autowired
	private TestEntityManager entityManager;
	
	@Autowired
	private ConversationRepository convoRepo;
	
	@Autowired
	private ConversationMessageRepository convoMsgRepo;
	
	@Test
	public void findConversationByParticipant() {
		Conversation convo = new Conversation();
		convo.setNumMessages(2);
		convo.setParticipant("convo participant");
		convo.setParticipantAvatarUrl("http://www.participant1.com");
		
		entityManager.persist(convo);
		entityManager.flush();
		
		Conversation foundConvo = convoRepo.findByParticipant("convo participant");
		
		assertThat(foundConvo.getId()).isEqualTo(convo.getId());
		assertThat(foundConvo.getParticipant()).isEqualTo(convo.getParticipant());
		assertThat(foundConvo.getParticipantAvatarUrl()).isEqualTo(convo.getParticipantAvatarUrl());
	}
	
	@Test
	public void findConvoMsgsByConvoID() {
		ConversationMessage msg1 = new ConversationMessage();
		msg1.setConversationId(1L);
		msg1.setMessage("first message");
		msg1.setReceived(false);
		msg1.setTimestamp(2L);
		entityManager.persist(msg1);
		ConversationMessage msg2 = new ConversationMessage();
		msg2.setConversationId(1L);
		msg2.setMessage("second message");
		msg2.setReceived(false);
		msg2.setTimestamp(1L);
		entityManager.persist(msg2);
		entityManager.flush();
		
		List<ConversationMessage> returnedMessages = convoMsgRepo.findByConversationIdOrderByTimestamp(1L);
		
		assertThat(returnedMessages.size()).isEqualTo(2);
		assertThat(returnedMessages.get(0).getMessage()).isEqualTo(msg2.getMessage());
		assertThat(returnedMessages.get(1).getMessage()).isEqualTo(msg1.getMessage());
	}
}
