package com.tiyb.tev.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Raised when Conversation XML is being parsed, and the name of the blog in the Metadata doesn't
 * match the name of the blog in the XML. Since the metadata has to be created first, before the
 * Conversation XML is parsed -- if there even <i>is</i> Conversation XML -- this is the only way to
 * handle this situation.
 *
 * @author tiyb
 *
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BlogMismatchParsingException extends XMLParsingException {

    private static final long serialVersionUID = 6991314129088247089L;

    /**
     * Name of the blog that was specified
     */
    private final String blogName;

    /**
     * Name of the 'main participant' that was specified
     */
    private final String mainParticipantName;

    /**
     * Constructor accepting params
     *
     * @param blogName            Name of the blog
     * @param mainParticipantName Name of the participant
     */
    public BlogMismatchParsingException(final String blogName, final String mainParticipantName) {
        this.blogName = blogName;
        this.mainParticipantName = mainParticipantName;
    }

    public String getBlogName() {
        return blogName;
    }

    public String getMainParticipantName() {
        return mainParticipantName;
    }

}
