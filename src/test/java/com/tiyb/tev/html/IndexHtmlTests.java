package com.tiyb.tev.html;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.WebWindowListener;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.tiyb.tev.TevTestingHelpers;
import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.Post;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource("classpath:static/js/i18n/messages.properties")
public class IndexHtmlTests {

    private WebClient webClient;
    private HtmlPage page;
    HtmlTable postTable;

    @LocalServerPort
    private int serverPort;

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${main.title}")
    private String indexPageTitle;

    @Value("${index_posttable_isFavouriteCLEAN}")
    private String favouriteText;

    @Value("${index_posttable_isNotFavouriteCLEAN}")
    private String nonFavouriteText;

    @Value("${index_read}")
    private String readText;

    @Value("${index_unread}")
    private String nonReadText;

    private static final int NUM_ITEMS_IN_TABLE = 12; // 9 rows of data, 1 header, 2 footer
    private static final int WAIT_TIME_FOR_JS = 60000;
    private static final String FIRST_POST_ID = "778563537472";

    @Before
    public void setupWC() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.addRequestHeader("Accept-Language", "en");

        HtmlTestingHelpers.restInitDataForMainBlog(restTemplate, serverPort, Optional.empty());

        page = webClient.getPage(HtmlTestingHelpers.baseUri(this.serverPort));

