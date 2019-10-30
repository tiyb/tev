package com.tiyb.tev.datamodel.staging;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Encapsulates the data for a Post that's being staged for later export
 * 
 * @author tiyb
 *
 */
@Entity
@Table(name = "statingposts")
public class StagingPost implements Serializable {

	private static final long serialVersionUID = -1260427401761905114L;
	
	@Id
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
