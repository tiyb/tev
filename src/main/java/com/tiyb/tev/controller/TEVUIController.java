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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.tiyb.tev.datamodel.Hashtag;
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.datamodel.Video;
import com.tiyb.tev.exception.InvalidTypeException;
import com.tiyb.tev.exception.NoStagedPostsException;
import com.tiyb.tev.exception.ResourceNotFoundException;
import com.tiyb.tev.exception.XMLParsingException;
import com.tiyb.tev.xml.BlogXmlReader;
import com.tiyb.tev.xml.BlogXmlWriter;
import com.tiyb.tev.xml.ConversationXmlReader;

/**
 * Controller for all UI (HTML pages / jQuery-enabled) for the TEV application.
 * 
 * @author tiyb
 */
@Controller
public class TEVUIController {

	Logger logger = LoggerFactory.getLogger(TEVUIController.class);

	/**
	 * REST controller for working with posts
	 */
	@Autowired
	private TEVPostRestController postController;

	/**
	 * REST controller for working with conversations
	 */
	@Autowired
	private TEVConvoRestController convoController;

	/**
	 * REST controller for working with metadata
	 */
	@Autowired
	private TEVMetadataRestController mdController;

	/**
	 * REST controller for working with staged data
	 */
	@Autowired
	private TEVStagingController stagingController;

	/**
	 * Returns the main (or index) page, at either / or /index
	 * 
	 * @param model not used
	 * @return name of the template to be used to render the page
	 */
	@RequestMapping(value = { "/", "/index" }, method = RequestMethod.GET)
	public String index(Model model) {
		updateModelWithTheme(model);
		return "index";
	}

	/**
	 * Returns the page for showing a list of message conversations, at
	 * /conversations. Retrieves metadata and conversations, which are added to the
	 * model, before returning the location of the conversations viewer.
	 * 
	 * TODO what to do about getting the right blog
	 * 
	 * @param model not used
	 * @return name of the template to be used to render the page
	 */
	@RequestMapping(value = { "/conversations" }, method = RequestMethod.GET)
	public String conversations(Model model) {
		Metadata md = mdController.getDefaultMetadata();
		model.addAttribute("metadata", md);
		List<Conversation> conversations = convoController.getAllConversationsForBlog(md.getBlog());
		model.addAttribute("conversations", conversations);

		updateModelWithTheme(model);

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
		updateModelWithTheme(model);
		return "metadata";
	}

	/**
	 * Handles file uploads, for reading in the Tumblr Post XML Export for a given
	 * blog. Actual logic is handled by the
	 * {@link com.tiyb.tev.xml.BlogXmlReader#parseDocument(java.io.InputStream, TEVPostRestController, TEVMetadataRestController)
	 * BlogXmlReader#parseDocument()} method; this method simply calls that class
	 * and then (upon success) redirects to the index. Failure redirects to the "bad
	 * XML" error page.
	 * 
	 * @param file               The Tumblr XML file to be read
	 * @param redirectAttributes not used
	 * @return The page to which the successful upload should be redirected
	 */
	@PostMapping("/postDataUpload/{blog}")
	public String handlePostFileUploadForBlog(@PathVariable(value = "blog") String blog,
			@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
		try {
			BlogXmlReader.parseDocument(file.getInputStream(), postController, mdController, blog);
		} catch (XMLParsingException | IOException e) {
			logger.error("UI Controller failing in handlePostFileUpload due to XML parsing error: ", e);
			return "redirect:/errorbadxml";
		}

		return "redirect:/index";
	}

	/**
	 * Handles file uploads for reading in Tumblr messaging XML extract. Actual
	 * logic is handled by the
	 * {@link com.tiyb.tev.xml.ConversationXmlReader#parseDocument(MultipartFile, TEVMetadataRestController, TEVConvoRestController)
	 * ConversationXmlReader#parseDocument()} method; this method simply calls that
	 * class and then (upon success) redirects to the index. Failure redirects to
	 * the "bad XML" error page.
	 * 
	 * TODO UI won't be passing this param
	 * 
	 * @param blog               Blog for which conversation should be read
	 * @param file               the XML file to be parsed
	 * @param redirectAttributes not used
	 * @return The page to which the application should be redirected after upload
	 */
	@PostMapping("/conversationDataUpload/{blog}")
	public String handleConversationFileUpload(@PathVariable("blog") String blog,
			@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

		try {
			ConversationXmlReader.parseDocument(file, mdController, convoController, blog);
		} catch (XMLParsingException e) {
			logger.error("UI Controller failing in handleConversationFileUpload due to XML parsing error: ", e);
			return "redirect:/errorbadxml";
		}

		return "redirect:/conversations";
	}

