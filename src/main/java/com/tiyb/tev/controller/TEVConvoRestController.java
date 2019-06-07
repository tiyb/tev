package com.tiyb.tev.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import com.tiyb.tev.exception.ResourceNotFoundException;
import com.tiyb.tev.repository.ConversationMessageRepository;
import com.tiyb.tev.repository.ConversationRepository;

@RestController
@RequestMapping("/api")
public class TEVConvoRestController {

	@Autowired
	ConversationRepository conversationRepo;
	@Autowired
	ConversationMessageRepository convoMsgRepo;
	/**
	 * GET request for listing all conversations
	 * 
	 * @return <code>List</code> of all conversations in the database
	 */
	@GetMapping("/conversations")
	public List<Conversation> getAllConversations() {
		return conversationRepo.findAll();
	}

	/**
	 * GET to return a single conversation, by ID
	 * 
	 * @param conversationID the conversation ID
	 * @return The Conversation details
	 */
	@GetMapping("/conversations/{id}")
	public Conversation getConversationById(@PathVariable(value = "id") Long conversationID) {
		return conversationRepo.findById(conversationID)
				.orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationID));
	}

	/**
	 * GET to return a single conversation, by participant name
	 * 
	 * @param participantName Name of the participant
	 * @return Single Conversation
	 */
	@GetMapping("/conversations/{participantName}")
	public Conversation getConversationByParticipant(@RequestParam("participantName") String participantName) {
		Conversation convo = conversationRepo.findByParticipant(participantName);

		if (convo == null) {
			throw new ResourceNotFoundException("Conversation", "name", participantName);
		}

		return convo;
	}

	/**
	 * GET to return a single conversation, by participant ID. Because some
	 * conversations (where all messages are from the main blog, and none are from
	 * the participant) will have no conversation ID, a backup method is provided
	 * whereby conversations can be searched by Participant Name instead of ID.
	 * 
	 * @param participantId   ID of the participant
	 * @param participantName Name of the participant; not used if ID is
	 *                        successfully used to retrieve a Conversation
	 * @return Single Conversation
	 */
	@GetMapping("/conversations/id/{participantId}/{participantName}")
	public Conversation getConversationByParticipantIdOrName(@RequestParam("participantId") String participantId,
			@RequestParam("participantName") String participantName) {
		List<Conversation> convos = conversationRepo.findByParticipantId(participantId);
		if (convos.size() == 1) {
			return convos.get(0);
		}

		Conversation convoByName = conversationRepo.findByParticipant(participantName);

		if (convoByName != null) {
			return convoByName;
		} else {
			throw new ResourceNotFoundException("Conversation", "id", participantId);
		}
	}

	/**
	 * Returns all conversations that are not set to "hidden" status
	 * 
	 * @return <code>List</code> of conversations
	 */
	@GetMapping("/conversations/unhidden")
	public List<Conversation> getUnhiddenConversations() {
		return conversationRepo.findByHideConversationFalse();
	}

	/**
	 * POST request to submit a conversation into the system
	 * 
	 * @param conversation The conversation object (in JSON format) to be saved into
	 *                     the database
	 * @return The same object that was saved (including the generated ID)
	 */
	@PostMapping("/conversations")
	public Conversation createConversation(@Valid @RequestBody Conversation conversation) {
		return conversationRepo.save(conversation);
	}

	/**
	 * PUT to update a conversation
	 * 
	 * @param conversationId ID of the convo to be updated
	 * @param convoDetails   Details to be inserted into the DB
	 * @return The updated Conversation details
	 */
	@PutMapping("/conversations/{id}")
	public Conversation updateConversation(@PathVariable(value = "id") Long conversationId,
			@RequestBody Conversation convoDetails) {
		Conversation convo = conversationRepo.findById(conversationId)
				.orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

		convo.setParticipant(convoDetails.getParticipant());
		convo.setParticipantAvatarUrl(convoDetails.getParticipantAvatarUrl());
		convo.setNumMessages(convoDetails.getNumMessages());
		convo.setHideConversation(convoDetails.getHideConversation());
		convo.setParticipantId(convoDetails.getParticipantId());

		Conversation updatedConvo = conversationRepo.save(convo);

		return updatedConvo;
	}

	/**
	 * Sets a conversation to "ignored" status
	 * 
	 * @param participantName Name of the participant in the conversation to be
	 *                        ignored
	 * @return Updated Conversation
	 */
	@PutMapping("/conversations/{participant}/ignoreConvo")
	public Conversation ignoreConversation(@PathVariable(value = "participant") String participantName) {
		Conversation convo = conversationRepo.findByParticipant(participantName);

		convo.setHideConversation(true);

		convo = conversationRepo.save(convo);

		return convo;
	}

	/**
	 * Sets a conversation to "un-ignored" status
	 * 
	 * @param participantName Name of the participant in the conversation to be
	 *                        ignored
	 * @return Updated Conversation
	 */
	@PutMapping("/conversations/{participant}/unignoreConvo")
	public Conversation unignoreConversation(@PathVariable(value = "participant") String participantName) {
		Conversation convo = conversationRepo.findByParticipant(participantName);

		convo.setHideConversation(false);

		convo = conversationRepo.save(convo);

		return convo;
	}

	/**
	 * Used to reset all conversations back to an un-hidden state
	 */
	@GetMapping("/conversations/unignoreAllConversations")
	public void unignoreAllConversations() {
		List<Conversation> hiddenConvos = conversationRepo.findByHideConversationTrue();

		if (hiddenConvos.size() < 1) {
			return;
		}

		for (Conversation convo : hiddenConvos) {
			convo.setHideConversation(false);
			conversationRepo.save(convo);
		}

		return;
	}

	/**
	 * DEL to delete a single conversation, by ID
	 * 
	 * @param convoId ID of convo to be deleted
	 * @return <code>ResponseEntity</code> with the response details
	 */
	@DeleteMapping("/conversations/{id}")
	public ResponseEntity<?> deleteConversation(@PathVariable(value = "id") Long convoId) {
		Conversation convo = conversationRepo.findById(convoId)
				.orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", convoId));

		conversationRepo.delete(convo);

		return ResponseEntity.ok().build();
	}

	/**
	 * DEL to delete all conversations in the DB
	 * 
	 * @return <code>ResponseEntity</code> with the response details
	 */
	@DeleteMapping("/conversations")
	public ResponseEntity<?> deleteAllConversations() {
		conversationRepo.deleteAll();

		return ResponseEntity.ok().build();
	}

	/**
	 * GET request for listing all messages (regardless of conversation)
	 * 
	 * @return list of all messages in the database
	 */
	@GetMapping("/conversations/messages")
	public List<ConversationMessage> getAllConversationMessages() {
		return convoMsgRepo.findAll();
	}

	/**
	 * POST request to submit a new conversation message into the system
	 * 
	 * @param convoMsg The data to be submitted
	 * @return The object that was just submitted (with ID)
	 */
	@PostMapping("/conversations/messages")
	public ConversationMessage createConvoMessage(@Valid @RequestBody ConversationMessage convoMsg) {
		return convoMsgRepo.save(convoMsg);
	}

	/**
	 * GET to return all messages for a particular conversation
	 * 
	 * @param convoId The conversation ID
	 * @return The list of messages
	 */
	@GetMapping("/conversations/{id}/messages")
	public List<ConversationMessage> getConvoMsgByConvoID(@PathVariable(value = "id") Long convoId) {
		return convoMsgRepo.findByConversationIdOrderByTimestamp(convoId);
	}

	/**
	 * PUT to update a conversation message
	 * 
	 * @param msgId    The ID of the message to be updated
	 * @param convoMsg The updated data
	 * @return The same data that was just submitted
	 */
	@PutMapping("/conversations/messages/{id}")
	public ConversationMessage updateConvoMsg(@PathVariable(value = "id") Long msgId,
			@RequestBody ConversationMessage convoMsg) {
		ConversationMessage cm = convoMsgRepo.findById(msgId)
				.orElseThrow(() -> new ResourceNotFoundException("ConversationMessage", "id", msgId));

		cm.setConversationId(convoMsg.getConversationId());
		cm.setMessage(convoMsg.getMessage());
		cm.setReceived(convoMsg.getReceived());
		cm.setType(convoMsg.getType());
		cm.setTimestamp(convoMsg.getTimestamp());

		ConversationMessage updatedCM = convoMsgRepo.save(cm);

		return updatedCM;
	}

	/**
	 * DEL to delete all conversation messages in the DB
	 * 
	 * @return <code>ResponseEntity</code> with the response details
	 */
	@DeleteMapping("/conversations/messages")
	public ResponseEntity<?> deleteAllConvoMsgs() {
		convoMsgRepo.deleteAll();

		return ResponseEntity.ok().build();
	}

	/**
	 * DEL to delete a single conversation message, by ID
	 * 
	 * @param msgId ID of the message to be deleted
	 * @return <code>ResponseEntity</code> with the response details
	 */
	@DeleteMapping("/conversations/messages/{id}")
	public ResponseEntity<?> deleteConversationMessage(@PathVariable(name = "id") Long msgId) {
		ConversationMessage cm = convoMsgRepo.findById(msgId)
				.orElseThrow(() -> new ResourceNotFoundException("ConversationMessage", "id", msgId));

		convoMsgRepo.delete(cm);

		return ResponseEntity.ok().build();
	}
}
