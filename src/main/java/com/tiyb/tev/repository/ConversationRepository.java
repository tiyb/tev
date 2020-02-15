package com.tiyb.tev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.Conversation;

/**
 * Repo for Conversations
 *
 * @author tiyb
 *
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Returns all conversations for a given blog
     *
     * @param blog Blog for which to retrieve conversations
     * @return List of conversations
     */
    public List<Conversation> findByBlog(String blog);

    /**
     * Returns a conversation for a given blog, for a given participant
     *
     * @param blog            Blog to search
     * @param participantName Participant to find
     * @return Conversation (if any)
     */
    public Conversation findByBlogAndParticipant(String blog, String participantName);

    /**
     * Returns all conversations for a given blog that are hidden
     *
     * @param blog Blog to search
     * @return All hidden conversations
     */
    public List<Conversation> findByBlogAndHideConversationTrue(String blog);

    /**
     * Returns all conversations for a given blog that are not hidden
     *
     * @param blog Blog to search
     * @return All un-hidden conversations
     */
    public List<Conversation> findByBlogAndHideConversationFalse(String blog);

    /**
     * Returns a list of conversations for a given blog and a given participant ID
     *
     * @param blog          The blog to search
     * @param participantId The participant ID to search
     * @return List of conversations
     */
    public List<Conversation> findByBlogAndParticipantId(String blog, String participantId);

    /**
     * Deletes all conversations for a given blog
     *
     * @param blog Blog to use
     * @return Return code
     */
    Long deleteByBlog(String blog);
}