	/**
	 * <p>
	 * This request is used to populate the viewer. It first determines the correct
	 * viewer to show (Regular, Photo, etc.), populates the
	 * {@link org.springframework.ui.Model Model} (since the pages are rendered by
	 * Thymeleaf on the server rather than jQuery on the client), then returns the
	 * correct template to show that type of post.
	 * </p>
	 * 
	 * <p>
	 * {@link #pullOutTagValues(String)} is leveraged for getting a nicer view of
	 * the tags in the post.
	 * </p>
	 * 
	 * @param blog   The blog for which this post should be retrieved
	 * @param postID The ID of the post to be viewed (regardless of type)
	 * @param model  The model to be populated with post data, for use by Thymeleaf
	 *               in the HTML template
	 * @return The name of the template to use for rendering the output
	 */
	@RequestMapping(value = { "/postViewer/{blog}" }, method = RequestMethod.GET)
	public String showViewer(@PathVariable(value = "blog") String blog, @RequestParam("id") Long postID, Model model) {
		Post post = postController.getPostForBlogById(blog, postID);
		model.addAttribute("post", post);
		model.addAttribute("tags", pullOutTagValues(post.getTags()));
		List<String> availableTypes = mdController.getAllTypes();
		String postType = "";

		for (String type : availableTypes) {
			if (type.equals(post.getType())) {
				postType = type;
				break;
			}
		}

		if (postType == "") {
			logger.error("Post found in DB with an invalid type");
			throw new InvalidTypeException();
		}

		switch (postType) {
		case "regular":
			Regular reg = postController.getRegularForBlogById(post.getTumblelog(), postID);
			model.addAttribute("regular", reg);
			return "viewers/regular";
		case "link":
			Link ln = postController.getLinkForBlogById(blog, postID);
			model.addAttribute("link", ln);
			return "viewers/link";
		case "answer":
			Answer ans = postController.getAnswerForBlogById(blog, postID);
			model.addAttribute("answer", ans);
			return "viewers/answer";
		case "photo":
			List<String> images = new ArrayList<String>();
			List<Photo> photos = postController.getPhotoForBlogById(post.getTumblelog(), postID);
			for (int i = 0; i < photos.size(); i++) {
				Photo photo = photos.get(i);
				String ext = photo.getUrl1280().substring(photo.getUrl1280().lastIndexOf('.'));
				images.add(postID + "_" + i + ext);
			}
			model.addAttribute("photos", images);
			model.addAttribute("caption", photos.get(0).getCaption());
			return "viewers/photo";
		case "video":
			Video vid = postController.getVideoForBlogById(post.getTumblelog(), postID);
			model.addAttribute("video", vid);
			return "viewers/video";
		}

		return "viewers/" + postType;
	}

	/**
	 * This request is used to populate the hashtag viewer.
	 * 
	 * @param blog  The blog for which hashtags are being retrieved
	 * @param model The model to be populated with post data, for use by Thymeleaf
	 *              in the HTML template
	 * @return The name of the template to use for rendering the output
	 */
	@RequestMapping(value = { "/hashtagViewer/{blog}" }, method = RequestMethod.GET)
	public String showHashtagViewerForBlog(@PathVariable(value = "blog") String blog, Model model) {
		List<Hashtag> hashtags = postController.getAllHashtagsForBlog(blog);
		model.addAttribute("hashtags", hashtags);
		updateModelWithTheme(model);
		return "viewers/hashtags";
	}

	/**
	 * This request is used to view the XML for export purposes.
	 * 
	 * @param model The model to be populated with post data, for use by Thymeleaf
	 *              (in this case, just theme information)
	 * @return The name of the template to use for rendering the output
	 */
	@RequestMapping(value = { "/exportViewer" }, method = RequestMethod.GET)
	public String showExportViewer(Model model) {
		updateModelWithTheme(model);
		return "viewers/exportedxml";
	}

	/**
	 * This request is to show the Staged Post viewer
	 * 
	 * @param model The model to be populated with a list of IDs, for use by
	 *              Thymeleaf
	 * @return The name of the template to use for rendering the output
	 */
	@RequestMapping(value = { "/staged" }, method = RequestMethod.GET)
	public String showStagedPosts(Model model) {
		updateModelWithTheme(model);
		return "staged";
	}

