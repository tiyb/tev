package com.tiyb.tev.html;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.tiyb.tev.datamodel.Hashtag;

/**
 * Unit tests for the Hashtag viewer page
 * 
 * @author tiyb
 *
 */
public class HashtagsHtmlTests extends HtmlTestingClass {

    private HtmlTable tagsTable;

    @Value("${htviewer_table_removeBtn}")
    private String removeButtonText;

    /**
     * Deletes all hashtags (including ones not associated with any blog), then sets
     * up data for the main and secondary blogs
     */
    @Before
    public void setupSite()
            throws FailingHttpStatusCodeException, MalformedURLException, IOException, URISyntaxException {
        deleteAllHashtags();
        restInitDataForMainBlog(Optional.empty());
        restInitDataForSecondBlog();

        mainPage = webClient.getPage(baseUri() + "/hashtagViewer");
        waitForScript();
        tagsTable = mainPage.getHtmlElementById("tagsTable");
    }

    /**
     * Tests that the main screen shows up, then randomly tests values from the
     * table.
     */
    @Test
    public void loadPage() {
        Hashtag[] hashtags = getAllHashtags();
        assertThat(hashtags.length).isEqualTo(16);

        assertThat(tagsTable.getRowCount()).isEqualTo(18);

        // not checking every cell, but random selections
        HtmlTableCell cell = tagsTable.getCellAt(2, 0);
        assertThat(cell.asText()).isEqualToNormalizingWhitespace("2ndtag1");

        cell = tagsTable.getCellAt(3, 1);
        assertThat(cell.asText()).isEqualToNormalizingWhitespace("mainblog");

        cell = tagsTable.getCellAt(4, 2);
        assertThat(cell.asText()).isEqualToNormalizingWhitespace("1");

        cell = tagsTable.getCellAt(5, 3);
        HtmlButton removeButton = (HtmlButton) cell.getFirstElementChild();
        assertThat(removeButton).isNotNull();
        assertThat(removeButton.asText()).isEqualToNormalizingWhitespace(removeButtonText);

        cell = tagsTable.getCellAt(10, 2);
        assertThat(cell.asText()).isEqualToNormalizingWhitespace("5");

        cell = tagsTable.getCellAt(10, 3);
        assertThat(cell.asText()).isEmpty();

        cell = tagsTable.getCellAt(3, 2);
        assertThat(cell.asText()).isEqualToNormalizingWhitespace("4");
    }

    /**
     * Tests that the same tag created in multiple blogs shows up properly in the
     * table
     */
    @Test
    public void tagForMultipleBlogs() {
        HtmlTableCell cell = tagsTable.getCellAt(10, 1);
        assertThat(cell.asText()).isEqualToNormalizingWhitespace("mainblog, secondblog");
    }

    /**
     * Tests the radio buttons for showing all tags vs. just tags for the current
     * blog
     */
    @Test
    public void showFromAllOrCurrent() throws IOException {
        HtmlRadioButtonInput showFromAllBlogsButton = mainPage.getHtmlElementById("showAllBlogsRadio");
        assertThat(showFromAllBlogsButton.isChecked()).isTrue();
        HtmlRadioButtonInput showFromCurrentButton = mainPage.getHtmlElementById("showDefaultBlogRadio");
        assertThat(showFromCurrentButton.isChecked()).isFalse();

        showFromCurrentButton.click();
        waitForScript();

        mainPage = webClient.getPage(baseUri() + "/hashtagViewer");
        waitForScript();
        showFromAllBlogsButton = mainPage.getHtmlElementById("showAllBlogsRadio");
        assertThat(showFromAllBlogsButton.isChecked()).isFalse();
        showFromCurrentButton = mainPage.getHtmlElementById("showDefaultBlogRadio");
        assertThat(showFromCurrentButton.isChecked()).isTrue();

        tagsTable = mainPage.getHtmlElementById("tagsTable");
        assertThat(tagsTable.getRowCount()).isEqualTo(17);

        HtmlTableCell cell = tagsTable.getCellAt(9, 1);
        assertThat(cell.asText()).isEqualToNormalizingWhitespace(MAIN_BLOG_NAME);
    }

    /**
     * Tests creating a new HT
     */
    @Test
    public void createHT() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        HtmlTextInput newTagBox = mainPage.getHtmlElementById("newTagTextBox");
        HtmlRadioButtonInput showFromCurrentButton = mainPage.getHtmlElementById("showDefaultBlogRadio");
        newTagBox.focus();
        newTagBox.setText("aNewTag");
        mainPage.setFocusedElement(showFromCurrentButton);
        waitForScript();

        Hashtag[] hashtags = getAllHashtags();
        assertThat(hashtags.length).isEqualTo(17);

        mainPage = webClient.getPage(baseUri() + "/hashtagViewer");
        waitForScript();
        tagsTable = mainPage.getHtmlElementById("tagsTable");
        assertThat(tagsTable.getRowCount()).isEqualTo(19);
        
        HtmlTableCell cell = tagsTable.getCellAt(3, 1);
        assertThat(cell.asText()).isEmpty();
    }

    /**
     * Tests removal of a Hashtag
     */
    @Test
    public void removeHT() throws IOException {
        HtmlTableCell cell = tagsTable.getCellAt(2, 3);
        HtmlButton removeButton = (HtmlButton) cell.getFirstElementChild();
        mainPage = removeButton.click();
        waitForScript();

        Hashtag[] hashtags = getAllHashtags();
        assertThat(hashtags.length).isEqualTo(15);

        mainPage = webClient.getPage(baseUri() + "/hashtagViewer");
        waitForScript();

        tagsTable = mainPage.getHtmlElementById("tagsTable");
        assertThat(tagsTable.getRowCount()).isEqualTo(17);
    }
}
