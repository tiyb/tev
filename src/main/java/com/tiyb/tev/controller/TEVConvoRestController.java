package com.tiyb.tev.controller;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tiyb.tev.datamodel.Conversation;
import com.tiyb.tev.datamodel.ConversationMessage;
import com.tiyb.tev.exception.InvalidConvoParentException;
import com.tiyb.tev.exception.ResourceNotFoundException;
import com.tiyb.tev.repository.ConversationMessageRepository;
import com.tiyb.tev.repository.ConversationRepository;

/**
 * REST controller for working with Conversations and Conversation Messages. All
 * Conversation APIs are blog-specific; Conversation Message APIs aren't, since
 * they are tied to specific Conversations already.
 * 
 * @author tiyb
 */
@RestController
@RequestMapping("/api")
public class TEVConvoRestController {

	private Logger logger = LoggerFactory.getLogger(TEVConvoRestController.class);

	/**
	 * The Repo for working with Conversation data
	 */
	@Autowired
	ConversationRepository convoRepo;

	/**
	 * The Repo for working with Conversation Message data
	 */
	@Autowired
	ConversationMessageRepository msgRepo;

	/**
	 * GET request for listing all conversations for a given blog
	 * 
	 * @param blog Blog for which to return conversations
	 * @return {@link java.util.List List} of all conversations in the database
	 */
	@GetMapping("/conversations/{blog}")
	public List<Conversation> getAllConversationsForBlog(@PathVariable("blog") String blog) {
		return convoRepo.findByBlog(blog);
	}

