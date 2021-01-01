package com.tiyb.tev.html.settings;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.tiyb.tev.datamodel.Post;

/**
 * Test cases for the settings page for a given blog for cases where posts also
 * need to be set up.
 * 
 * @author tiyb
 *
 */
public class BlogSettingsWithPosts extends SettingsTester {

    private Logger logger = LoggerFactory.getLogger(BlogSettingPageHtmlTests.class);

    @Rule
    public TemporaryFolder mainBlogMediaFolder = new TemporaryFolder();

    @Value("${metadata.defaultBlog.message}")
    private String blogIsDefaultMessage;
    @Value("${md_submit_success}")
    private String attributeChangedSuccessfullyMessage;

    @Before
    public void setupSite() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        restInitMainBlogSettings(Optional.of(mainBlogMediaFolder.getRoot().getAbsolutePath()));

        // TODO remove this alert handler
        webClient.setAlertHandler(new AlertHandler() {

            @Override
            public void handleAlert(Page page, String message) {
                logger.info("JS ALERT: " + message);
            }

        });

        mainPage = getSettingsPage(MAIN_BLOG_NAME);
    }

    /**
     * Miscellaneous tests for the settings page for a blog.
     * 
     * <ol>
     * <li>mark all posts read</li>
     * <li>mark all posts unread</li>
     * <li>TODO clean up images</li>
     * <li>TODO import images</li>
     * </ol>
     */
    @Test
    public void otherSettings() throws IOException {
        Post[] posts = getAllPostsFromRest(MAIN_BLOG_NAME);
        for (Post post : posts) {
            assertThat(post.getIsRead()).isFalse();
        }
        HtmlSubmitInput markAllReadButton = mainPage.getHtmlElementById("markAllPostsReadButton");
        mainPage = markAllReadButton.click();
        waitForScript();
        posts = getAllPostsFromRest(MAIN_BLOG_NAME);
        for (Post post : posts) {
            assertThat(post.getIsRead()).isTrue();
        }

        HtmlSubmitInput markAllUneadButton = mainPage.getHtmlElementById("markAllPostsUnreadButton");
        mainPage = markAllUneadButton.click();
        waitForScript();
        posts = getAllPostsFromRest(MAIN_BLOG_NAME);
        for (Post post : posts) {
            assertThat(post.getIsRead()).isFalse();
        }

    }

}
