package com.tiyb.tev.datamodel;

import java.util.List;

/**
 * Some of the IDs for conversations and conversation messages get generated
 * automatically by the DB, so it's difficult to handle uploading conversations
 * and messages without keeping them together, similar to how they're kept
 * together in the XML export from Tumblr.
 * 
 * @author tiyb
 *
 */
public class FullConversation {

    private Conversation conversation;
    private List<ConversationMessage> messages;

    public FullConversation(Conversation conversation, List<ConversationMessage> messages) {
        super();
        this.conversation = conversation;
        this.messages = messages;
    }

    public FullConversation() {

    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public List<ConversationMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ConversationMessage> messages) {
        this.messages = messages;
    }
}
