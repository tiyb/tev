package com.tiyb.tev.html;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.gargoylesoftware.htmlunit.HttpHeader;
import com.gargoylesoftware.htmlunit.ImmediateRefreshHandler;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.SilentJavaScriptErrorListener;
import com.tiyb.tev.TevTestingClass;
import com.tiyb.tev.datamodel.Answer;
import com.tiyb.tev.datamodel.Conversation;
import com.tiyb.tev.datamodel.ConversationMessage;
import com.tiyb.tev.datamodel.FullConversation;
import com.tiyb.tev.datamodel.Hashtag;
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.datamodel.Video;

/**
 * Parent class for any HtmlUnit-based test cases. Provides a number of helper
 * functions for use in testing, and sets up the WebClient used by all tests.
 * 
 * @author tiyb
 *
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource("classpath:static/js/i18n/messages.properties")
public abstract class HtmlTestingClass extends TevTestingClass {

    private final static int WAIT_TIME_FOR_JS = 60000;

    @Autowired
    private TestRestTemplate restTemplate;

    protected WebClient webClient;
    protected HtmlPage mainPage;

    /**
     * The server starts with a random port for each execution; this property
     * contains the port currently being used.
     */
    @LocalServerPort
    protected int serverPort;

    /**
     * Called before each test (and before the <code>@Before</code> method of child
     * classes) to set up the {@link com.gargoylesoftware.htmlunit.WebClient
     * WebClient} object.
     */
    @Before
    public void setupWebClient() {
        webClient = new WebClient(); //BrowserVersion.CHROME
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPopupBlockerEnabled(false);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getCache().setMaxSize(0);
        webClient.getCache().clear();
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        webClient.setJavaScriptErrorListener(new SilentJavaScriptErrorListener());
        webClient.setIncorrectnessListener(new SilentHtmlIncorrectnessListener());
        webClient.setRefreshHandler(new ImmediateRefreshHandler());
        webClient.addRequestHeader(HttpHeader.ACCEPT_LANGUAGE, "en");
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.SEVERE);
    }

    /**
     * Called after each test (and after the <code>@After</code> method for any
     * child class)
     */
    @After
    public void teardownWebClient() {
        webClient.close();
    }

    /**
     * Called to wait for any background JavaScript code that might be running in
     * the "browser"
     */
    protected void waitForScript() {
        webClient.waitForBackgroundJavaScript(WAIT_TIME_FOR_JS);
    }

    /**
     * Called to initialize conversations for the "main" blog via REST calls.
     * Deletes any existing conversation data, first, to load it all from scratch.
     */
    protected void restInitConvosForMainBlog() {
        restTemplate.delete(String.format("%s/api/conversations/%s/messages", baseUri(), MAIN_BLOG_NAME));
        restTemplate.delete(String.format("%s/api/conversations/%s", baseUri(), MAIN_BLOG_NAME));

        for (FullConversation fc : conversationsToUpload) {

            Conversation convoOnServer = restTemplate.postForObject(
                    baseUri() + "/api/conversations/" + fc.getConversation().getBlog(), fc.getConversation(),
                    Conversation.class);

            for (ConversationMessage message : fc.getMessages()) {
                message.setConversationId(convoOnServer.getId());
                restTemplate.postForObject(baseUri() + "/api/conversations/" + convoOnServer.getBlog() + "/messages",
                        message, ConversationMessage.class);
            }
        }
        
        Metadata md = getMDFromServer(Optional.of(MAIN_BLOG_NAME));
        md.setMainTumblrUser(MAIN_BLOG_NAME);
        md.setMainTumblrUserAvatarUrl("http://mainblog/avatar");
        updateMD(md);
    }

    /**
     * Sets up initial settings + post data for the "main" blog via REST calls.
     * Leverages {@link #restInitMainBlogSettings(Optional)} method to initialize
     * the settings.
     * 
     * @param baseMediaPath (Optional) path to where media is stored for the blog
     */
    protected void restInitDataForMainBlog(Optional<String> baseMediaPath) {
        restInitMainBlogSettings(baseMediaPath);

        deletePostDataForBlog(MAIN_BLOG_NAME);

        for (Post post : postsForUploading) {
            restTemplate.postForObject(String.format("%s/api/posts/%s", baseUri(), MAIN_BLOG_NAME), post, Post.class);
            setHashtags(MAIN_BLOG_NAME, post.getTags());
        }

        for (Regular reg : regularsForUploading) {
            restTemplate.postForObject(
                    String.format("%s/api/posts/%s/%s/regular", baseUri(), MAIN_BLOG_NAME, reg.getPostId()), reg,
                    Regular.class);
        }

        for (Answer answer : answersForUploading) {
            restTemplate.postForObject(
                    String.format("%s/api/posts/%s/%s/answer", baseUri(), MAIN_BLOG_NAME, answer.getPostId()), answer,
                    Answer.class);
        }

        for (Link link : linksForUploading) {
            restTemplate.postForObject(
                    String.format("%s/api/posts/%s/%s/link", baseUri(), MAIN_BLOG_NAME, link.getPostId()), link,
                    Link.class);
        }

        for (Photo photo : photosForUploading) {
            restTemplate.postForObject(String.format("%s/api/posts/%s/photo", baseUri(), MAIN_BLOG_NAME), photo,
                    Photo.class);
        }

        for (Video vid : videosForUploading) {
            restTemplate.postForObject(
                    String.format("%s/api/posts/%s/%s/video", baseUri(), MAIN_BLOG_NAME, vid.getPostId()), vid,
                    Video.class);
        }
    }

    /**
     * Sets up initial settings + post data for the "main" blog via REST calls.
     * Leverages {@link #restInitMainBlogSettings(Optional)} method to initialize
     * the settings.
     */
    protected void restInitDataForSecondBlog() {
        restInitAdditionalBlog(SECOND_BLOG_NAME);

        deletePostDataForBlog(SECOND_BLOG_NAME);

        for (Post post : postsForUploadingForSecondBlog) {
            restTemplate.postForObject(String.format("%s/api/posts/%s", baseUri(), SECOND_BLOG_NAME), post, Post.class);
            setHashtags(SECOND_BLOG_NAME, post.getTags());
        }

        for (Regular reg : regularsForUploadingSecondBlog) {
            String url = String.format("%s/api/posts/%s/%s/regular", baseUri(), SECOND_BLOG_NAME, reg.getPostId());
            restTemplate.postForObject(url, reg, Regular.class);
        }
    }

    /**
     * Removes post data from the DB for a given blog
     * 
     * @param blogName Name of the blog for which to remove data
     */
    private void deletePostDataForBlog(String blogName) {
        restTemplate.delete(String.format("%s/staging-api/posts/%s", baseUri(), blogName));
        restTemplate.delete(String.format("%s/api/posts/%s/regulars", baseUri(), blogName));
        restTemplate.delete(String.format("%s/api/posts/%s/answers", baseUri(), blogName));
        restTemplate.delete(String.format("%s/api/posts/%s/links", baseUri(), blogName));
        restTemplate.delete(String.format("%s/api/posts/%s/photos", baseUri(), blogName));
        restTemplate.delete(String.format("%s/api/posts/%s/videos", baseUri(), blogName));
        restTemplate.delete(String.format("%s/api/hashtags/forBlog/%s", baseUri(), blogName));
        restTemplate.delete(String.format("%s/api/posts/%s", baseUri(), blogName));
    }

    /**
     * Adds a set of hashtags to the DB for a given blog
     * 
     * @param blogName Name of the blog for which the hshtags should be added
     * @param tags     String containing a comma-separated list of tags
     */
    private void setHashtags(String blogName, String tags) {
        if (tags.length() < 1) {
            return;
        }

        String[] individualTags = tags.split(",");
        String url = String.format("%s/api/hashtags/%s", baseUri(), blogName);
        for (int i = 0; i < individualTags.length; i++) {
            individualTags[i] = individualTags[i].trim();
            restTemplate.postForObject(url, individualTags[i], Hashtag.class);
        }
    }

    /**
     * Gets metadata for a given blog from the server via REST calls.
     * 
     * @param blogName Name of the blog for which to retrieve the data
     * @return Metadata object for the given blog (or the default MD object if none
     *         existed)
     */
    protected Metadata getMDFromServer(Optional<String> blogName) {
        String blogForWhichToFetchMD;
        if (blogName.isPresent()) {
            blogForWhichToFetchMD = blogName.get();
        } else {
            blogForWhichToFetchMD = MAIN_BLOG_NAME;
        }

        return restTemplate.getForObject(
                String.format("%s/api/metadata/byBlog/%s/orDefault", baseUri(), blogForWhichToFetchMD), Metadata.class);
    }

    /**
     * Gets metadata for a given blog from the server via REST calls. Will
     * <i>not</i> use the "or default" option provided by the API.
     * 
     * @param blogName Name of the blog for which to retrieve the data
     * @return Metadata object for the given blog (or the default MD object if none
     *         existed)
     */
    protected Metadata getMDFromServerNotDefault(Optional<String> blogName) {
        String blogForWhichToFetchMD;
        if (blogName.isPresent()) {
            blogForWhichToFetchMD = blogName.get();
        } else {
            blogForWhichToFetchMD = MAIN_BLOG_NAME;
        }

        return restTemplate.getForObject(String.format("%s/api/metadata/byBlog/%s", baseUri(), blogForWhichToFetchMD),
                Metadata.class);
    }

    /**
     * Updates a MD object via REST
     * 
     * @param md The object to be updated
     */
    protected void updateMD(Metadata md) {
        restTemplate.put(String.format("%s/api/metadata/%d", baseUri(), md.getId()), md);
    }

    /**
     * Initializes the settings for the main blog
     * 
     * @param baseMediaPath (Optional) path to use for media for this blog
     */
    protected void restInitMainBlogSettings(Optional<String> baseMediaPath) {
        Metadata mainBlogMD = null;
        Metadata[] blogs = getAllMDObjects();
        if(blogs.length > 1) {
            for(Metadata m : blogs) {
                if((m.getBlog().equals(MAIN_BLOG_NAME)) || (m.getBlog() == null)) {
                    mainBlogMD = m;
                } else {
                    String deleteUri = String.format("%s/api/metadata/%d", baseUri(), m.getId());
                    restTemplate.delete(deleteUri);
                }
            }
        } else {
            mainBlogMD = blogs[0];
        }
        
        mainBlogMD.setBlog(MAIN_BLOG_NAME);
        mainBlogMD.setOverwritePostData(true);
        mainBlogMD.setOverwriteConvoData(true);
        mainBlogMD.setMainTumblrUser(MAIN_BLOG_NAME);
        mainBlogMD.setIsDefault(true);
        mainBlogMD.setConversationDisplayStyle("table");
        mainBlogMD.setConversationSortColumn("numMessages");
        mainBlogMD.setConversationSortOrder("Descending");
        mainBlogMD.setFavFilter("Show Everything");
        mainBlogMD.setFilter("Do Not Filter");
        mainBlogMD.setPageLength(10);
        mainBlogMD.setShowHashtagsForAllBlogs(true);
        mainBlogMD.setShowReadingPane(false);
        mainBlogMD.setSortColumn("ID");
        mainBlogMD.setSortOrder("Descending");
        mainBlogMD.setTheme(Metadata.DEFAULT_THEME); 
        mainBlogMD.setShowReadingPane(false);
        mainBlogMD.setExportImagesFilePath("");
        if (baseMediaPath.isPresent()) {
            mainBlogMD.setBaseMediaPath(baseMediaPath.get());
        }

        updateMD(mainBlogMD);
    }

    /**
     * Initializes settings for any blog (by name)
     * 
     * @param blogName Name of the blog for which to initialize
     */
    protected void restInitAdditionalBlog(String blogName) {
        Metadata md = getMDFromServer(Optional.of(blogName));
        md.setBlog(blogName);
        md.setIsDefault(false);
        md.setOverwritePostData(true);
        md.setOverwriteConvoData(true);
        md.setMainTumblrUser(blogName);
        md.setConversationDisplayStyle("table");
        md.setConversationSortColumn("numMessages");
        md.setConversationSortOrder("Descending");
        md.setFavFilter("Show Everything");
        md.setFilter("Do Not Filter");
        md.setPageLength(10);
        md.setShowHashtagsForAllBlogs(true);
        md.setShowReadingPane(false);
        md.setSortColumn("ID");
        md.setSortOrder("Descending");
        md.setTheme(Metadata.DEFAULT_THEME);

        updateMD(md);
    }

    /**
     * Gets the base URI for this test
     * 
     * @return URL string with http, localhost, current server port.
     */
    protected String baseUri() {
        return "http://localhost:" + serverPort;
    }

    /**
     * Returns the number of "real" windows in play. Ignores frame windows, which
     * don't count, as well as extraneous about:help windows that are sometimes
     * created by HtmlUnit, seemingly at random.
     * 
     * @return The number of "real" windows in play; the main window, plus any
     *         pop-ups.
     */
    protected int getNumRealWindows() {
        int i = 0;

        for (WebWindow ww : webClient.getWebWindows()) {
            if (ww instanceof FrameWindow) {
                continue;
            }
            HtmlPage p = (HtmlPage) ww.getEnclosedPage();
            String theURL = p.getUrl().toString();
            if (theURL.contains("localhost")) {
                i++;
            }
        }

        return i;
    }

    /**
     * Retrieves a post from the server via REST
     * 
     * @param blogName Blog to which the post belongs
     * @param postID   ID of the post to retrieve
     * @return Post object
     */
    protected Post getPostFromRest(String blogName, String postID) {
        return restTemplate.getForObject(String.format("%s/api/posts/%s/%s", baseUri(), blogName, postID), Post.class);
    }
    
    /**
     * Retrieves all posts for a given blog
     * 
     * @param blogName Name of the blog for which posts should be retrieved
     * @return Array of Post objects
     */
    protected Post[] getAllPostsFromRest(String blogName) {
        ResponseEntity<Post[]> responseEntity = restTemplate
                .getForEntity(String.format("%s/api/posts/%s", baseUri(), blogName), Post[].class);
        return responseEntity.getBody();
    }

    /**
     * Retrieves a conversation from the server via REST
     * 
     * @param blogName        Name of the blog for which the conversation exists
     * @param participantName Name of the participant of the conversation
     * @return A Conversation object
     */
    protected Conversation getConversation(String blogName, String participantName) {
        String url = String.format("%s/api/conversations/%s/byParticipant/%s", baseUri(), blogName, participantName);
        return restTemplate.getForObject(url, Conversation.class);
    }

    /**
     * Updates the server with new conversation details via REST
     * 
     * @param convo Conversation to be updated
     */
    protected void updateConversation(Conversation convo) {
        restTemplate.put(String.format("%s/api/conversations/%s/%d", baseUri(), convo.getBlog(), convo.getId()), convo);
    }

    /**
     * Returns all staged posts for a blog
     * 
     * @param blogName Blog for which to return the staged posts
     * @return Array of Strings containing the post IDs (because that's how the API
     *         works)
     */
    protected String[] getStagedPostsForBlog(String blogName) {
        ResponseEntity<String[]> responseEntity = restTemplate
                .getForEntity(String.format("%s/staging-api/posts/%s", baseUri(), blogName), String[].class);
        return responseEntity.getBody();
    }

    /**
     * Return all hashtags
     * 
     * @return Array of Hashtag objects
     */
    protected Hashtag[] getAllHashtags() {
        ResponseEntity<Hashtag[]> responseEntity = restTemplate
                .getForEntity(String.format("%s/api/hashtags", baseUri()), Hashtag[].class);
        return responseEntity.getBody();
    }

    /**
     * Deletes all hashtags from the server.
     * 
     * @throws URISyntaxException Thrown if the URL for deleting the hashtag can't
     *                            be created properly
     */
    protected void deleteAllHashtags() throws URISyntaxException {
        Hashtag[] allTags = getAllHashtags();

        for (int i = 0; i < allTags.length; i++) {
            String htUrl = String.format("%s/api/hashtags/%d", baseUri(), allTags[i].getId());
            URI uri = new URI(htUrl);
            restTemplate.delete(uri);
        }
    }

    /**
     * Returns Metadata objects for all blogs in the system
     * 
     * @return Array of Metadata objects
     */
    protected Metadata[] getAllMDObjects() {
        ResponseEntity<Metadata[]> responseEntity = restTemplate
                .getForEntity(String.format("%s/api/metadata", baseUri()), Metadata[].class);
        return responseEntity.getBody();
    }

}
