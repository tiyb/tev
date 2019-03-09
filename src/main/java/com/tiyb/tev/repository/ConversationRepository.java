package com.tiyb.tev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

	public Conversation findByParticipant(String participantName);
	public List<Conversation> findByHideConversationTrue();
	public List<Conversation> findByHideConversationFalse();
}
