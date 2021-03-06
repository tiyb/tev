package com.tiyb.tev.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
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

    /**
     * Standard attribute used for the model
     */
    public static final String MODEL_ATTRIBUTE_THEME = "theme";

    /**
     * Standard attribute used for the model
     */
    public static final String MODEL_ATTRIBUTE_BLOGNAME = "blogName";

    /**
     * Standard attribute used for the model
     */
    private static final String MODEL_ATTRIBUTE_IMAGENAME = "imageName";

    /**
     * Standard attribute used for the model
     */
    private static final String MODEL_ATTRIBUTE_MESSAGES = "messages";

    /**
     * Standard attribute used for the model
     */
    private static final String MODEL_ATTRIBUTE_CONVERSATION = "conversation";

    /**
     * Number of bytes to use for buffering operations
     */
    private static final int BYTE_BUFFER_LENGTH = 1024;

    /**
     * Standard attribute used for the model
     */
    private static final String MODEL_ATTRIBUTE_HASHTAGS = "hashtags";

    /**
     * Standard attribute used for the model
     */
    private static final String MODEL_ATTRIBUTE_VIDEO = "video";

    /**
     * Standard attribute used for the model
     */
    private static final String MODEL_ATTRIBUTE_CAPTION = "caption";

    /**
     * Standard attribute used for the model
     */
    private static final String MODEL_ATTRIBUTE_PHOTOS = "photos";

    /**
     * Standard attribute used for the model
     */
    private static final String MODEL_ATTRIBUTE_ANSWER = "answer";

    /**
     * Standard attribute used for the model
     */
    private static final String MODEL_ATTRIBUTE_LINK = "link";

    /**
     * Standard attribute used for the model
     */
    private static final String MODEL_ATTRIBUTE_REGULAR = "regular";

    /**
     * Standard attribute used for the model
     */
    private static final String MODEL_ATTRIBUTE_TAGS = "tags";

    /**
     * Standard attribute used for the model
     */
    private static final String MODEL_ATTRIBUTE_POST = "post";

    /**
     * Standard attribute used for the model
     */
    private static final String MODEL_ATTRIBUTE_BLOGNAMEJS = "blogNameJScript";

    /**
     * Standard attribute used for the model
     */
    private static final String MODEL_ATTRIBUTE_POSTIDJS = "postIDJS";

    /**
     * Standard attribute used for the model
     */
    private static final String MODEL_ATTRIBUTE_MDCOLLECTION = "mdCollection";

    /**
     * Standard attribute used for the model
     */
    private static final String MODEL_ATTRIBUTE_NOBLOGSCREATED = "noBlogsCreated";

    /**
     * Parameter used in URLs to specify a particular blog (other than the default)
     */
    private static final String TEMPBLOGNAME_PARAM = "tempBlogName";

    private Logger logger = LoggerFactory.getLogger(TEVUIController.class);

    /**
     * REST controller for working with posts
     */
    @Autowired
    private TEVPostRestController postController;

    /**
     * REST controller for working with Regulars
     */
    @Autowired
    private TEVRegularController regController;

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
     * Returns the main (or index) page, at either / or /index. Checks first to see whether any
     * metadata has been created; if not, redirects to the settings page.
     *
     * @param model     not used
     * @param blogParam Specifies a blog to use, if other than the default
     * @return name of the template to be used to render the page
     */
    @RequestMapping(value = { "/", "/index" }, method = RequestMethod.GET)
    public String index(final Model model, @RequestParam("tempBlogName") final Optional<String> blogParam) {
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
        return "index";
    }

    /**
     * Returns the page for showing a list of message conversations, at /conversations. Retrieves
     * metadata and conversations, which are added to the model, before returning the location of
     * the conversations viewer.
     *
     * @param model     not used
     * @param blogParam Specifies a blog to use, if other than the default
     * @return name of the template to be used to render the page
     */
    @RequestMapping(value = { "/conversations" }, method = RequestMethod.GET)
    public String conversations(final Model model, @RequestParam("tempBlogName") final Optional<String> blogParam) {
        Metadata md = null;
        if (blogParam.isPresent()) {
            md = mdController.getMetadataForBlog(blogParam.get());
        } else {
            md = mdController.getDefaultMetadata();
        }
        model.addAttribute("metadata", md);
        final List<Conversation> conversations = convoController.getAllConversationsForBlog(md.getBlog());
        model.addAttribute("conversations", conversations);

        model.addAttribute(MODEL_ATTRIBUTE_BLOGNAME, md.getBlog());
        updateModelWithTheme(model);

        return "conversations";
    }

    /**
     * Returns the a page for maintaining the application's metadata, at /metadata. If no blogs have
     * been created (i.e. if there are no Metadata objects in the DB), the model is updated with
     * this info, so the client can create one.
     *
     * @param model not used
     * @return name of the template to be used to render the page
     */
    @RequestMapping(value = { "/metadata" }, method = RequestMethod.GET)
    public String metadata(final Model model) {
        try {
            model.addAttribute(MODEL_ATTRIBUTE_BLOGNAME, mdController.getDefaultBlogName());
            updateModelWithTheme(model);
        } catch (NoMetadataFoundException e) {
            model.addAttribute(MODEL_ATTRIBUTE_NOBLOGSCREATED, Boolean.TRUE);
            model.addAttribute(MODEL_ATTRIBUTE_THEME, Metadata.DEFAULT_THEME);
        }

        final List<Metadata> mdCollection = mdController.getAllMetadata();
        model.addAttribute(MODEL_ATTRIBUTE_MDCOLLECTION, mdCollection);
        return "metadata";
    }

    /**
     * Returns the page for maintaining metadata for a particular blog
     *
     * @param blogName Name of the blog being edited
     * @param model    Used for setting the theme
     * @return Name of the template to be used to render the page
     */
    @RequestMapping(value = { "/metadata/{blogName}" }, method = RequestMethod.GET)
    public String individualMetatada(@PathVariable("blogName") final String blogName, final Model model) {
        model.addAttribute(MODEL_ATTRIBUTE_BLOGNAME, blogName);
        addBlogNameJSToModel(model, blogName);
        updateModelWithTheme(model);

        return "metadata-frame";
    }

    /**
     * Helper function to add an attribute to the Model for a client-side bit of JS code for setting
     * a variable with the blog's name. This <i>should</i> have been as simple as setting an
     * attribute with the blog's name and then setting the JS code in the Thymeleaf page, but for
     * some reason the initial developer couldn't figure out a way to get single quotes into the
     * generated string...
     *
     * @param model    The Model to which the JS code should be added
     * @param blogName The name of the current blog
     */
    private void addBlogNameJSToModel(final Model model, final String blogName) {
        model.addAttribute(MODEL_ATTRIBUTE_BLOGNAMEJS, String.format("var blogName = \"%s\";", blogName));
    }

    /**
     * Helper function to add an attribute to the Model for a client-side bit of JS code for setting
     * a variable with the post's ID. This <i>should</i> have been as simple as setting an attribute
     * with the value and then setting the JS code in the Thymeleaf page, but for some reason the
     * initial developer couldn't figure out a way to get single quotes into the generated string...
     *
     * @param model  The Model to which the JS code should be added
     * @param postID The ID of the current post
     */
    private void addPostIdJSToModel(final Model model, final String postID) {
        model.addAttribute(MODEL_ATTRIBUTE_POSTIDJS, String.format("var postId = \"%s\"", postID));
    }

    /**
     * Handles file uploads, for reading in the Tumblr Post XML Export for a given blog. Actual
     * logic is handled by the <code>parseDocument()</code> method; this method simply calls that
     * class and then (upon success) redirects to the index. Failure redirects to the "bad XML"
     * error page.
     *
     * @param blog               The blog for which post data should be uploaded
     * @param file               The Tumblr XML file to be read
     * @param redirectAttributes not used
     * @return The page to which the successful upload should be redirected
     */
    @PostMapping("/postDataUpload/{blog}")
    public String handlePostFileUploadForBlog(@PathVariable("blog") final String blog,
            @RequestParam("file") final MultipartFile file, final RedirectAttributes redirectAttributes) {
        try {
            BlogXmlReader.parseDocument(file.getInputStream(), postController, blog);
        } catch (XMLParsingException | IOException e) {
            logger.error("UI Controller failing in handlePostFileUpload due to XML parsing error: ", e);
            return "redirect:/errorbadxml";
        }

        final String defaultBlogName = mdController.getDefaultBlogName();
        if (blog.equals(defaultBlogName)) {
            return "redirect:/index";
        } else {
            return String.format("redirect:/index?" + TEMPBLOGNAME_PARAM + "=%s", blog);
        }
    }

    /**
     * Handles file uploads for reading in Tumblr messaging XML extract. Actual logic is handled by
     * the <code>parseDocument()</code> method; this method simply calls that class and then (upon
     * success) redirects to the index. Failure redirects to the "bad XML" error page.
     *
     * @param blog               Blog for which conversation should be uploaded
     * @param file               the XML file to be parsed
     * @param redirectAttributes not used
     * @return The page to which the application should be redirected after upload
     */
    @PostMapping("/conversationDataUpload/{blog}")
    public String handleConversationFileUpload(@PathVariable("blog") final String blog,
            @RequestParam("conversationFile") final MultipartFile file, final RedirectAttributes redirectAttributes) {
        try {
            ConversationXmlReader.parseDocument(file, mdController, convoController, blog);
        } catch (BlogMismatchParsingException e) {
            logger.error("Mismatch in XML between specified blog name ({}) and name in XML ({}).", e.getBlogName(),
                    e.getMainParticipantName());
            final String redirectString = String.format("redirect:/errorblogmismatch?blogName=%s&participantName=%s",
                    e.getBlogName(), e.getMainParticipantName());
            return redirectString;
        } catch (XMLParsingException e) {
            logger.error("UI Controller failing in handleConversationFileUpload due to XML parsing error: ", e);
            return "redirect:/errorbadxml";
        }

        final String defaultBlogName = mdController.getDefaultBlogName();
        if (blog.equals(defaultBlogName)) {
            return "redirect:/conversations";
        } else {
            return String.format("redirect:/conversations?%s=%s", TEMPBLOGNAME_PARAM, blog);
        }
    }

    /**
     * <p>
     * This request is used to populate the viewer. It first determines the correct viewer to show
     * (Regular, Photo, etc.), populates the {@link org.springframework.ui.Model Model} (since the
     * pages are rendered by Thymeleaf on the server rather than jQuery on the client), then returns
     * the correct template to show that type of post.
     * </p>
     *
     * <p>
     * {@link #pullOutTagValues(String)} is leveraged for getting a nicer view of the tags in the
     * post.
     * </p>
     *
     * @param blog   The blog for which this post should be retrieved
     * @param postID The ID of the post to be viewed (regardless of type)
     * @param model  The model to be populated with post data, for use by Thymeleaf in the HTML
     *               template
     * @return The name of the template to use for rendering the output
     */
    @RequestMapping(value = { "/postViewer/{blog}" }, method = RequestMethod.GET)
    public String showViewer(@PathVariable("blog") final String blog, @RequestParam("id") final String postID,
            final Model model) {
        final Post post = postController.getPostForBlogById(blog, postID);
        model.addAttribute(MODEL_ATTRIBUTE_POST, post);
        addBlogNameJSToModel(model, blog);
        addPostIdJSToModel(model, postID);
        model.addAttribute(MODEL_ATTRIBUTE_TAGS, pullOutTagValues(post.getTags()));
        final List<String> availableTypes = mdController.getAllTypes();
        String postType = StringUtils.EMPTY;

        for (String type : availableTypes) {
            if (type.equals(post.getType())) {
                postType = type;
                break;
            }
        }

        if (postType == StringUtils.EMPTY) {
            logger.error("Post found in DB with an invalid type");
            throw new InvalidTypeException();
        }

        switch (postType) {
        case Post.POST_TYPE_REGULAR:
            final Regular reg = regController.getRegularForBlogById(post.getTumblelog(), postID);
            model.addAttribute(MODEL_ATTRIBUTE_REGULAR, reg);
            return "viewers/regular";
        case Post.POST_TYPE_LINK:
            final Link ln = postController.getLinkController().getLinkForBlogById(blog, postID);
            model.addAttribute(MODEL_ATTRIBUTE_LINK, ln);
            return "viewers/link";
        case Post.POST_TYPE_ANSWER:
            final Answer ans = postController.getAnswerController().getAnswerForBlogById(blog, postID);
            model.addAttribute(MODEL_ATTRIBUTE_ANSWER, ans);
            return "viewers/answer";
        case Post.POST_TYPE_PHOTO:
            final List<String> images = new ArrayList<String>();
            final List<Photo> photos =
                    postController.getPhotoController().getPhotoForBlogById(post.getTumblelog(), postID);
            for (int i = 0; i < photos.size(); i++) {
                final Photo photo = photos.get(i);
                final String ext = photo.getUrl1280().substring(photo.getUrl1280().lastIndexOf('.'));
                images.add(String.format("%s_%d%s", postID, i, ext));
            }
            model.addAttribute(MODEL_ATTRIBUTE_PHOTOS, images);
            model.addAttribute(MODEL_ATTRIBUTE_CAPTION, photos.get(0).getCaption());
            return "viewers/photo";
        case Post.POST_TYPE_VIDEO:
            final Video vid = postController.getVideoController().getVideoForBlogById(post.getTumblelog(), postID);
            model.addAttribute(MODEL_ATTRIBUTE_VIDEO, vid);
            return "viewers/video";
        default:
            // should never reach here
        }

        return "viewers/".concat(postType);
    }

    /**
     * This request is used to populate the hashtag viewer.
     *
     * @param model     The model to be populated with post data, for use by Thymeleaf in the HTML
     *                  template
     * @param blogParam Optional: the name of the blog for which this page should be rendered,
     *                  regardless of the default
     * @return The name of the template to use for rendering the output
     */
    @RequestMapping(value = { "/hashtagViewer" }, method = RequestMethod.GET)
    public String showHashtagViewerForBlog(final Model model,
            @RequestParam("tempBlogName") final Optional<String> blogParam) {
        final String blog = blogParam.isPresent() ? blogParam.get() : mdController.getDefaultBlogName();
        final List<Hashtag> hashtags = postController.getHashtagController().getAllHashtagsForBlog(blog);
        model.addAttribute(MODEL_ATTRIBUTE_HASHTAGS, hashtags);
        model.addAttribute(MODEL_ATTRIBUTE_BLOGNAME, blog);
        updateModelWithTheme(model);
        addBlogNameJSToModel(model, blog);
        return "viewers/hashtags";
    }

    /**
     * This request is used to view the XML for export purposes.
     *
     * @param model     The model to be populated with post data, for use by Thymeleaf (in this
     *                  case, just theme information)
     * @param blogParam Blog to use, if other than the default
     * @return The name of the template to use for rendering the output
     */
    @RequestMapping(value = { "/exportViewer" }, method = RequestMethod.GET)
    public String showExportViewer(final Model model, @RequestParam("tempBlogName") final Optional<String> blogParam) {
        if (blogParam.isPresent()) {
            model.addAttribute(MODEL_ATTRIBUTE_BLOGNAME, blogParam.get());
        } else {
            model.addAttribute(MODEL_ATTRIBUTE_BLOGNAME, mdController.getDefaultBlogName());
        }
        updateModelWithTheme(model);
        addBlogNameJSToModel(model, (String) model.getAttribute(MODEL_ATTRIBUTE_BLOGNAME));
        return "viewers/exportedxml";
    }

    /**
     * This request is to show the Staged Post viewer
     *
     * @param model     The model to be populated with a list of IDs, for use by Thymeleaf
     * @param blogParam Blog to use, if other than the default
     * @return The name of the template to use for rendering the output
     */
    @RequestMapping(value = { "/staged" }, method = RequestMethod.GET)
    public String showStagedPosts(final Model model, @RequestParam("tempBlogName") final Optional<String> blogParam) {
        if (blogParam.isPresent()) {
            model.addAttribute(MODEL_ATTRIBUTE_BLOGNAME, blogParam.get());
        } else {
            model.addAttribute(MODEL_ATTRIBUTE_BLOGNAME, mdController.getDefaultBlogName());
        }
        updateModelWithTheme(model);
        return "staged";
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
    public String showConversationViewer(@RequestParam("blog") final String blog,
            @RequestParam("participant") final String participantName, final Model model) {
        final Metadata md = mdController.getMetadataForBlog(blog);
        model.addAttribute("metadata", md);
        addBlogNameJSToModel(model, blog);
        final Conversation convo = convoController.getConversationForBlogByParticipant(md.getBlog(), participantName);
        model.addAttribute(MODEL_ATTRIBUTE_CONVERSATION, convo);
        final List<ConversationMessage> messages =
                convoController.getConvoMsgForBlogByConvoID(convo.getBlog(), convo.getId());
        model.addAttribute(MODEL_ATTRIBUTE_MESSAGES, messages);

        model.addAttribute(MODEL_ATTRIBUTE_BLOGNAME, blog);
        updateModelWithTheme(model);

        return "viewers/conversation";
    }

    /**
     * Returns a binary image, for use in the viewer. HTML pages can't directly access local images
     * or videos, so this has to be done via the "server."
     *
     * @param imageName Name of the image to be retrieved
     * @param model     not used
     * @return Byte stream of the file, as loaded from the file system
     */
    @RequestMapping(value = { "/viewerMedia/{imageName}" }, method = RequestMethod.GET, produces = {
            MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_PNG_VALUE })
    public @ResponseBody byte[] getMedia(@PathVariable(MODEL_ATTRIBUTE_IMAGENAME) final String imageName,
            final Model model) {
        final String fullName = String.format("%s/%s", mdController.getDefaultMetadata().getBaseMediaPath(), imageName);

        final File file = new File(fullName);
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            logger.warn("File {} not found.", imageName);
            throw new ResourceNotFoundException(fullName, fullName, e);
        }
    }

    /**
     * Returns a binary video, for use in the viewer. HTML pages can't directly access local images
     * or videos, so this has to be done via the "server."
     *
     * @param response HTTP Response object
     * @param request  HTTP Request object
     */
    @RequestMapping(value = { "/viewerVideo/{videoName}" }, method = RequestMethod.GET, produces = { "video/mp4" })
    public void getVideo(final HttpServletResponse response, final HttpServletRequest request) {
        final String fullName = String.format("%s/%s", mdController.getDefaultMetadata().getBaseMediaPath(),
                request.getRequestURI().substring(request.getRequestURI().lastIndexOf('/')));

        response.setContentType("video/mp4");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");

        final File file = new File(fullName);
        try {
            final FileInputStream in = new FileInputStream(file);
            final OutputStream out = response.getOutputStream();
            final byte[] buf = new byte[BYTE_BUFFER_LENGTH];
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
     * Used to request the XML export, for any posts that have been "staged" for export. Returns the
     * data as a string, with the intent that it is displayed in the browser.
     *
     * @param blog     The blog for which staged downloads should be retrieved
     * @param response The HTTP Response object (used for setting headers)
     * @param request  The HTTP Request object
     * @return The XML file, as a String
     */
    @RequestMapping(value = { "/stagedPostsDownload/{blog}" }, method = RequestMethod.GET, produces = { "text/plain" })
    public @ResponseBody String getStagedPostsFileForBlog(@PathVariable("blog") final String blog,
            final HttpServletResponse response, final HttpServletRequest request) {
        response.setContentType("application/xml");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        final List<String> postIDs = stagingController.getAllPostsForBlog(blog);

        if (postIDs.size() < 1) {
            logger.warn("No posts staged for download");
            throw new NoStagedPostsException();
        }

        final List<Post> posts = new ArrayList<Post>();
        for (String id : postIDs) {
            final Post post = postController.getPostForBlogById(blog, id);
            posts.add(post);
        }

        return BlogXmlWriter.getStagedPostXMLForBlog(postIDs, postController, blog);
    }

    /**
     * Retrieves the application version number from the application's POM, adds it to the model,
     * then returns the "footer" page.
     *
     * @param model For setting the application version, used by Thymeleaf
     * @return name of the template to be used to render the page
     */
    @RequestMapping(value = { "/footer" }, method = RequestMethod.GET)
    public String footer(final Model model) {
        final MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        String version = "";
        try {
            final org.apache.maven.model.Model mavenModel;
            if (new File("pom.xml").exists()) {
                mavenModel = mavenReader.read(new FileReader("pom.xml"));
            } else {
                mavenModel = mavenReader.read(new InputStreamReader(
                        TEVUIController.class.getResourceAsStream("/META-INF/maven/com.tiyb/tev/pom.xml")));
            }
            version = mavenModel.getVersion();
        } catch (IOException | XmlPullParserException e) {
            logger.error("Error getting version from POM", e);
        }

        model.addAttribute("applicationVersion", version);
        return "footer";
    }

    /**
     * Returns the header
     *
     * @param model not used
     * @return name of the template to be used to render the page
     */
    @RequestMapping(value = { "/header" }, method = RequestMethod.GET)
    public String header(final Model model) {
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
     * Helper method that pulls out the tags into spans. This kind of ties processing together with
     * UI, but... whatever.
     *
     * @param csvTags String containing the hashtags, separated by commas
     * @return String, containing HTML span tags containing the hashtags
     */
    private String pullOutTagValues(final String csvTags) {
        if (csvTags == null || csvTags.equals(StringUtils.EMPTY)) {
            return csvTags;
        }

        final List<String> items = Arrays.asList(csvTags.split("\\s*,\\s*"));
        final StringBuilder builder = new StringBuilder();

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
     * @param model The model to be updated
     */
    private void updateModelWithTheme(final Model model) {
        final String blogName = (String) model.getAttribute(MODEL_ATTRIBUTE_BLOGNAME);
        final Metadata md = mdController.getMetadataForBlog(blogName);
        String theme = md.getTheme();
        if (theme == null || theme.equals(StringUtils.EMPTY)) {
            theme = Metadata.DEFAULT_THEME;
            md.setTheme(theme);
            mdController.updateMetadata(md.getId(), md);
        }

        model.addAttribute(MODEL_ATTRIBUTE_THEME, theme);
    }

}
