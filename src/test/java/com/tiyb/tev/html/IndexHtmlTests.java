package com.tiyb.tev.html;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
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

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class IndexHtmlTests {

    private WebClient webClient;
    private HtmlPage page;

    @LocalServerPort
    private int serverPort;

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${main.title}")
    private String indexPageTitle;

    @Before
    public void setupWC() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.addRequestHeader("Accept-Language", "en");

        HtmlTestingHelpers.restInitDataForMainBlog(restTemplate, serverPort, Optional.empty());

        page = webClient.getPage(HtmlTestingHelpers.baseUri(this.serverPort));

        webClient.waitForBackgroundJavaScript(60000);
    }

    @After
    public void close() {
        webClient.close();
    }

    @Test
    public void loadApplication() {
        assertThat(page.getTitleText()).isEqualTo(this.indexPageTitle);

        HtmlTable postTable = page.getHtmlElementById("postTable");
        assertThat(postTable.getRowCount()).isEqualTo(12); // 9 rows of data, 1 header, 2 footer
        
        // not checking every cell, but a random selection
        HtmlTableCell cell = postTable.getCellAt(3, 0);
        assertThat(cell.asText()).isEqualToIgnoringWhitespace("778563537472");
        
        cell = postTable.getCellAt(4, 1);
        assertThat(cell.asText()).isEqualToIgnoringWhitespace("Regular");
        
        cell = postTable.getCellAt(5, 2);
        assertThat(cell.asText()).isEqualToIgnoringWhitespace("draft");
        
        cell = postTable.getCellAt(6, 3);
        assertThat(cell.asText()).isEqualToIgnoringWhitespace("first post");
        
        cell = postTable.getCellAt(7, 4);
        assertThat(cell.asText()).isEqualToIgnoringWhitespace("tag3, tag4");
        
        cell = postTable.getCellAt(8, 5);
        assertThat(cell.asText()).isEqualToIgnoringWhitespace("2018-12-05 02:10:22");
        
        // all posts should be unread, and not favourited
        for(int i = 3; i < 11; i++) {
            cell = postTable.getCellAt(i, 6);
            assertThat(cell.asText()).isEqualToIgnoringWhitespace("Not Favorite");
            cell = postTable.getCellAt(i, 7);
            assertThat(cell.asText()).isEqualToIgnoringWhitespace("Unread");
        }
    }

//    @Test
//    public void showReadPosts() {
//        assertThat(true).isEqualTo(false);
//    }
//
//    @Test
//    public void showUneadPosts() {
//        assertThat(true).isEqualTo(false);
//    }
//
//    @Test
//    public void showReadAndUnreadPosts() {
//        assertThat(true).isEqualTo(false);
//    }
//
//    @Test
//    public void showFavourites() {
//        assertThat(true).isEqualTo(false);
//    }
//
//    @Test
//    public void showNonFavourites() {
//        assertThat(true).isEqualTo(false);
//    }
//
//    @Test
//    public void showFavAndNonFav() {
//        assertThat(true).isEqualTo(false);
//    }
//
//    @Test
//    public void showReadingPane() {
//        assertThat(true).isEqualTo(false);
//    }
//
//    @Test
//    public void showPopups() {
//        assertThat(true).isEqualTo(false);
//    }
//
//    @Test
//    public void markPostFav() {
//        assertThat(true).isEqualTo(false);
//    }
//
//    @Test
//    public void markPostNonFav() {
//        assertThat(true).isEqualTo(false);
//    }
//
//    @Test
//    public void markPostRead() {
//        assertThat(true).isEqualTo(false);
//    }
//
//    @Test
//    public void markPostUnread() {
//        assertThat(true).isEqualTo(false);
//    }
//
//    @Test
//    public void openPost() {
//        assertThat(true).isEqualTo(false);
//    }
//    
//    @Test
//    public void changeBlogHeaderDropdown() {
//        assertThat(true).isEqualTo(false);
//    }
}
