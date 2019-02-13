package com.tiyb.tev.datamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

/**
 * Encapsulates the data needed for a Tumblr "Answer" style of post
 * 
 * @author tiyb
 * @apiviz.landmark
 *
 */
@Entity
@Table(name = "answer")
public class Answer implements Serializable {

	private static final long serialVersionUID = -4839741081483158077L;
	@Id
	private Long postId;
	@NotBlank
	@Lob
	@Column(name="question", length=50000)
	private String question;
	@NotBlank
	@Lob
	@Column(name="answer", length=50000)
	private String answer;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
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

	public Long getPostId() {
		return postId;
	}

	public void setPostId(Long postId) {
		this.postId = postId;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}
}
