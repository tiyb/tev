package com.tiyb.tev.html;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.gargoylesoftware.htmlunit.WebClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BlogSettingPageHtmlTests {
    @LocalServerPort
    private int serverPort;

    private WebClient webClient;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setupSite() {
        webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.addRequestHeader("Accept-Language", "en");

        HtmlTestingHelpers.restInitDataForMainBlog(restTemplate, serverPort, Optional.empty());
    }

    @After
    public void close() {
        webClient.close();
    }

    @Test
    public void changeBlogName() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void deleteBlog() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void setAsDefaultBlog() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void changeBaseMediaPath() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void changeFilter() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void changeSortOrder() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void changeShowFavourites() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void changeNumItemsToDisplay() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void changeShowReadingPane() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void changeOverwritePosts() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void changePostImageExportPath() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void changeTheme() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void changeConversationDisplayStyle() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void changeConvoSortBy() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void changeConvoSortOrder() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void changeOverwriteConvos() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void uploadPostXml() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void uploadConvoXml() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void markAllPostsRead() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void markAllPostsUnread() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void cleanUpImages() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void importImages() {
        assertThat(true).isEqualTo(false);
    }
}
