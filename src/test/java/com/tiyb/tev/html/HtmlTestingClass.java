package com.tiyb.tev.html;

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.gargoylesoftware.htmlunit.HttpHeader;
import com.gargoylesoftware.htmlunit.ImmediateRefreshHandler;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.SilentJavaScriptErrorListener;
import com.tiyb.tev.FullConversation;
import com.tiyb.tev.TevTestingHelpers;
import com.tiyb.tev.datamodel.Answer;
import com.tiyb.tev.datamodel.Conversation;
import com.tiyb.tev.datamodel.ConversationMessage;
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.datamodel.Video;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource("classpath:static/js/i18n/messages.properties")
public abstract class HtmlTestingClass {

    protected final static int WAIT_TIME_FOR_JS = 60000;

    @Autowired
    protected TestRestTemplate restTemplate;

    protected WebClient webClient;
    protected HtmlPage mainPage;

    @LocalServerPort
    protected int serverPort;

    @Before
    public void setupWebClient() {
        webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPopupBlockerEnabled(false);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getCache().setMaxSize(0);
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        webClient.setJavaScriptErrorListener(new SilentJavaScriptErrorListener());
        webClient.setIncorrectnessListener(new SilentHtmlIncorrectnessListener());
        webClient.setRefreshHandler(new ImmediateRefreshHandler());
        webClient.addRequestHeader(HttpHeader.ACCEPT_LANGUAGE, "en");
    }

    @After
    public void teardownWebClient() {
        webClient.close();
    }

    protected void restInitConvosForMainBlog() {
        restTemplate.delete(baseUri() + "/api/conversations/" + TevTestingHelpers.MAIN_BLOG_NAME + "/messages");
        restTemplate.delete(baseUri() + "/api/conversations/" + TevTestingHelpers.MAIN_BLOG_NAME);

        for (FullConversation fc : TevTestingHelpers.conversationsToUpload) {

            Conversation convoOnServer = restTemplate.postForObject(
                    baseUri() + "/api/conversations/" + fc.getConversation().getBlog(), fc.getConversation(),
                    Conversation.class);

            for (ConversationMessage message : fc.getMessages()) {
                message.setConversationId(convoOnServer.getId());
                restTemplate.postForObject(baseUri() + "/api/conversations/" + convoOnServer.getBlog() + "/messages",
                        message, ConversationMessage.class);
            }
        }
    }

    protected void restInitDataForMainBlog(Optional<String> baseMediaPath) {
        restInitMainBlogSettings(baseMediaPath);

        restTemplate.delete(baseUri() + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME + "/regulars");
        restTemplate.delete(baseUri() + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME + "/answers");
        restTemplate.delete(baseUri() + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME + "/links");
        restTemplate.delete(baseUri() + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME + "/photos");
        restTemplate.delete(baseUri() + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME + "/videos");
        restTemplate.delete(baseUri() + "/api/hashtags/" + TevTestingHelpers.MAIN_BLOG_NAME);
        restTemplate.delete(baseUri() + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME);

        for (Post post : TevTestingHelpers.postsForUploading) {
            restTemplate.postForObject(baseUri() + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME, post, Post.class);
        }

        for (Regular reg : TevTestingHelpers.regularsForUploading) {
            restTemplate.postForObject(
                    baseUri() + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME + "/" + reg.getPostId() + "/regular",
                    reg, Regular.class);
        }

        for (Answer answer : TevTestingHelpers.answersForUploading) {
            restTemplate.postForObject(
                    baseUri() + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME + "/" + answer.getPostId() + "/answer",
                    answer, Answer.class);
        }

        for (Link link : TevTestingHelpers.linksForUploading) {
            restTemplate.postForObject(
                    baseUri() + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME + "/" + link.getPostId() + "/link",
                    link, Link.class);
        }

        for (Photo photo : TevTestingHelpers.photosForUploading) {
            restTemplate.postForObject(baseUri() + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME + "/photo", photo,
                    Photo.class);
        }

        for (Video vid : TevTestingHelpers.videosForUploading) {
            restTemplate.postForObject(
                    baseUri() + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME + "/" + vid.getPostId() + "/video",
                    vid, Video.class);
        }
    }

    protected Metadata getMDFromServer(Optional<String> blogName) {
        String blogForWhichToFetchMD;
        if (blogName.isPresent()) {
            blogForWhichToFetchMD = blogName.get();
        } else {
            blogForWhichToFetchMD = TevTestingHelpers.MAIN_BLOG_NAME;
        }

        return restTemplate.getForObject(baseUri() + "/api/metadata/byBlog/" + blogForWhichToFetchMD, Metadata.class);
    }

    protected void updateMD(Metadata md) {
        restTemplate.put(baseUri() + "/api/metadata/" + md.getId(), md);
    }

    protected void restInitMainBlogSettings(Optional<String> baseMediaPath) {
        Metadata md = restTemplate.getForObject(
                baseUri() + "/api/metadata/byBlog/" + TevTestingHelpers.MAIN_BLOG_NAME + "/orDefault", Metadata.class);
        md.setOverwritePostData(true);
        md.setOverwriteConvoData(true);
        md.setMainTumblrUser(TevTestingHelpers.MAIN_BLOG_NAME);
        md.setIsDefault(true);
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
        md.setTheme("base");
        if (baseMediaPath.isPresent()) {
            md.setBaseMediaPath(baseMediaPath.get());
        }

        restTemplate.put(baseUri() + "/api/metadata/" + md.getId(), md);
    }

    protected void restInitAdditionalBlog(String blogName) {
        Metadata md = restTemplate.getForObject(baseUri() + "/api/metadata/byBlog/" + blogName + "/orDefault",
                Metadata.class);
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
        md.setTheme("base");

        restTemplate.put(baseUri() + "/api/metadata/" + md.getId(), md);
    }

    protected String baseUri() {
        return "http://localhost:" + serverPort;
    }

    protected int getNumRealWindows(List<WebWindow> allWindows) {
        int i = 0;

        for (WebWindow ww : allWindows) {
            HtmlPage p = (HtmlPage) ww.getEnclosedPage();
            String theURL = p.getUrl().toString();
            if (theURL.contains("localhost")) {
                i++;
            }
        }

        return i;
    }
}
