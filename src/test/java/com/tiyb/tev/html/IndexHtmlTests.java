package com.tiyb.tev.html;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class IndexHtmlTests {

    private WebClient webClient;

    @LocalServerPort
    private int serverPort;

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${main.title}")
    private String indexPageTitle;

    @Before
    public void setupWC() {
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
    public void loadApplication() throws Exception {
        HtmlPage page = webClient.getPage(HtmlTestingHelpers.baseUri(this.serverPort));

        assertThat(page.getTitleText()).isEqualTo(this.indexPageTitle);
    }

    @Test
    public void showReadPosts() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void showUneadPosts() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void showReadAndUnreadPosts() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void showFavourites() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void showNonFavourites() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void showFavAndNonFav() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void showReadingPane() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void showPopups() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void markPostFav() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void markPostNonFav() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void markPostRead() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void markPostUnread() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void openPost() {
        assertThat(true).isEqualTo(false);
    }
    
    @Test
    public void changeBlogHeaderDropdown() {
        assertThat(true).isEqualTo(false);
    }
}
