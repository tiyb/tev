package com.tiyb.tev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiyb.tev.datamodel.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

	public List<Conversation> findByBlog(String blog);
	public Conversation findByBlogAndParticipant(String blog, String participantName);
	public List<Conversation> findByBlogAndHideConversationTrue(String blog);
	public List<Conversation> findByBlogAndHideConversationFalse(String blog);
	public List<Conversation> findByBlogAndParticipantId(String blog, String participantId);
	Long deleteByBlog(String blog);
}
