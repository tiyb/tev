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
 */
@Entity
@Table(name = "conversation")
public class Conversation implements Serializable {

    private static final long serialVersionUID = 8779758912300542993L;

    /**
     * Unique ID for the conversation
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * Participant with which the conversation is being held
     */
    private String participant;

    /**
     * URL to the participant's avatar image
     */
    private String participantAvatarUrl;

    /**
     * Unique ID of the participant
     */
    private String participantId;

    /**
     * Number of messages in the conversation
     */
    private Integer numMessages;

    /**
     * Whether the conversation is marked "hidden" in the DB
     */
    private Boolean hideConversation;

    /**
     * Blog for which the conversation was held
     */
    private String blog;

    /**
     * Default constructor, ensures that new conversations are initially hidden
     */
    public Conversation() {
        hideConversation = false;
    }

    /**
     * Helper function for updating all of the object's properties with values from a new object. ID
     * is ignored.
     *
     * @param newData The new object from which to take the values
     */
    public void updateData(final Conversation newData) {
        // this.id = newData.id;
        this.participant = newData.participant;
        this.participantAvatarUrl = newData.participantAvatarUrl;
        this.participantId = newData.participantId;
        this.numMessages = newData.numMessages;
        this.hideConversation = newData.hideConversation;
        this.blog = newData.blog;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
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
        if (blog != null) {
            builder.append("blog=");
            builder.append(blog);
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

    public void setId(final Long id) {
        this.id = id;
    }

    public String getParticipant() {
        return participant;
    }

    public void setParticipant(final String participant) {
        this.participant = participant;
    }

    public String getParticipantAvatarUrl() {
        return participantAvatarUrl;
    }

    public void setParticipantAvatarUrl(final String participantAvatarUrl) {
        this.participantAvatarUrl = participantAvatarUrl;
    }

    public Integer getNumMessages() {
        return numMessages;
    }

    public void setNumMessages(final Integer numMessages) {
        this.numMessages = numMessages;
    }

    public Boolean getHideConversation() {
        return hideConversation;
    }

    public void setHideConversation(final Boolean hideConversation) {
        this.hideConversation = hideConversation;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(final String participantId) {
        this.participantId = participantId;
    }

    public String getBlog() {
        return blog;
    }

    public void setBlog(final String blog) {
        this.blog = blog;
    }
}
