package com.tiyb.tev.datamodel;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Encapsulates metadata stored in the database for use by the application.
 * 
 * @author tiyb
 * @apiviz.landmark
 *
 */
@Entity
@Table(name = "metadata")
public class Metadata implements Serializable {

	private static final long serialVersionUID = -2517986171637243590L;

	@Id
	private Integer id;
	private String baseMediaPath;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Metadata [");
		if (id != null) {
			builder.append("id=");
			builder.append(id);
			builder.append(", ");
		}
		if (baseMediaPath != null) {
			builder.append("baseMediaPath=");
			builder.append(baseMediaPath);
		}
		builder.append("]");
		return builder.toString();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getBaseMediaPath() {
		return baseMediaPath;
	}

	public void setBaseMediaPath(String baseMediaPath) {
		this.baseMediaPath = baseMediaPath;
	}
}
