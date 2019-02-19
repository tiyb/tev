package com.tiyb.tev.xml;

/**
 * Helper class with details for a conversation <b>participant</b>. Not used
 * externally, so therefore not serializable, etc.
 * 
 * @author tiyb
 *
 */
public class Participant {

	private String name;
	private String avatarUrl;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
}
