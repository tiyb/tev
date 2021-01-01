package com.tiyb.tev.html.settings;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.tiyb.tev.datamodel.Metadata;

/**
 * Test cases for the settings page for a given blog. These test cases require
 * two blogs to be initialized to test properly.
 * 
 * @author tiyb
 *
 */
public class BlogSettingsTwoBlogs extends SettingsTester {

    private static final String DELETE_BLOG_BUTTON = "deleteBlogButton";

    private static final String SET_DEFAULT_BLOG_BUTTON = "setDefaultBlogButton";

    private static final String BLOG_IS_DEFAULT_MESSAGE = "blogIsDefaultMessage";
    
    @Value("${metadata.defaultBlog.message}")
    private String blogIsDefaultMessage;
    @Value("${md_submit_success}")
    private String attributeChangedSuccessfullyMessage;

    @Before
    public void setupSite() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        restInitMainBlogSettings(Optional.empty());
        restInitAdditionalBlog(SECOND_BLOG_NAME);

        mainPage = getSettingsPage(MAIN_BLOG_NAME);
    }

    /**
     * Tests settings around the main blog
     * 
     * <ol>
     * <li>Checks that the correct messaging about default or non-default shows up,
     * along with the "make default blog" button only showing up when the blog is
     * <i>not</i> the default</li>
     * <li>Makes a non-default blog the default, then back, asserting that metadata
     * is changed appropriately</li>
     * </ol>
     */
    @Test
    public void setDefaultBlog() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
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
    }

    /**
     * Tests deleting the blog
     */
    @Test
    public void deleteBlog() throws IOException {
        HtmlButtonInput deleteButton = mainPage.getHtmlElementById(DELETE_BLOG_BUTTON);
        mainPage = deleteButton.click();
        waitForScript();

        Metadata[] allMDObjects = getAllMDObjects();
        assertThat(allMDObjects.length).isEqualTo(1);
    }

}
