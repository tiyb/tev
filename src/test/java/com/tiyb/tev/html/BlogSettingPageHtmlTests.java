package com.tiyb.tev.html;

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
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.tiyb.tev.datamodel.Metadata;

public class BlogSettingPageHtmlTests extends HtmlTestingClass {

    private static final String SET_DEFAULT_BLOG_BUTTON = "setDefaultBlogButton";

    private static final String BLOG_IS_DEFAULT_MESSAGE = "blogIsDefaultMessage";

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
        restInitAdditionalBlog(SECOND_BLOG_NAME);

        webClient.setAlertHandler(new AlertHandler() {

            @Override
            public void handleAlert(Page page, String message) {
                logger.info("JS ALERT: " + message);
            }
            
        });
        
        mainPage = getSettingsPage(MAIN_BLOG_NAME);
    }

    /**
     * Tests for the very high-level blog settings
     * 
     * <ol>
     * <li>Checks that the correct messaging about default or non-default shows up,
     * along with the "make default blog" button only showing up when the blog is
     * <i>not</i> the default</li>
     * <li>Makes a non-default blog the default, then back, asserting that metadata is changed appropriately</li>
     * <li>Changes the blog name</li>
     * <li>TODO delete the blog</li>
     * </ol>
     */
    @Test
    public void mainBlogSettings() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        HtmlParagraph mainBlogMessage = mainPage.getHtmlElementById(BLOG_IS_DEFAULT_MESSAGE);
        assertThat(mainBlogMessage.isDisplayed()).isTrue();
        assertThat(mainBlogMessage.asText()).isEqualToNormalizingWhitespace(blogIsDefaultMessage);
        HtmlButtonInput makeDefaultButton = mainPage.getHtmlElementById(SET_DEFAULT_BLOG_BUTTON);
        assertThat(makeDefaultButton.isDisplayed()).isFalse();
        HtmlPage alternateBlogSettingsPage = getSettingsPage(SECOND_BLOG_NAME);
        mainBlogMessage = alternateBlogSettingsPage.getHtmlElementById(BLOG_IS_DEFAULT_MESSAGE);
        assertThat(mainBlogMessage.isDisplayed()).isFalse();
        makeDefaultButton = alternateBlogSettingsPage.getHtmlElementById(SET_DEFAULT_BLOG_BUTTON);
        assertThat(makeDefaultButton.isDisplayed()).isTrue();
        
        makeDefaultButton.click();
        waitForScript();
        Metadata md = getMDFromServer(Optional.of(MAIN_BLOG_NAME));
        assertThat(md.getIsDefault()).isFalse();
        md = getMDFromServer(Optional.of(SECOND_BLOG_NAME));
        assertThat(md.getIsDefault()).isTrue();
        mainPage = getSettingsPage(MAIN_BLOG_NAME);
        makeDefaultButton = mainPage.getHtmlElementById(SET_DEFAULT_BLOG_BUTTON);
        assertThat(makeDefaultButton.isDisplayed()).isTrue();
        makeDefaultButton.click();
        waitForScript();
        
        md = getMDFromServer(Optional.of(MAIN_BLOG_NAME));
        assertThat(md.getIsDefault()).isTrue();
        md = getMDFromServer(Optional.of(SECOND_BLOG_NAME));
        assertThat(md.getIsDefault()).isFalse();
        
        logger.info("about to change blog name");
        HtmlTextInput blogNameInput = mainPage.getHtmlElementById("blogNameInput");
        HtmlTextInput baseMediaPathInput = mainPage.getHtmlElementById("baseMediaPath");
        assertThat(blogNameInput.getText()).isEqualTo(MAIN_BLOG_NAME);
        blogNameInput.focus();
        blogNameInput.setText("blah");
        baseMediaPathInput.focus();
        waitForScript();
        logger.info("changed blog name");
        
        md = getMDFromServerNotDefault(Optional.of("blah"));
        assertThat(md).isNotNull();
        Metadata[] allMDObjects = getAllMDObjects();
        assertThat(allMDObjects.length).isEqualTo(2);
        
        blogNameInput.focus();
        blogNameInput.setText(MAIN_BLOG_NAME);
        baseMediaPathInput.focus();
        waitForScript();
        
        md = getMDFromServerNotDefault(Optional.of(MAIN_BLOG_NAME));
        assertThat(md).isNotNull();
        allMDObjects = getAllMDObjects();
        assertThat(allMDObjects.length).isEqualTo(2);
    }

    @Test
    public void postViewSettings() {
        // TODO base media path
        // TODO filter
        // TODO sort order
        // TODO show favs
        // TODO num items to show
        // TODO reading pane
        // TODO overwrite posts
        // TODO export path
        // TODO theme
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void conversationSettings() {
        // TODO verify user name and avatar show up
        // TODO conversation display style
        // TODO conversation sorting
        // TODO overwrite convos
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void uploads() {
        // TODO upload posts
        // TODO upload convos
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void otherSettings() {
        // TODO mark all posts read
        // TODO mark all posts unread
        // TODO clean up images
        // TODO import images
        assertThat(true).isEqualTo(false);
    }

    /**
     * Helper function to get the settings page for a particular blog
     * 
     * @param blogName Name of the blog for which to retrieve the settings page
     * @return The HtmlPage object, <i>after</i> the scripts have finished running
     */
    private HtmlPage getSettingsPage(String blogName)
            throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        HtmlPage page = webClient.getPage(baseUri() + "/metadata/" + blogName);
        waitForScript();
        return page;
    }

}
