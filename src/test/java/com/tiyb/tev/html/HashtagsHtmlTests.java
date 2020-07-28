package com.tiyb.tev.html;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.tiyb.tev.datamodel.Hashtag;

public class HashtagsHtmlTests extends HtmlTestingClass {

    private HtmlTable tagsTable;
    
    private Logger logger = LoggerFactory.getLogger(HashtagsHtmlTests.class);

    @Before
    public void setupSite() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        restInitDataForMainBlog(Optional.empty());
        restInitDataForSecondBlog();

        mainPage = webClient.getPage(baseUri() + "/hashtagViewer");
        waitForScript();
        tagsTable = mainPage.getHtmlElementById("tagsTable");
    }

    @Test
    public void showFromAllOrCurrent() {
        HtmlRadioButtonInput showFromAllBlogsButton = mainPage.getHtmlElementById("showAllBlogsRadio");
        assertThat(showFromAllBlogsButton.isChecked()).isTrue();
        HtmlRadioButtonInput showFromCurrentButton = mainPage.getHtmlElementById("showDefaultBlogRadio");
        assertThat(showFromCurrentButton.isChecked()).isFalse();

        Hashtag[] hashtags = getAllHashtags();
        assertThat(hashtags.length).isEqualTo(16);
        
        logger.info(mainPage.asXml());

        assertThat(tagsTable.getRowCount()).isEqualTo(18);
    }

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
