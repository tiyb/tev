package com.tiyb.tev.datamodel;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entity representing a conversation, from Tumblr's messaging system
 * 
 * @author tiyb
 * @apiviz.landmark
 *
 */
@Entity
@Table(name = "conversation")
public class Conversation implements Serializable {

	private static final long serialVersionUID = 8779758912300542993L;

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Long id;
	private String participant;
	private String participantAvatarUrl;
	private String participantId;
	private Integer numMessages;
	private Boolean hideConversation;
	
	public Conversation() {
		hideConversation = false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Conversation [");
		if (id != null) {
			builder.append("id=");
			builder.append(id);
			builder.append(", ");
		}
		if (participant != null) {
			builder.append("participant=");
			builder.append(participant);
			builder.append(", ");
		}
		if (participantAvatarUrl != null) {
			builder.append("participantAvatarUrl=");
			builder.append(participantAvatarUrl);
			builder.append(", ");
		}
		if (participantId != null) {
			builder.append("participantId=");
			builder.append(participantId);
			builder.append(", ");
		}
		if (numMessages != null) {
			builder.append("numMessages=");
			builder.append(numMessages);
			builder.append(", ");
		}
		if (hideConversation != null) {
			builder.append("hideConversation=");
			builder.append(hideConversation);
		}
		builder.append("]");
		return builder.toString();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getParticipant() {
		return participant;
	}

	public void setParticipant(String participant) {
		this.participant = participant;
	}

	public String getParticipantAvatarUrl() {
		return participantAvatarUrl;
	}

	public void setParticipantAvatarUrl(String participantAvatarUrl) {
		this.participantAvatarUrl = participantAvatarUrl;
	}

	public Integer getNumMessages() {
		return numMessages;
	}

	public void setNumMessages(Integer numMessages) {
		this.numMessages = numMessages;
	}

	public Boolean getHideConversation() {
		return hideConversation;
	}

	public void setHideConversation(Boolean hideConversation) {
		this.hideConversation = hideConversation;
	}

	public String getParticipantId() {
		return participantId;
	}

	public void setParticipantId(String participantId) {
		this.participantId = participantId;
	}
}
