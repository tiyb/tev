package com.tiyb.tev.html;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.tiyb.tev.datamodel.Hashtag;

public class HashtagsHtmlTests extends HtmlTestingClass {

    private HtmlTable tagsTable;
    
    private Logger logger = LoggerFactory.getLogger(HashtagsHtmlTests.class);
    
    @Value("${htviewer_table_removeBtn}")
    private String removeButtonText;

    @Before
    public void setupSite() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        restInitDataForMainBlog(Optional.empty());
        restInitDataForSecondBlog();

        mainPage = webClient.getPage(baseUri() + "/hashtagViewer");
        waitForScript();
        tagsTable = mainPage.getHtmlElementById("tagsTable");
    }

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
        
        cell = tagsTable.getCellAt(10, 1);
        assertThat(cell.asText()).isEqualToNormalizingWhitespace("mainblog, secondblog");
        
        cell = tagsTable.getCellAt(10, 2);
        assertThat(cell.asText()).isEqualToNormalizingWhitespace("5");
        
        cell = tagsTable.getCellAt(10, 3);
        assertThat(cell.asText()).isEmpty();
        
        cell = tagsTable.getCellAt(3, 2);
        assertThat(cell.asText()).isEqualToNormalizingWhitespace("4");
        
    }
    
    @Test
    public void showFromAllOrCurrent() {
        HtmlRadioButtonInput showFromAllBlogsButton = mainPage.getHtmlElementById("showAllBlogsRadio");
        assertThat(showFromAllBlogsButton.isChecked()).isTrue();
        HtmlRadioButtonInput showFromCurrentButton = mainPage.getHtmlElementById("showDefaultBlogRadio");
        assertThat(showFromCurrentButton.isChecked()).isFalse();
    }
    
//    @Test
//    public void tagInMultipleBlogs() {
//        // remove button shouldn't show if a tag is in multiple blogs
//        assertThat(true).isFalse();
//    }
//
//    @Test
//    public void createHT() {
//        assertThat(true).isEqualTo(false);
//    }
//
//    @Test
//    public void removeHT() {
//        assertThat(true).isEqualTo(false);
//    }
}
