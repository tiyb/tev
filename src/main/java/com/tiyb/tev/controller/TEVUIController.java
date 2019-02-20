package com.tiyb.tev.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tiyb.tev.datamodel.Answer;
import com.tiyb.tev.datamodel.Conversation;
import com.tiyb.tev.datamodel.ConversationMessage;
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.datamodel.Type;
import com.tiyb.tev.datamodel.Video;
import com.tiyb.tev.datamodel.helpers.TEVSuperClass;
import com.tiyb.tev.exception.InvalidTypeException;
import com.tiyb.tev.exception.ResourceNotFoundException;
import com.tiyb.tev.exception.XMLParsingException;
import com.tiyb.tev.xml.BlogXmlReader;
import com.tiyb.tev.xml.ConversationXmlReader;

/**
 * Controller for all UI (HTML pages / jQuery-enabled) for the TEV application.
 * 
 * @author tiyb
 * @apiviz.landmark
 *
 */
@Controller
public class TEVUIController {

	@Autowired
	private TEVRestController restController;

	/**
	 * Returns the main (or index) page, at either / or /index
	 * 
	 * @param model not used
	 * @return name of the template to be used to render the page
	 */
	@RequestMapping(value = { "/", "/index" }, method = RequestMethod.GET)
	public String index(Model model) {
		return "index";
	}

	/**
	 * Returns the page for showing a list of message conversations, at
	 * /conversations
	 * 
	 * @param model not used
	 * @return name of the template to be used to render the page
	 */
	@RequestMapping(value = { "/conversations" }, method = RequestMethod.GET)
	public String conversations(Model model) {
		Metadata md = restController.getMetadata();
		model.addAttribute("metadata", md);
		List<Conversation> conversations = restController.getAllConversations();
		model.addAttribute("conversations", conversations);

		return "conversations";
	}

	/**
	 * Returns the a page for maintaining the application's metadata, at /metadata
	 * 
	 * @param model not used
	 * @return name of the template to be used to render the page
	 */
	@RequestMapping(value = { "/metadata" }, method = RequestMethod.GET)
	public String metadata(Model model) {
		return "metadata";
	}

	/**
	 * Handles file uploads, for reading in the big Tumblr XML Export
	 * 
	 * @param file               The Tumblr XML file to be read
	 * @param redirectAttributes not used
	 * @return The page to which the successful upload should be redirected
	 */
	@PostMapping("/postDataUpload")
	public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
		TEVSuperClass tsc;
		List<Type> types = restController.getAllTypes();
		try {
			tsc = BlogXmlReader.parseDocument(file.getInputStream(), types);
		} catch (XMLParsingException | IOException e) {
			throw new XMLParsingException();
		}

		if (tsc == null) {
			throw new XMLParsingException();
		}

		restController.deleteAllRegulars();
		restController.deleteAllAnswers();
		restController.deleteAllLinks();
		restController.deleteAllPhotos();
		restController.deleteAllVideos();
		restController.deleteAllPosts();
		for (Post post : tsc.getPosts()) {
			restController.createPost(post);
		}
		for (Regular regular : tsc.getRegulars()) {
			restController.createRegular(regular.getPostId(), regular);
		}
		for (Answer answer : tsc.getAnswers()) {
			restController.createAnswer(answer.getPostId(), answer);
		}
		for (Link link : tsc.getLinks()) {
			restController.createLink(link.getPostId(), link);
		}
		for (Photo photo : tsc.getPhotos()) {
			restController.createPhoto(photo);
		}
		for (Video video : tsc.getVideos()) {
			restController.createVideo(video.getPostId(), video);
		}

