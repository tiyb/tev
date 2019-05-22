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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Participant [");
		if (name != null) {
			builder.append("name=");
			builder.append(name);
			builder.append(", ");
		}
		if (avatarUrl != null) {
			builder.append("avatarUrl=");
			builder.append(avatarUrl);
		}
		builder.append("]");
		return builder.toString();
	}

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
