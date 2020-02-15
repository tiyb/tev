package com.tiyb.tev.datamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Encapsulates the data needed for a Tumblr "Answer" style of post
 *
 * @author tiyb
 */
@Entity
@Table(name = "answer")
public class Answer implements Serializable, TEVCommonItems<Answer> {

    private static final long serialVersionUID = -4839741081483158077L;

    /**
     * ID of the post to which this answer is associated
     */
    @Id
    private Long postId;

    /**
     * The Question field
     */
    @Lob
    @Column(name = "question", length = Post.LONG_FIELD_SIZE)
    private String question;

    /**
     * The actual answer field (unfortunately ambiguously named with the class)
     */
    @Lob
    @Column(name = "answer", length = Post.LONG_FIELD_SIZE)
    private String answer;

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Answer [");
        if (postId != null) {
            builder.append("postId=");
            builder.append(postId);
            builder.append(", ");
        }
        if (question != null) {
            builder.append("question=");
            builder.append(question);
            builder.append(", ");
        }
        if (answer != null) {
            builder.append("answer=");
            builder.append(answer);
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * Helper method used for updating the fields in an Answer with fields from another Answer
     * object. Ignores ID field.
     *
     * @param newDataObject Object from which to copy the fields
     */
    @Override
    public void updateItem(final Answer newDataObject) {
        this.answer = newDataObject.answer;
        // this.postId = newDataObject.postId;
        this.question = newDataObject.question;
    }

    @Override
    public Long getPostId() {
        return postId;
    }

    @Override
    public void setPostId(final Long postId) {
        this.postId = postId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(final String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(final String answer) {
        this.answer = answer;
    }
}