        webClient.waitForBackgroundJavaScript(WAIT_TIME_FOR_JS);
        postTable = page.getHtmlElementById("postTable");
    }

    @After
    public void close() {
        webClient.close();
    }

    @Test
    public void loadApplication() {
        assertThat(page.getTitleText()).isEqualTo(this.indexPageTitle);

        assertThat(postTable.getRowCount()).isEqualTo(NUM_ITEMS_IN_TABLE);

        // not checking every cell, but a random selection
        HtmlTableCell cell = postTable.getCellAt(3, 0);
        assertThat(cell.asText()).isEqualToIgnoringWhitespace(FIRST_POST_ID);

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
        for (int i = 3; i < 11; i++) {
            cell = postTable.getCellAt(i, 6);
            assertThat(cell.asText()).isEqualToIgnoringWhitespace(nonFavouriteText);
            cell = postTable.getCellAt(i, 7);
            assertThat(cell.asText()).isEqualToIgnoringWhitespace(nonReadText);
        }
    }

    @Test
    public void openPost() throws IOException {
        webClient.addWebWindowListener(new WebWindowListener() {

            @Override
            public void webWindowOpened(WebWindowEvent event) {
                HtmlTableCell readCell = postTable.getCellAt(3, 7);
                assertThat(readCell.asText()).isEqualToIgnoringWhitespace(readText);
            }

            @Override
            public void webWindowContentChanged(WebWindowEvent event) {
                String contents = event.getWebWindow().getEnclosedPage().getWebResponse().getContentAsString();
                assertThat(contents).contains("<h1>Queued Post</h1>");
                assertThat(contents).contains("<div id=\"regularContentContainer\">post body text here</div>");
            }

            @Override
            public void webWindowClosed(WebWindowEvent event) {
                // do nothing
            }

        });

        postTable.getCellAt(3, 0).getFirstElementChild().click();
    }

    @Test
    public void toggleViewingButtons() throws IOException {
        postTable.getCellAt(3, 0).getFirstElementChild().click();
        postTable.getCellAt(3, 6).getFirstElementChild().click();

        assertThat(postTable.getCellAt(3, 6).asText()).isEqualToIgnoringWhitespace(favouriteText);

        HtmlDivision additionalOptionsDiv = page.getHtmlElementById("additionalOptionsTable");
        assertThat(additionalOptionsDiv.isDisplayed()).isFalse();
        HtmlAnchor showOptionsLink = page.getHtmlElementById("additionalOptionsLink");
        showOptionsLink.click();
        assertThat(additionalOptionsDiv.isDisplayed()).isTrue();

        HtmlRadioButtonInput filterUnreadButton = page.getHtmlElementById("filterUnread");
        assertThat(filterUnreadButton.isChecked()).isFalse();
        HtmlRadioButtonInput filterReadButton = page.getHtmlElementById("filterRead");
        assertThat(filterReadButton.isChecked()).isFalse();
        HtmlRadioButtonInput filterNoneButton = page.getHtmlElementById("filterNoValues");
        assertThat(filterNoneButton.isChecked()).isTrue();

        filterUnreadButton.click();
        webClient.waitForBackgroundJavaScript(WAIT_TIME_FOR_JS);
        assertThat(postTable.getRowCount()).isEqualTo(NUM_ITEMS_IN_TABLE - 8);
        assertThat(filterUnreadButton.isChecked()).isTrue();
        assertThat(filterReadButton.isChecked()).isFalse();
        assertThat(filterNoneButton.isChecked()).isFalse();

        Metadata md = HtmlTestingHelpers.getMDFromServer(restTemplate, serverPort,
                Optional.of(TevTestingHelpers.MAIN_BLOG_NAME));
        assertThat(md.getFilter()).isEqualTo("Filter Unread Posts");

        filterReadButton.click();
        webClient.waitForBackgroundJavaScript(WAIT_TIME_FOR_JS);
        assertThat(postTable.getRowCount()).isEqualTo(NUM_ITEMS_IN_TABLE - 1);
        assertThat(filterUnreadButton.isChecked()).isFalse();
        assertThat(filterReadButton.isChecked()).isTrue();
        assertThat(filterNoneButton.isChecked()).isFalse();

        md = HtmlTestingHelpers.getMDFromServer(restTemplate, serverPort,
                Optional.of(TevTestingHelpers.MAIN_BLOG_NAME));
        assertThat(md.getFilter()).isEqualTo("Filter Read Posts");

        filterNoneButton.click();
        webClient.waitForBackgroundJavaScript(WAIT_TIME_FOR_JS);
        assertThat(postTable.getRowCount()).isEqualTo(NUM_ITEMS_IN_TABLE);
        assertThat(filterUnreadButton.isChecked()).isFalse();
        assertThat(filterReadButton.isChecked()).isFalse();
        assertThat(filterNoneButton.isChecked()).isTrue();

        md = HtmlTestingHelpers.getMDFromServer(restTemplate, serverPort,
                Optional.of(TevTestingHelpers.MAIN_BLOG_NAME));
        assertThat(md.getFilter()).isEqualTo("Do not Filter");

        HtmlRadioButtonInput showFavsBtn = page.getHtmlElementById("showFavourites");
        assertThat(showFavsBtn.isChecked()).isFalse();
        HtmlRadioButtonInput hideFavsBtn = page.getHtmlElementById("showNonFavourites");
        assertThat(hideFavsBtn.isChecked()).isFalse();
        HtmlRadioButtonInput showAllBtn = page.getHtmlElementById("showAll");
        assertThat(showAllBtn.isChecked()).isTrue();

        showFavsBtn.click();
        webClient.waitForBackgroundJavaScript(WAIT_TIME_FOR_JS);
        assertThat(postTable.getRowCount()).isEqualTo(NUM_ITEMS_IN_TABLE - 8);
        assertThat(showFavsBtn.isChecked()).isTrue();
        assertThat(hideFavsBtn.isChecked()).isFalse();
        assertThat(showAllBtn.isChecked()).isFalse();

        md = HtmlTestingHelpers.getMDFromServer(restTemplate, serverPort,
                Optional.of(TevTestingHelpers.MAIN_BLOG_NAME));
        assertThat(md.getFavFilter()).isEqualTo("Show Favourites");

        hideFavsBtn.click();
        webClient.waitForBackgroundJavaScript(WAIT_TIME_FOR_JS);
        assertThat(postTable.getRowCount()).isEqualTo(NUM_ITEMS_IN_TABLE - 1);
        assertThat(showFavsBtn.isChecked()).isFalse();
        assertThat(hideFavsBtn.isChecked()).isTrue();
        assertThat(showAllBtn.isChecked()).isFalse();

        md = HtmlTestingHelpers.getMDFromServer(restTemplate, serverPort,
                Optional.of(TevTestingHelpers.MAIN_BLOG_NAME));
        assertThat(md.getFavFilter()).isEqualTo("Show Non Favourites");

        showAllBtn.click();
        webClient.waitForBackgroundJavaScript(WAIT_TIME_FOR_JS);
        assertThat(postTable.getRowCount()).isEqualTo(NUM_ITEMS_IN_TABLE);
        assertThat(showFavsBtn.isChecked()).isFalse();
        assertThat(hideFavsBtn.isChecked()).isFalse();
        assertThat(showAllBtn.isChecked()).isTrue();

        md = HtmlTestingHelpers.getMDFromServer(restTemplate, serverPort,
                Optional.of(TevTestingHelpers.MAIN_BLOG_NAME));
        assertThat(md.getFavFilter()).isEqualTo("Show Everything");

        HtmlRadioButtonInput showReadingPaneBtn = page.getHtmlElementById("showReadingPaneSelected");
        assertThat(showReadingPaneBtn.isChecked()).isFalse();
        HtmlRadioButtonInput showPopups = page.getHtmlElementById("showPopupsSelected");
        assertThat(showPopups.isChecked()).isTrue();
        HtmlTableCell readingPaneCell = page.getHtmlElementById("contentDisplayReadingPane");
        assertThat(readingPaneCell.isDisplayed()).isFalse();

        showReadingPaneBtn.click();
        webClient.waitForBackgroundJavaScript(WAIT_TIME_FOR_JS);
        postTable.getCellAt(3, 0).getFirstElementChild().click();
        assertThat(readingPaneCell.isDisplayed()).isTrue();
        List<FrameWindow> frames = page.getFrames();
        String contents = frames.get(0).getEnclosedPage().getWebResponse().getContentAsString();
        assertThat(contents).contains("<h1>Queued Post</h1>");
        assertThat(contents).contains("<div id=\"regularContentContainer\">post body text here</div>");

        showPopups.click();
        webClient.waitForBackgroundJavaScript(WAIT_TIME_FOR_JS);
        assertThat(readingPaneCell.isDisplayed()).isFalse();
        postTable.getCellAt(3, 0).getFirstElementChild().click();
        assertThat(readingPaneCell.isDisplayed()).isFalse();

        showOptionsLink.click();
        assertThat(additionalOptionsDiv.isDisplayed()).isFalse();
    }

    @Test
    public void markPostUnread() throws IOException {
        postTable.getCellAt(3, 0).getFirstElementChild().click();
        webClient.waitForBackgroundJavaScript(WAIT_TIME_FOR_JS);
        assertThat(postTable.getCellAt(3, 7).asText()).isEqualToIgnoringWhitespace(readText);

        Post postFromServer = restTemplate.getForObject(HtmlTestingHelpers.baseUri(serverPort) + "/api/posts/"
                + TevTestingHelpers.MAIN_BLOG_NAME + "/" + FIRST_POST_ID, Post.class);
        assertThat(postFromServer.getIsRead()).isTrue();

        postTable.getCellAt(3, 7).getFirstElementChild().click();
        webClient.waitForBackgroundJavaScript(WAIT_TIME_FOR_JS);
        assertThat(postTable.getCellAt(3, 7).asText()).isEqualToIgnoringWhitespace(nonReadText);
        postFromServer = restTemplate.getForObject(HtmlTestingHelpers.baseUri(serverPort) + "/api/posts/"
                + TevTestingHelpers.MAIN_BLOG_NAME + "/" + FIRST_POST_ID, Post.class);
        assertThat(postFromServer.getIsRead()).isFalse();
    }

}