	/**
	 * Request used to populate conversation viewer
	 * 
	 * TODO UI won't be passing blog name
	 * 
	 * @param blog            Name of the blog
	 * @param participantName Name of the conversation
	 * @param model           Populated with information from this conversation
	 * @return Name of the viewer to load
	 */
	@RequestMapping(value = { "/conversationViewer" }, method = RequestMethod.GET)
	public String showConversationViewer(@RequestParam("blog") String blog,
			@RequestParam("participant") String participantName, Model model) {
		Metadata md = mdController.getMetadataForBlogOrDefault(blog);
		model.addAttribute("metadata", md);
		Conversation convo = convoController.getConversationForBlogByParticipant(md.getBlog(), participantName);
		model.addAttribute("conversation", convo);
		List<ConversationMessage> messages = convoController.getConvoMsgForBlogByConvoID(convo.getBlog(),
				convo.getId());
		model.addAttribute("messages", messages);

		updateModelWithTheme(model);

		return "viewers/conversation";
	}

	/**
	 * Used to display a pop-up window to display a single image
	 * 
	 * @param imageName Name of the image to be displayed
	 * @param model     Model used used for populating the Thymeleaf page
	 * @return Name of the viewer to load
	 */
	@RequestMapping(value = { "/viewers/imageViewer/{imageName}" }, method = RequestMethod.GET)
	public String showSingleImageViewer(@PathVariable("imageName") String imageName, Model model) {
		model.addAttribute("imageName", imageName);
		updateModelWithTheme(model);
		return "viewers/singleimageviewer";
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
		// TODO may need something better than just the default MD
		String fullName = mdController.getDefaultMetadata().getBaseMediaPath() + "/" + imageName;

		File file = new File(fullName);
		try {
			return Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			logger.warn("File " + imageName + " not found.");
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
		// TODO may need something better than just the default MD
		String fullName = mdController.getDefaultMetadata().getBaseMediaPath() + "/"
				+ request.getRequestURI().substring(request.getRequestURI().lastIndexOf('/'));

		response.setContentType("video/mp4");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");

		File file = new File(fullName);
		try {
			FileInputStream in = new FileInputStream(file);
			OutputStream out = response.getOutputStream();
			byte[] buf = new byte[1024];
			while (in.read(buf) >= 0) {
				out.write(buf);
			}
			in.close();
			out.close();
		} catch (IOException e) {
			logger.error("IO exception reading video file", e);
			throw new ResourceNotFoundException(fullName, fullName, e);
		}
	}

	/**
	 * Used to request the XML export, for any posts that have been "staged" for
	 * export. Returns the data as a string, with the intent that it is displayed in
	 * the browser.
	 * 
	 * @param blog     The blog for which staged downloads should be retrieved
	 * @param response The HTTP Response object (used for setting headers)
	 * @param request  The HTTP Request object
	 * @return The XML file, as a String
	 */
	@RequestMapping(value = { "/stagedPostsDownload/{blog}" }, method = RequestMethod.GET, produces = { "text/plain" })
	public @ResponseBody String getStagedPostsFileForBlog(@PathVariable(value = "blog") String blog,
			HttpServletResponse response, HttpServletRequest request) {
		response.setContentType("application/xml");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");
		List<Long> postIDs = stagingController.getAllPostsForBlog(blog);

		if (postIDs.size() < 1) {
			logger.warn("No posts staged for download");
			throw new NoStagedPostsException();
		}

		List<Post> posts = new ArrayList<Post>();
		for (Long id : postIDs) {
			Post post = postController.getPostForBlogById(blog, id);
			posts.add(post);
		}

		String xmlOutput = BlogXmlWriter.getStagedPostXMLForBlog(postIDs, postController, blog);
		return xmlOutput;
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
	 * Returns the viewer buttons used in all post viewer pages
	 * 
	 * @return name of the template to be used to render the page
	 */
	@RequestMapping(value = { "/viewerbuttons" }, method = RequestMethod.GET)
	public String viewerButtons() {
		return "viewers/viewerbuttons";
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

	/**
	 * Used to set the theme, so that Thymeleaf pages can set the correct CSS
	 * 
	 * TODO definitely need to get the proper blog, not just pass the default
	 * 
	 * @param model The model to be updated
	 */
	private void updateModelWithTheme(Model model) {
		Metadata md = mdController.getDefaultMetadata();
		String theme = md.getTheme();
		if (theme == null || theme.equals("")) {
			theme = "base";
			md.setTheme(theme);
			mdController.updateMetadata(md.getId(), md);
		}

		model.addAttribute("theme", theme);
	}

}
