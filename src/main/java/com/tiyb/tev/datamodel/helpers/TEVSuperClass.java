package com.tiyb.tev.datamodel.helpers;

import java.util.ArrayList;
import java.util.List;

import com.tiyb.tev.datamodel.Answer;
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.datamodel.Video;

/**
 * This is a helper class containing multiple objects from the rest of the data
 * model.
 * 
 * @author tiyb
 * @apiviz.landmark
 * @apiviz.uses com.tiyb.tev.datamodel.Post
 * @apiviz.uses com.tiyb.tev.datamodel.Answer
 * @apiviz.uses com.tiyb.tev.datamodel.Link
 * @apiviz.uses com.tiyb.tev.datamodel.Photo
 * @apiviz.uses com.tiyb.tev.datamodel.Regular
 * @apiviz.uses com.tiyb.tev.datamodel.Video
 *
 */
public class TEVSuperClass {

	private List<Post> posts;
	private List<Answer> answers;
	private List<Link> links;
	private List<Photo> photos;
	private List<Regular> regulars;
	private List<Video> videos;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TEVSuperClass [");
		if (posts != null) {
			builder.append("posts=");
			builder.append(posts);
			builder.append(", ");
		}
		if (answers != null) {
			builder.append("answers=");
			builder.append(answers);
			builder.append(", ");
		}
		if (links != null) {
			builder.append("links=");
			builder.append(links);
			builder.append(", ");
		}
		if (photos != null) {
			builder.append("photos=");
			builder.append(photos);
			builder.append(", ");
		}
		if (regulars != null) {
			builder.append("regulars=");
			builder.append(regulars);
			builder.append(", ");
		}
		if (videos != null) {
			builder.append("videos=");
			builder.append(videos);
			builder.append(", ");
		}
		builder.append("]");
		return builder.toString();
	}

	public TEVSuperClass() {
		this.photos = new ArrayList<Photo>();
		this.answers = new ArrayList<Answer>();
		this.links = new ArrayList<Link>();
		this.posts = new ArrayList<Post>();
		this.regulars = new ArrayList<Regular>();
		this.videos = new ArrayList<Video>();
	}
	
	public List<Post> getPosts() {
		return posts;
	}

	public void setPosts(List<Post> posts) {
		this.posts = posts;
	}

	public List<Answer> getAnswers() {
		return answers;
	}

	public void setAnswers(List<Answer> answers) {
		this.answers = answers;
	}

	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(List<Link> links) {
		this.links = links;
	}

	public List<Photo> getPhotos() {
		return photos;
	}

	public void setPhotos(List<Photo> photos) {
		this.photos = photos;
	}

	public List<Regular> getRegulars() {
		return regulars;
	}

	public void setRegulars(List<Regular> regulars) {
		this.regulars = regulars;
	}

	public List<Video> getVideos() {
		return videos;
	}

	public void setVideos(List<Video> videos) {
		this.videos = videos;
	}

}
