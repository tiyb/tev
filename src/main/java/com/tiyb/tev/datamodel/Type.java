package com.tiyb.tev.datamodel;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

/**
 * Encapsulates the different "types" of post that are available for Tumblr
 * 
 * @author tiyb
 * @apiviz.landmark
 *
 */
@Entity
@Table(name = "type")
public class Type implements Serializable {
	private static final long serialVersionUID = -6283739216706944117L;
	@Id
	private Long id;
	@NotBlank
	private String type;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Type [");
		if (id != null) {
			builder.append("id=");
			builder.append(id);
			builder.append(", ");
		}
		if (type != null) {
			builder.append("type=");
			builder.append(type);
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
