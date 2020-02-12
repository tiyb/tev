package com.tiyb.tev.datamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Entity containing a message from a conversation, from Tumblr's messaging
 * system.
 * 
 * @author tiyb
 */
@Entity
@Table(name = "conversation_message")
public class ConversationMessage implements Serializable {

	private static final long serialVersionUID = -319566740443708571L;

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Long id;
	private Long conversationId;
	private Long timestamp;
	private Boolean received;
	/**
	 * one of TEXT, IMAGE, or POSTREF
	 */
	private String type;
	@Lob
	@Column(name = "message", length = 50000)
	private String message;

	/**
	 * Helper method for updating all [updateable] fields with new data
	 * 
	 * @param newData The data to be copied into this object
	 */
	public void updateData(ConversationMessage newData) {
		// this.id = newData.id;
		// this.conversationId = newData.conversationId;
		this.timestamp = newData.timestamp;
		this.received = newData.received;
		this.type = newData.type;
		this.message = newData.message;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ConversationMessage [");
		if (id != null) {
			builder.append("id=");
			builder.append(id);
			builder.append(", ");
		}
		if (received != null) {
			builder.append("received=");
			builder.append(received);
			builder.append(", ");
		}
		if (type != null) {
			builder.append("type=");
			builder.append(type);
			builder.append(", ");
		}
		if (message != null) {
			builder.append("message=");
			builder.append(message);
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

	public Long getConversationId() {
		return conversationId;
	}

	public void setConversationId(Long conversationId) {
		this.conversationId = conversationId;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Boolean getReceived() {
		return received;
	}

	public void setReceived(Boolean received) {
		this.received = received;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