	/**
	 * GET to return a single conversation by ID for a given blog
	 * 
	 * @param blog           Not used
	 * @param conversationID the conversation ID
	 * @return The Conversation details
	 */
	@GetMapping("/conversations/{blog}/{id}")
	public Conversation getConversationForBlogById(@PathVariable("blog") String blog,
			@PathVariable("id") Long conversationID) {
		return convoRepo.findById(conversationID)
				.orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationID));
	}

	/**
	 * GET to return a single conversation, by participant name, for a given blog
	 * 
	 * @param participantName Name of the participant
	 * @return Single Conversation
	 */
	@GetMapping("/conversations/{blog}/{participantName}")
	public Conversation getConversationForBlogByParticipant(@RequestParam("blog") String blog,
			@RequestParam("participantName") String participantName) {
		Conversation convo = convoRepo.findByBlogAndParticipant(blog, participantName);

		if (convo == null) {
			throw new ResourceNotFoundException("Conversation", "name", participantName);
		}

		return convo;
	}

	/**
	 * GET to return a single conversation by participant ID. Because some
	 * conversations (where all messages are from the main blog and none are from
	 * the participant) will have no conversation ID, a backup method is provided
	 * whereby conversations can be searched by Participant Name instead of ID. So
	 * if the initial search (by Participant ID) returns no results, a secondary
	 * search (by Participant name) will be attempted
	 * 
	 * @param blog            Blog to be searched
	 * @param participantId   ID of the participant
	 * @param participantName Name of the participant; not used if ID is
	 *                        successfully used to retrieve a Conversation
	 * @return Single Conversation
	 */
	@GetMapping("/conversations/{blog}/id/{participantId}/{participantName}")
	public Conversation getConversationForBlogByParticipantIdOrName(@PathVariable("blog") String blog,
			@PathVariable("participantId") String participantId,
			@PathVariable("participantName") String participantName) {
		List<Conversation> convos = convoRepo.findByBlogAndParticipantId(blog, participantId);
		if (convos.size() == 1) {
			return convos.get(0);
		}

		Conversation convoByName = convoRepo.findByBlogAndParticipant(blog, participantName);

		if (convoByName != null) {
			return convoByName;
		} else {
			throw new ResourceNotFoundException("Conversation", "id", participantId);
		}
	}

	/**
	 * Returns all conversations that are not set to "hidden" status for a given
	 * blog
	 * 
	 * @param blog Blog for which conversations should be returned
	 * @return {@link java.util.List List} of conversations
	 */
	@GetMapping("/conversations/{blog}/unhidden")
	public List<Conversation> getUnhiddenConversationsForBlog(@PathVariable("blog") String blog) {
		return convoRepo.findByBlogAndHideConversationFalse(blog);
	}

	/**
	 * Returns all conversations that are set to "hidden" status for a given blog.
	 * Not used by TEV, but included for the sake of completeness.
	 * 
	 * @param blog Blog for which conversations should be returned
	 * @return {@link java.util.List List} of conversations
	 */
	@GetMapping("/conversations/{blog}/hidden")
	public List<Conversation> getHiddenConversationsForBlog(@PathVariable("blog") String blog) {
		return convoRepo.findByBlogAndHideConversationTrue(blog);
	}

	/**
	 * POST request to submit a conversation into the system for a given blog.
	 * Instead of validating that the blog in the conversation property and the blog
	 * passed on the URL match, the object is simply updated with the value passed.
	 * 
	 * @param blog         The blog for which this conversation should be submitted
	 * @param conversation The conversation object (in JSON format) to be saved into
	 *                     the database
	 * @return The same object that was saved (including the generated ID)
	 */
	@PostMapping("/conversations/{blog}")
	public Conversation createConversationForBlog(@PathVariable("blog") String blog,
			@Valid @RequestBody Conversation conversation) {
		conversation.setBlog(blog);
		return convoRepo.save(conversation);
	}

	/**
	 * PUT to update a conversation for a given blog
	 * 
	 * @param blog           Used only for validation purposes
	 * @param conversationId ID of the convo to be updated
	 * @param convoDetails   Details to be inserted into the DB
	 * @return The updated Conversation details
	 */
	@PutMapping("/conversations/{blog}/{id}")
	public Conversation updateConversationForBlog(@PathVariable("blog") String blog,
			@PathVariable("id") Long conversationId, @RequestBody Conversation convoDetails) {
		assert blog.equals(convoDetails.getBlog());

		Conversation convo = convoRepo.findById(conversationId)
				.orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

		convo.updateData(convoDetails);

		return convoRepo.save(convo);
	}

	/**
	 * Sets a conversation to "ignored" status for a given blog. Note that the blog
	 * is still needed, not just for the URL but because multiple blogs could have
	 * had conversations with the same participant.
	 * 
	 * @param blog            Blog for which the conversation should be updated.
	 * @param participantName Name of the participant in the conversation to be
	 *                        ignored
	 * @return Updated Conversation
	 */
	@PutMapping("/conversations/{blog}/{participant}/ignoreConvo")
	public Conversation ignoreConversationForBlog(@PathVariable("blog") String blog,
			@PathVariable("participant") String participantName) {
		Conversation convo = convoRepo.findByBlogAndParticipant(blog, participantName);

		convo.setHideConversation(true);

		convo = convoRepo.save(convo);

		return convo;
	}

	/**
	 * Sets a conversation to "un-ignored" status for a given blog
	 * 
	 * @param blog            Blog for which the conversation should be updated
	 * @param participantName Name of the participant in the conversation to be
	 *                        ignored
	 * @return Updated Conversation
	 */
	@PutMapping("/conversations/{blog}/{participant}/unignoreConvo")
	public Conversation unignoreConversationForBlog(@PathVariable("blog") String blog,
			@PathVariable("participant") String participantName) {
		Conversation convo = convoRepo.findByBlogAndParticipant(blog, participantName);

		convo.setHideConversation(false);

		convo = convoRepo.save(convo);

		return convo;
	}

	/**
	 * Used to reset all conversations back to an un-hidden state for a given blog
	 * 
	 * @param blog Blog for which conversations should be un-hidden
	 */
	@GetMapping("/conversations/{blog}/unignoreAllConversations")
	public void unignoreAllConversationsForBlog(@PathVariable("blog") String blog) {
		List<Conversation> hiddenConvos = convoRepo.findByBlogAndHideConversationTrue(blog);

		if (hiddenConvos.size() < 1) {
			return;
		}

		for (Conversation convo : hiddenConvos) {
			convo.setHideConversation(false);
			convoRepo.save(convo);
		}

		return;
	}

	/**
	 * DEL to delete a single conversation by ID for a given blog
	 * 
	 * @param blog    Used for validation purposes
	 * @param convoId ID of convo to be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/conversations/{blog}/{id}")
	public ResponseEntity<?> deleteConversation(@PathVariable("blog") String blog, @PathVariable("id") Long convoId) {
		Conversation convo = convoRepo.findById(convoId)
				.orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", convoId));

		if (!blog.equals(convo.getBlog())) {
			logger.error("Attempt to update conversation but blog doesn't match: " + blog);
			throw new InvalidConvoParentException();
		}
		convoRepo.delete(convo);

		return ResponseEntity.ok().build();
	}

	/**
	 * DEL to delete all conversations in the DB for a given blog
	 * 
	 * @param blog Blog for which conversations should be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@Transactional
	@DeleteMapping("/conversations/{blog}")
	public ResponseEntity<?> deleteAllConversationsForBlog(@PathVariable("blog") String blog) {
		convoRepo.deleteByBlog(blog);

		return ResponseEntity.ok().build();
	}

	/**
	 * GET request for listing all messages (regardless of conversation)
	 * 
	 * TODO remove this?
	 * 
	 * @return list of all messages in the database
	 */
	@Deprecated
	@GetMapping("/conversations/messages")
	public List<ConversationMessage> getAllConversationMessages() {
		return msgRepo.findAll();
	}

	/**
	 * POST request to submit a new conversation message into the system
	 * 
	 * @param convoMsg The data to be submitted
	 * @return The object that was just submitted (with ID)
	 */
	@PostMapping("/conversations/{blog}/messages")
	public ConversationMessage createConvoMessageForBlog(@PathVariable("blog") String blog,
			@Valid @RequestBody ConversationMessage convoMsg) {
		Optional<Conversation> optConvo = convoRepo.findById(convoMsg.getConversationId());
		if (!optConvo.isPresent()) {
			logger.error("Invalid convo/message combo; convoID=" + convoMsg.getConversationId());
			throw new InvalidConvoParentException();
		} else {
			if (!optConvo.get().getBlog().equals(blog)) {
				logger.error("invalid message/blog combo; convoID=" + convoMsg.getConversationId() + ", blog=" + blog);
				throw new InvalidConvoParentException();
			}
		}
		return msgRepo.save(convoMsg);
	}

	/**
	 * GET to return all messages for a particular conversation for a given blog
	 * 
	 * @param blog    Not used
	 * @param convoId The conversation ID
	 * @return The list of messages
	 */
	@GetMapping("/conversations/{blog}/{id}/messages")
	public List<ConversationMessage> getConvoMsgForBlogByConvoID(@PathVariable("blog") String blog,
			@PathVariable("id") Long convoId) {
		return msgRepo.findByConversationIdOrderByTimestamp(convoId);
	}

	/**
	 * PUT to update a conversation message for a given blog
	 * 
	 * @param blog     Not used
	 * @param msgId    The ID of the message to be updated
	 * @param convoMsg The updated data
	 * @return The same data that was just submitted
	 */
	@PutMapping("/conversations/{blog}/messages/{id}")
	public ConversationMessage updateConvoMsgForBlog(@PathVariable("blog") String blog, @PathVariable("id") Long msgId,
			@RequestBody ConversationMessage convoMsg) {
		ConversationMessage cm = msgRepo.findById(msgId)
				.orElseThrow(() -> new ResourceNotFoundException("ConversationMessage", "id", msgId));

		cm.updateData(convoMsg);

		ConversationMessage updatedCM = msgRepo.save(cm);

		return updatedCM;
	}

	/**
	 * DEL to delete all conversation messages in the DB for a given blog
	 * 
	 * @param blog The blog for which convo messsages should be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/conversations/{blog}/messages")
	public ResponseEntity<?> deleteAllConvoMsgsForBlog(@PathVariable("blog") String blog) {
		List<Conversation> convosForBlog = convoRepo.findByBlog(blog);

		for (Conversation c : convosForBlog) {
			List<ConversationMessage> msgs = msgRepo.findByConversationIdOrderByTimestamp(c.getId());
			for (ConversationMessage cm : msgs) {
				msgRepo.delete(cm);
			}
		}

		return ResponseEntity.ok().build();
	}

	/**
	 * DEL to delete a single conversation message, by ID
	 * 
	 * @param blog  Not used
	 * @param msgId ID of the message to be deleted
	 * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with
	 *         the response details
	 */
	@DeleteMapping("/conversations/{blog}/messages/{id}")
	public ResponseEntity<?> deleteConversationMessageForBlog(@PathVariable("blog") String blog,
			@PathVariable("id") Long msgId) {
		ConversationMessage cm = msgRepo.findById(msgId)
				.orElseThrow(() -> new ResourceNotFoundException("ConversationMessage", "id", msgId));

		msgRepo.delete(cm);

		return ResponseEntity.ok().build();
	}
}
