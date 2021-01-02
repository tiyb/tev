package com.tiyb.tev.html.settings;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Value;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.tiyb.tev.datamodel.Post;

/**
 * Test cases for the settings page for a given blog for cases where posts also
 * need to be set up.
 * 
 * @author tiyb
 *
 */
public class BlogSettingsWithPosts extends SettingsTester {

    @Rule
    public TemporaryFolder mainBlogMediaFolder = new TemporaryFolder();
    
    @Rule
    public TemporaryFolder tempImageImportFolder = new TemporaryFolder();

    @Value("${metadata.defaultBlog.message}")
    private String blogIsDefaultMessage;
    @Value("${md_submit_success}")
    private String attributeChangedSuccessfullyMessage;

    @Before
    public void setupSite() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        restInitDataForMainBlog(Optional.of(mainBlogMediaFolder.getRoot().getAbsolutePath()));

        mainPage = getSettingsPage(MAIN_BLOG_NAME);
    }

    /**
     * Miscellaneous tests for the settings page for a blog.
     * 
     * <ol>
     * <li>mark all posts read</li>
     * <li>mark all posts unread</li>
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
    
    @Test
    public void cleanImages() throws IOException {
        mainBlogMediaFolder.newFile("blah.txt");
        HtmlSubmitInput cleanImagesBtn = mainPage.getHtmlElementById("cleanImagesButton");
        mainPage = cleanImagesBtn.click();
        waitForScript();
        assertThat(mainBlogMediaFolder.getRoot().list().length).isEqualTo(0);
    }

    @Test
    public void importImages() throws IOException {
        tempImageImportFolder.newFile("180784644740_0.gif");
        tempImageImportFolder.newFile("180254465582_0.gif");
        tempImageImportFolder.newFile("180254465582_1.gif");
        
        HtmlTextInput tempImagePathInput = mainPage.getHtmlElementById("importImagesPath");
        tempImagePathInput.setText(tempImageImportFolder.getRoot().getAbsolutePath());
        HtmlSubmitInput importImagesBtn = mainPage.getHtmlElementById("importImagesButton");
        mainPage = importImagesBtn.click();
        waitForScript();
        assertThat(mainBlogMediaFolder.getRoot().list().length).isEqualTo(3);
    }
}
