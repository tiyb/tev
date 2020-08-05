package com.tiyb.tev.html;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.PromptHandler;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.tiyb.tev.datamodel.Metadata;

/**
 * Unit tests for the "main" settings page (i.e. the one that lists all blogs,
 * and allows for the creation of a new one)
 * 
 * @author tiyb
 *
 */
public class MainSettingPageHtmlTests extends HtmlTestingClass {

    @Value("${md_createBlog_errorMessage}")
    private String pleaseEnterBlogErrorMessage;

    /**
     * Sets up the main and secondary blogs, and retrieves the settings page
     */
    @Before
    public void setupSite() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        restInitDataForMainBlog(Optional.empty());
        restInitAdditionalBlog(SECOND_BLOG_NAME);

        mainPage = webClient.getPage(baseUri() + "/metadata");
        waitForScript();
    }

    /**
     * Tests creating a new blog
     */
    @SuppressWarnings("serial")
    @Test
    public void createNewBlog() throws IOException {
        List<Metadata> mdObjects = Arrays.asList(getAllMDObjects());
        assertThat(mdObjects.size()).isEqualTo(2);

        webClient.setPromptHandler(new PromptHandler() {

            @Override
            public String handlePrompt(Page page, String message, String defaultValue) {
                return "newblogname";
            }

        });

        HtmlButtonInput newBlogButton = mainPage.getHtmlElementById("newBlogButton");
        newBlogButton.click();
        waitForScript();

        mdObjects = Arrays.asList(getAllMDObjects());
        assertThat(mdObjects.size()).isEqualTo(3);

        Metadata newMD = getMDFromServer(Optional.of("newblogname"));
        assertThat(newMD).isNotNull();
        assertThat(newMD.getBlog()).isEqualTo("newblogname");
    }

    /**
     * Clicks the create new blog button, then cancels, and tests that the right
     * error message shows up
     */
    @SuppressWarnings("serial")
    @Test
    public void cancelNewBlogCreate() throws IOException {
        webClient.setPromptHandler(new PromptHandler() {

            @Override
            public String handlePrompt(Page page, String message, String defaultValue) {
                return null;
            }

        });

        HtmlButtonInput newBlogButton = mainPage.getHtmlElementById("newBlogButton");
        newBlogButton.click();

        HtmlDivision errorsDiv = mainPage.getHtmlElementById("errorMessageText");
        DomNodeList<DomNode> children = errorsDiv.getChildNodes();
        assertThat(children.size()).isEqualTo(1);
        HtmlDivision childDiv = (HtmlDivision) children.get(0);
        assertThat(childDiv.getVisibleText()).isEqualToNormalizingWhitespace(pleaseEnterBlogErrorMessage);
    }

    /**
     * Clicks one of the links to a particular blog, and verifies that the settings
     * page for that blog comes up
     */
    @Test
    public void clickABlog() throws IOException {
        String xpath = String.format("//a[text()='%s']", MAIN_BLOG_NAME);
        HtmlAnchor mainBlogAnchor = (HtmlAnchor) mainPage.getByXPath(xpath).get(0);
        mainPage = mainBlogAnchor.click();
        waitForScript();
        assertThat(getNumRealWindows()).isEqualTo(1);

        List<WebWindow> windows = webClient.getWebWindows();
        boolean pageChecked = false;
        for (WebWindow win : windows) {
            String url = win.getEnclosedPage().getUrl().toString();
            if (url.contains("localhost")) {
                assertThat(url).contains(MAIN_BLOG_NAME);
                pageChecked = true;
            }
        }
        assertThat(pageChecked).isTrue();
    }

}