		return "redirect:/index";
	}

	/**
	 * Handles file uploads for reading in Tumblr messaging extract
	 * 
	 * @param file               the XML file to be parsed
	 * @param redirectAttributes not used
	 * @return The page to which the application should be redirected after upload
	 */
	@PostMapping("/conversationDataUpload")
	public String handleConversationFileUpload(@RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes) {
		
		restController.deleteAllConvoMsgs();
		restController.deleteAllConversations();

		try {
			ConversationXmlReader.parseDocument(file.getInputStream(), restController);
		} catch (XMLParsingException | IOException e) {
			throw new XMLParsingException();
		}

		return "redirect:/conversations";
	}

	/**
	 * This request is used to populate the viewer. It first determines the correct
	 * viewer to show (Regular, Photo, etc.), then returns the correct template to
	 * show that type of post. In this case, the pages are being populated by
	 * Thymeleaf, rather than jQuery, so the <code>Model</code> is populated with
	 * the necessary data.
	 * 
	 * @param postID The ID of the post to be viewed (regardless of type)
	 * @param model  The model to be populated with post data, for use by Thymeleaf
	 *               in the HTML template
	 * @return The name of the template to use for rendering the output
	 */
	@RequestMapping(value = { "/postViewer" }, method = RequestMethod.GET)
	public String showViewer(@RequestParam("id") Long postID, Model model) {
		Post post = restController.getPostById(postID);
		model.addAttribute("tags", pullOutTagValues(post.getTags()));
		List<Type> availableTypes = restController.getAllTypes();
		String postType = "";

		for (Type type : availableTypes) {
			if (type.getId().equals(post.getType())) {
				postType = type.getType();
				break;
			}
		}

		if (postType == "") {
			throw new InvalidTypeException();
		}

		switch (postType) {
		case "regular":
			Regular reg = restController.getRegularById(postID);
			model.addAttribute("regular", reg);
			return "viewers/regular";
		case "link":
			Link ln = restController.getLinkById(postID);
			model.addAttribute("link", ln);
			return "viewers/link";
		case "answer":
			Answer ans = restController.getAnswerById(postID);
			model.addAttribute("answer", ans);
			return "viewers/answer";
		case "photo":
			List<String> images = new ArrayList<String>();
			List<Photo> photos = restController.getPhotoById(postID);
			for (int i = 0; i < photos.size(); i++) {
				Photo photo = photos.get(i);
				String ext = photo.getUrl1280().substring(photo.getUrl1280().lastIndexOf('.'));
				images.add(postID + "_" + i + ext);
			}
			model.addAttribute("photos", images);
			model.addAttribute("caption", photos.get(0).getCaption());
			return "viewers/photo";
		case "video":
			Video vid = restController.getVideoById(postID);
			model.addAttribute("video", vid);
			return "viewers/video";
		}

		return "viewers/" + postType;
	}
	
	/**
	 * Request used to populate conversation viewer
	 * 
	 * @param participantName Name of the conversation
	 * @param model           Populated with information from this conversation
	 * @return Name of the viewer to load
	 */
	@RequestMapping(value = {"/conversationViewer"}, method = RequestMethod.GET)
	public String showConversationViewer(@RequestParam("participant") String participantName, Model model) {
		Metadata md = restController.getMetadata();
		model.addAttribute("metadata", md);
		Conversation convo = restController.getConversationByParticipant(participantName);
		model.addAttribute("conversation", convo);
		List<ConversationMessage> messages = restController.getConvoMsgByConvoID(convo.getId());
		model.addAttribute("messages", messages);
		
		return "viewers/conversation";
	}

	/**
	 * Returns a binary image, for use in the viewer. HTML pages can't directly
	 * access local images or videos, so this has to be done via the "server."
	 * 
	 * @param imageName Name of the image to be retrieved
	 * @param model     not used
	 * @return Byte stream of the file, as loaded from the file system
	 */
	@RequestMapping(value = { "/viewerMedia/{imageName}" }, method = RequestMethod.GET, produces = {
			MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_PNG_VALUE })
	public @ResponseBody byte[] getMedia(@PathVariable("imageName") String imageName, Model model) {
		String fullName = restController.getMetadata().getBaseMediaPath() + "/" + imageName;

		File file = new File(fullName);
		try {
			return Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			throw new ResourceNotFoundException(fullName, fullName, e);
		}
	}

	/**
	 * Returns a binary video, for use in the viewer. HTML pages can't directly
	 * access local images or videos, so this has to be done via the "server."
	 * 
	 * @param response HTTP Response object
	 * @param request  HTTP Request object
	 */
	@RequestMapping(value = { "/viewerVideo/{videoName}" }, method = RequestMethod.GET, produces = { "video/mp4" })
	public void getVideo(HttpServletResponse response, HttpServletRequest request) {
		// String ext =
		// photo.getUrl1280().substring(photo.getUrl1280().lastIndexOf('.'));
		String fullName = restController.getMetadata().getBaseMediaPath() + "/"
				+ request.getRequestURI().substring(request.getRequestURI().lastIndexOf('/'));

		response.setContentType("video/mp4");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");

		File file = new File(fullName);
		try {
			FileInputStream in = new FileInputStream(file);
			OutputStream out = response.getOutputStream();
			byte[] buf = new byte[1024];
			// int len = 0;
			while (in.read(buf) >= 0) {
				out.write(buf);
			}
			in.close();
			out.close();
		} catch (IOException e) {
			throw new ResourceNotFoundException(fullName, fullName, e);
		}
	}

	/**
	 * Returns the footer
	 * 
	 * @param model not used
	 * @return name of the template to be used to render the page
	 */
	@RequestMapping(value = { "/footer" }, method = RequestMethod.GET)
	public String footer(Model model) {
		return "footer";
	}

	/**
	 * Returns the header
	 * 
	 * @param model not used
	 * @return name of the template to be used to render the page
	 */
	@RequestMapping(value = { "/header" }, method = RequestMethod.GET)
	public String header(Model model) {
		return "header";
	}
	
	/**
	 * Helper method that pulls out the tags into spans. This kind of ties
	 * processing together with UI, but... whatever.
	 * 
	 * @param csvTags String containing the hashtags, separated by commas
	 * @return String, containing HTML span tags containing the hashtags
	 */
	private String pullOutTagValues(String csvTags) {
		if (csvTags == null || csvTags.equals("")) {
			return csvTags;
		}

		List<String> items = Arrays.asList(csvTags.split("\\s*,\\s*"));
		StringBuilder builder = new StringBuilder();

		for (String s : items) {
			builder.append("<span class='hashtagspan'>");
			builder.append(s);
			builder.append("</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		}

		return builder.toString();
	}
}
