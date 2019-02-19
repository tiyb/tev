package com.tiyb.tev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.ConversationMessage;

@Repository
public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

	public List<ConversationMessage> findByConversationIdOrderByTimestamp(Long conversationId);
}
