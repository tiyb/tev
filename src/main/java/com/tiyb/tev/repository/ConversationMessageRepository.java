package com.tiyb.tev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.ConversationMessage;

/**
 * Repo for Messages within conversations
 *
 * @author tiyb
 *
 */
@Repository
public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

    /**
     * Used for getting all messages from the DB based on the ID of the conversation to which they
     * belong
     *
     * @param conversationId ID of the conversation
     * @return List of messages
     */
    public List<ConversationMessage> findByConversationIdOrderByTimestamp(Long conversationId);
}
