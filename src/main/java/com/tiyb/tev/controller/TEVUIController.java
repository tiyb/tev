package com.tiyb.tev.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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
import com.tiyb.tev.exception.BlogMismatchParsingException;
import com.tiyb.tev.exception.InvalidTypeException;
import com.tiyb.tev.exception.NoMetadataFoundException;
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

	private static final String HASHTAG_ENDSPAN = "</span>&nbsp;&nbsp;&nbsp;&nbsp;"; //$NON-NLS-1$
	private static final String HASHTAG_STARTSPAN = "<span class='hashtagspan'>"; //$NON-NLS-1$
	private static final String HASHTAG_SPLITSTRING = "\\s*,\\s*"; //$NON-NLS-1$
	
	
	private static final String XML_CONTENTTYPE = "application/xml"; //$NON-NLS-1$
	private static final String CACHECONTROL_VALUE = "no-cache"; //$NON-NLS-1$
	private static final String CACHECONTROL_HEADER = "Cache-Control"; //$NON-NLS-1$
	private static final String PRAGMA_VALUE = "no-cache"; //$NON-NLS-1$
	private static final String PRAGMA_HEADER = "Pragma"; //$NON-NLS-1$
	private static final String VIDEO_CONTENTTYPE = "video/mp4"; //$NON-NLS-1$
	
	private static final String FULL_IMAGE_PATH = "%s/%s"; //$NON-NLS-1$
	private static final String IMAGE_NAME_FORMAT = "%d_%d%s"; //$NON-NLS-1$
	private static final String JS_BLOGNAME = "var blogName = \"%s\""; //$NON-NLS-1$
	
	private static final String MODEL_ATTRIBUTE_IMAGENAME = "imageName"; //$NON-NLS-1$
	private static final String MODEL_ATTRIBUTE_MESSAGES = "messages"; //$NON-NLS-1$
	private static final String MODEL_ATTRIBUTE_CONVERSATION = "conversation"; //$NON-NLS-1$
	private static final String MODEL_ATTRIBUTE_HASHTAGS = "hashtags"; //$NON-NLS-1$
	private static final String MODEL_ATTRIBUTE_VIDEO = "video"; //$NON-NLS-1$
	private static final String MODEL_ATTRIBUTE_CAPTION = "caption"; //$NON-NLS-1$
	private static final String MODEL_ATTRIBUTE_PHOTOS = "photos"; //$NON-NLS-1$
	private static final String MODEL_ATTRIBUTE_ANSWER = "answer"; //$NON-NLS-1$
	private static final String MODEL_ATTRIBUTE_LINK = "link"; //$NON-NLS-1$
	private static final String MODEL_ATTRIBUTE_REGULAR = "regular"; //$NON-NLS-1$
	private static final String MODEL_ATTRIBUTE_TAGS = "tags"; //$NON-NLS-1$
	private static final String MODEL_ATTRIBUTE_POST = "post"; //$NON-NLS-1$
	private static final String MODEL_ATTRIBUTE_BLOGNAMEJS = "blogNameJScript"; //$NON-NLS-1$
	private static final String MODEL_ATTRIBUTE_MDCOLLECTION = "mdCollection"; //$NON-NLS-1$
	private static final String MODEL_ATTRIBUTE_NOBLOGSCREATED = "noBlogsCreated"; //$NON-NLS-1$
	public static final String MODEL_ATTRIBUTE_THEME = "theme"; //$NON-NLS-1$
	public static final String MODEL_ATTRIBUTE_BLOGNAME = "blogName"; //$NON-NLS-1$
	
	private static final String REQUEST_MAPPING_VIDEOVIEWER = "viewers/video"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_PHOTOVIEWER = "viewers/photo"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_ANSWERVIEWER = "viewers/answer"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_LINKVIEWER = "viewers/link"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_REGULARVIEWER = "viewers/regular"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_REDIRECT_BLOGNAMEMISMATCH = "redirect:/errorblogmismatch?blogName=%s&participantName=%s"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_VIEWERBUTTONS = "viewers/viewerbuttons"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_HEADER = "header"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_FOOTER = "footer"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_SINGLEIMAGEVIEWER = "viewers/singleimageviewer"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_CONVERSATIONVIEWER = "viewers/conversation"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_STAGED = "staged"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_XMLEXPORTVIEWER = "viewers/exportedxml"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_HASHTAGVIEWER = "viewers/hashtags"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_VIEWERS = "viewers/"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_REDIRECT_CONVERSATIONS = "redirect:/conversations"; //$NON-NLS-1$
	private static final String TEMPBLOGNAME_URLSUFFIX = "?tempBlogName=%s"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_REDIRECT_INDEX = "redirect:/index"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_REDIRECT_ERRORBADXML = "redirect:/errorbadxml"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_METADATAFRAME = "metadata-frame"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_METADATA = "metadata"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_CONVERSATIONS = "conversations"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_INDEX = "index"; //$NON-NLS-1$

	private static final String NOSTAGEDPOSTS_WARNING = "No posts staged for download"; //$NON-NLS-1$
	private static final String VIDEO_IOEXCEPTION = "IO exception reading video file"; //$NON-NLS-1$
	private static final String FILE_NOT_FOUND_WARNING = "File {} not found."; //$NON-NLS-1$

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
	 * Returns the main (or index) page, at either / or /index. Checks first to see
	 * whether any metadata has been created; if not, redirects to the settings
	 * page.
	 * 
	 * @param model not used
	 * @return name of the template to be used to render the page
	 */
	@RequestMapping(value = { "/", "/index" }, method = RequestMethod.GET)
	public String index(Model model, @RequestParam("tempBlogName") Optional<String> blogParam) {
		try {
			mdController.getDefaultBlogName();
		} catch (NoMetadataFoundException e) {
			return metadata(model);
		}
		if (blogParam.isPresent()) {
			model.addAttribute(MODEL_ATTRIBUTE_BLOGNAME, blogParam.get());
		} else {
			model.addAttribute(MODEL_ATTRIBUTE_BLOGNAME, mdController.getDefaultBlogName());
		}
		updateModelWithTheme(model);
		return REQUEST_MAPPING_INDEX;
	}

	/**
	 * Returns the page for showing a list of message conversations, at
	 * /conversations. Retrieves metadata and conversations, which are added to the
	 * model, before returning the location of the conversations viewer.
	 * 
	 * @param model not used
	 * @return name of the template to be used to render the page
	 */
	@RequestMapping(value = { "/conversations" }, method = RequestMethod.GET)
	public String conversations(Model model, @RequestParam("tempBlogName") Optional<String> blogParam) {
		Metadata md = null;
		if (blogParam.isPresent()) {
			md = mdController.getMetadataForBlog(blogParam.get());
		} else {
			md = mdController.getDefaultMetadata();
		}
		model.addAttribute(REQUEST_MAPPING_METADATA, md);
		List<Conversation> conversations = convoController.getAllConversationsForBlog(md.getBlog());
		model.addAttribute(REQUEST_MAPPING_CONVERSATIONS, conversations);

		model.addAttribute(MODEL_ATTRIBUTE_BLOGNAME, md.getBlog());
		updateModelWithTheme(model);

		return REQUEST_MAPPING_CONVERSATIONS;
	}

	/**
	 * Returns the a page for maintaining the application's metadata, at /metadata.
	 * If no blogs have been created (i.e. if there are no Metadata objects in the
	 * DB), the model is updated with this info, so the client can create one.
	 * 
	 * @param model not used
	 * @return name of the template to be used to render the page
	 */
	@RequestMapping(value = { "/metadata" }, method = RequestMethod.GET)
	public String metadata(Model model) {
		try {
			model.addAttribute(MODEL_ATTRIBUTE_BLOGNAME, mdController.getDefaultBlogName());
			updateModelWithTheme(model);
		} catch (NoMetadataFoundException e) {
			model.addAttribute(MODEL_ATTRIBUTE_NOBLOGSCREATED, Boolean.TRUE);
			model.addAttribute(MODEL_ATTRIBUTE_THEME, Metadata.DEFAULT_THEME);
		}

		List<Metadata> mdCollection = mdController.getAllMetadata();
		model.addAttribute(MODEL_ATTRIBUTE_MDCOLLECTION, mdCollection);
		return REQUEST_MAPPING_METADATA;
	}

	/**
	 * Returns the page for maintaining metadata for a particular blog
	 * 
	 * @param blogName Name of the blog being edited
	 * @param model    Used for setting the theme
	 * @return Name of the template to be used to render the page
	 */
	@RequestMapping(value = { "/metadata/{blogName}" }, method = RequestMethod.GET)
	public String individualMetatada(@PathVariable(MODEL_ATTRIBUTE_BLOGNAME) String blogName, Model model) {
		model.addAttribute(MODEL_ATTRIBUTE_BLOGNAME, blogName);
		addBlogNameJSToModel(model, blogName);
		updateModelWithTheme(model);

		return REQUEST_MAPPING_METADATAFRAME;
	}

	/**
	 * Helper function to add an attribute to the Model for a client-side bit of JS
	 * code for setting a variable with the blog's name. This <i>should</i> have
	 * been as simple as setting an attribute with the blog's name and then setting
	 * the JS code in the Thymeleaf page, but for some reason the initial developer
	 * couldn't figure out a way to get single quotes into the generated string...
	 * 
	 * @param model    The Model to which the JS code should be added
	 * @param blogName The name of the current blog
	 */
	private void addBlogNameJSToModel(Model model, String blogName) {
		model.addAttribute(MODEL_ATTRIBUTE_BLOGNAMEJS, String.format(JS_BLOGNAME, blogName));
	}

	/**
	 * Handles file uploads, for reading in the Tumblr Post XML Export for a given
	 * blog. Actual logic is handled by the
	 * {@link com.tiyb.tev.xml.BlogXmlReader#parseDocument(java.io.InputStream, TEVPostRestController, TEVMetadataRestController, String)
	 * parseDocument()} method; this method simply calls that class and then (upon
	 * success) redirects to the index. Failure redirects to the "bad XML" error
	 * page.
	 * 
	 * @param file               The Tumblr XML file to be read
	 * @param redirectAttributes not used
	 * @return The page to which the successful upload should be redirected
	 */
	@PostMapping("/postDataUpload/{blog}")
	public String handlePostFileUploadForBlog(@PathVariable("blog") String blog,
			@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
		try {
			BlogXmlReader.parseDocument(file.getInputStream(), postController, mdController, blog);
		} catch (XMLParsingException | IOException e) {
			logger.error("UI Controller failing in handlePostFileUpload due to XML parsing error: ", e); //$NON-NLS-1$
			return REQUEST_MAPPING_REDIRECT_ERRORBADXML;
		}

		String defaultBlogName = mdController.getDefaultBlogName();
		if (blog.equals(defaultBlogName)) {
			return REQUEST_MAPPING_REDIRECT_INDEX;
		} else {
			return String.format(REQUEST_MAPPING_REDIRECT_INDEX.concat(TEMPBLOGNAME_URLSUFFIX), blog);
		}
	}

	/**
	 * Handles file uploads for reading in Tumblr messaging XML extract. Actual
	 * logic is handled by the
	 * {@link com.tiyb.tev.xml.ConversationXmlReader#parseDocument(MultipartFile, TEVMetadataRestController, TEVConvoRestController, String)
	 * parseDocument()} method; this method simply calls that class and then (upon
	 * success) redirects to the index. Failure redirects to the "bad XML" error
	 * page.
	 * 
	 * @param blog               Blog for which conversation should be read
	 * @param file               the XML file to be parsed
	 * @param redirectAttributes not used
	 * @return The page to which the application should be redirected after upload
	 */
	@PostMapping("/conversationDataUpload/{blog}")
	public String handleConversationFileUpload(@PathVariable("blog") String blog,
			@RequestParam("conversationFile") MultipartFile file, RedirectAttributes redirectAttributes) {
		try {
			ConversationXmlReader.parseDocument(file, mdController, convoController, blog);
		} catch (BlogMismatchParsingException e) {
			logger.error("Mismatch in XML between specified blog name ({}) and name in XML ({}).", e.getBlogName(), //$NON-NLS-1$
					e.getMainParticipantName());
			String redirectString = String.format(REQUEST_MAPPING_REDIRECT_BLOGNAMEMISMATCH, e.getBlogName(),
					e.getMainParticipantName());
			return redirectString;
		} catch (XMLParsingException e) {
			logger.error("UI Controller failing in handleConversationFileUpload due to XML parsing error: ", e); //$NON-NLS-1$
			return REQUEST_MAPPING_REDIRECT_ERRORBADXML;
		}

		String defaultBlogName = mdController.getDefaultBlogName();
		if (blog.equals(defaultBlogName)) {
			return REQUEST_MAPPING_REDIRECT_CONVERSATIONS;
		} else {
			return String.format(REQUEST_MAPPING_REDIRECT_CONVERSATIONS.concat(TEMPBLOGNAME_URLSUFFIX), blog);
		}
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
	public String showViewer(@PathVariable("blog") String blog, @RequestParam("id") Long postID, Model model) {
		Post post = postController.getPostForBlogById(blog, postID);
		model.addAttribute(MODEL_ATTRIBUTE_POST, post);
		addBlogNameJSToModel(model, blog);
		model.addAttribute(MODEL_ATTRIBUTE_TAGS, pullOutTagValues(post.getTags()));
		List<String> availableTypes = mdController.getAllTypes();
		String postType = StringUtils.EMPTY;

		for (String type : availableTypes) {
			if (type.equals(post.getType())) {
				postType = type;
				break;
			}
		}

		if (postType == StringUtils.EMPTY) {
			logger.error("Post found in DB with an invalid type"); //$NON-NLS-1$
			throw new InvalidTypeException();
		}

		switch (postType) {
		case Post.POST_TYPE_REGULAR:
			Regular reg = postController.getRegularForBlogById(post.getTumblelog(), postID);
			model.addAttribute(MODEL_ATTRIBUTE_REGULAR, reg);
			return REQUEST_MAPPING_REGULARVIEWER;
		case Post.POST_TYPE_LINK:
			Link ln = postController.getLinkForBlogById(blog, postID);
			model.addAttribute(MODEL_ATTRIBUTE_LINK, ln);
			return REQUEST_MAPPING_LINKVIEWER;
		case Post.POST_TYPE_ANSWER:
			Answer ans = postController.getAnswerForBlogById(blog, postID);
			model.addAttribute(MODEL_ATTRIBUTE_ANSWER, ans);
			return REQUEST_MAPPING_ANSWERVIEWER;
		case Post.POST_TYPE_PHOTO:
			List<String> images = new ArrayList<String>();
			List<Photo> photos = postController.getPhotoForBlogById(post.getTumblelog(), postID);
			for (int i = 0; i < photos.size(); i++) {
				Photo photo = photos.get(i);
				String ext = photo.getUrl1280().substring(photo.getUrl1280().lastIndexOf('.'));
				images.add(String.format(IMAGE_NAME_FORMAT, postID, i, ext));
			}
			model.addAttribute(MODEL_ATTRIBUTE_PHOTOS, images);
			model.addAttribute(MODEL_ATTRIBUTE_CAPTION, photos.get(0).getCaption());
			return REQUEST_MAPPING_PHOTOVIEWER;
		case Post.POST_TYPE_VIDEO:
			Video vid = postController.getVideoForBlogById(post.getTumblelog(), postID);
			model.addAttribute(MODEL_ATTRIBUTE_VIDEO, vid);
			return REQUEST_MAPPING_VIDEOVIEWER;
		}

		return REQUEST_MAPPING_VIEWERS.concat(postType);
	}

	/**
	 * This request is used to populate the hashtag viewer.
	 * 
	 * @param model     The model to be populated with post data, for use by
	 *                  Thymeleaf in the HTML template
	 * @param blogParam Optional: the name of the blog for which this page should be
	 *                  rendered, regardless of the default
	 * @return The name of the template to use for rendering the output
	 */
	@RequestMapping(value = { "/hashtagViewer" }, method = RequestMethod.GET)
	public String showHashtagViewerForBlog(Model model, @RequestParam("tempBlogName") Optional<String> blogParam) {
		String blog = blogParam.isPresent() ? blogParam.get() : mdController.getDefaultBlogName();
		List<Hashtag> hashtags = postController.getAllHashtagsForBlog(blog);
		model.addAttribute(MODEL_ATTRIBUTE_HASHTAGS, hashtags);
		model.addAttribute(MODEL_ATTRIBUTE_BLOGNAME, blog);
		updateModelWithTheme(model);
		addBlogNameJSToModel(model, blog);
		return REQUEST_MAPPING_HASHTAGVIEWER;
	}

	/**
	 * This request is used to view the XML for export purposes.
	 * 
	 * @param model The model to be populated with post data, for use by Thymeleaf
	 *              (in this case, just theme information)
	 * @return The name of the template to use for rendering the output
	 */
	@RequestMapping(value = { "/exportViewer" }, method = RequestMethod.GET)
	public String showExportViewer(Model model, @RequestParam("tempBlogName") Optional<String> blogParam) {
		if (blogParam.isPresent()) {
			model.addAttribute(MODEL_ATTRIBUTE_BLOGNAME, blogParam.get());
		} else {
			model.addAttribute(MODEL_ATTRIBUTE_BLOGNAME, mdController.getDefaultBlogName());
		}
		updateModelWithTheme(model);
		addBlogNameJSToModel(model, (String) model.getAttribute(MODEL_ATTRIBUTE_BLOGNAME));
		return REQUEST_MAPPING_XMLEXPORTVIEWER;
	}

	/**
	 * This request is to show the Staged Post viewer
	 * 
	 * @param model The model to be populated with a list of IDs, for use by
	 *              Thymeleaf
	 * @return The name of the template to use for rendering the output
	 */
	@RequestMapping(value = { "/staged" }, method = RequestMethod.GET)
	public String showStagedPosts(Model model, @RequestParam("tempBlogName") Optional<String> blogParam) {
		if (blogParam.isPresent()) {
			model.addAttribute(MODEL_ATTRIBUTE_BLOGNAME, blogParam.get());
		} else {
			model.addAttribute(MODEL_ATTRIBUTE_BLOGNAME, mdController.getDefaultBlogName());
		}
		updateModelWithTheme(model);
		return REQUEST_MAPPING_STAGED;
	}

	/**
	 * Request used to populate conversation viewer
	 * 
	 * @param blog            Name of the blog
	 * @param participantName Name of the conversation
	 * @param model           Populated with information from this conversation
	 * @return Name of the viewer to load
	 */
	@RequestMapping(value = { "/conversationViewer" }, method = RequestMethod.GET)
	public String showConversationViewer(@RequestParam("blog") String blog,
			@RequestParam("participant") String participantName, Model model) {
		Metadata md = mdController.getMetadataForBlog(blog);
		model.addAttribute(REQUEST_MAPPING_METADATA, md);
		addBlogNameJSToModel(model, blog);
		Conversation convo = convoController.getConversationForBlogByParticipant(md.getBlog(), participantName);
		model.addAttribute(MODEL_ATTRIBUTE_CONVERSATION, convo);
		List<ConversationMessage> messages = convoController.getConvoMsgForBlogByConvoID(convo.getBlog(),
				convo.getId());
		model.addAttribute(MODEL_ATTRIBUTE_MESSAGES, messages);

		model.addAttribute(MODEL_ATTRIBUTE_BLOGNAME, blog);
		updateModelWithTheme(model);

		return REQUEST_MAPPING_CONVERSATIONVIEWER;
	}

	/**
	 * Used to display a pop-up window to display a single image
	 * 
	 * @param imageName Name of the image to be displayed
	 * @param model     Model used used for populating the Thymeleaf page
	 * @return Name of the viewer to load
	 */
	@RequestMapping(value = { "/viewers/imageViewer/{imageName}" }, method = RequestMethod.GET)
	public String showSingleImageViewer(@PathVariable(MODEL_ATTRIBUTE_IMAGENAME) String imageName, Model model) {
		model.addAttribute(MODEL_ATTRIBUTE_IMAGENAME, imageName);
		return REQUEST_MAPPING_SINGLEIMAGEVIEWER;
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
	public @ResponseBody byte[] getMedia(@PathVariable(MODEL_ATTRIBUTE_IMAGENAME) String imageName, Model model) {
		String fullName = String.format(FULL_IMAGE_PATH, mdController.getDefaultMetadata().getBaseMediaPath(),
				imageName);

		File file = new File(fullName);
		try {
			return Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			logger.warn(FILE_NOT_FOUND_WARNING, imageName);
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
	@RequestMapping(value = { "/viewerVideo/{videoName}" }, method = RequestMethod.GET, produces = { VIDEO_CONTENTTYPE })
	public void getVideo(HttpServletResponse response, HttpServletRequest request) {
		String fullName = String.format(FULL_IMAGE_PATH, mdController.getDefaultMetadata().getBaseMediaPath(),
				request.getRequestURI().substring(request.getRequestURI().lastIndexOf('/')));

		response.setContentType(VIDEO_CONTENTTYPE);
		response.setHeader(PRAGMA_HEADER, PRAGMA_VALUE);
		response.setHeader(CACHECONTROL_HEADER, CACHECONTROL_VALUE);

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
			logger.error(VIDEO_IOEXCEPTION, e);
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
	public @ResponseBody String getStagedPostsFileForBlog(@PathVariable("blog") String blog,
			HttpServletResponse response, HttpServletRequest request) {
		response.setContentType(XML_CONTENTTYPE);
		response.setHeader(PRAGMA_HEADER, PRAGMA_VALUE);
		response.setHeader(CACHECONTROL_HEADER, CACHECONTROL_VALUE);
		List<Long> postIDs = stagingController.getAllPostsForBlog(blog);

		if (postIDs.size() < 1) {
			logger.warn(NOSTAGEDPOSTS_WARNING);
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
		return REQUEST_MAPPING_FOOTER;
	}

	/**
	 * Returns the header
	 * 
	 * @param model not used
	 * @return name of the template to be used to render the page
	 */
	@RequestMapping(value = { "/header" }, method = RequestMethod.GET)
	public String header(Model model) {
		return REQUEST_MAPPING_HEADER;
	}

	/**
	 * Returns the viewer buttons used in all post viewer pages
	 * 
	 * @return name of the template to be used to render the page
	 */
	@RequestMapping(value = { "/viewerbuttons" }, method = RequestMethod.GET)
	public String viewerButtons() {
		return REQUEST_MAPPING_VIEWERBUTTONS;
	}

	/**
	 * Helper method that pulls out the tags into spans. This kind of ties
	 * processing together with UI, but... whatever.
	 * 
	 * @param csvTags String containing the hashtags, separated by commas
	 * @return String, containing HTML span tags containing the hashtags
	 */
	private String pullOutTagValues(String csvTags) {
		if (csvTags == null || csvTags.equals(StringUtils.EMPTY)) {
			return csvTags;
		}

		List<String> items = Arrays.asList(csvTags.split(HASHTAG_SPLITSTRING));
		StringBuilder builder = new StringBuilder();

		for (String s : items) {
			builder.append(HASHTAG_STARTSPAN);
			builder.append(s);
			builder.append(HASHTAG_ENDSPAN);
		}

		return builder.toString();
	}

	/**
	 * Used to set the theme, so that Thymeleaf pages can set the correct CSS
	 * 
	 * @param model The model to be updated
	 */
	private void updateModelWithTheme(Model model) {
		String blogName = (String) model.getAttribute(MODEL_ATTRIBUTE_BLOGNAME);
		Metadata md = mdController.getMetadataForBlog(blogName);
		String theme = md.getTheme();
		if (theme == null || theme.equals(StringUtils.EMPTY)) {
			theme = Metadata.DEFAULT_THEME;
			md.setTheme(theme);
			mdController.updateMetadata(md.getId(), md);
		}

		model.addAttribute(MODEL_ATTRIBUTE_THEME, theme);
	}

}
