package com.tiyb.tev.html;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlHeading1;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.javascript.host.event.KeyboardEvent;
import com.tiyb.tev.TevTestingHelpers;
import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.Post;

public class IndexHtmlTests extends HtmlTestingClass {

    Logger logger = LoggerFactory.getLogger(IndexHtmlTests.class);
    HtmlTable postTable;

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

    @Value("${viewer_buttons_favourite}")
    private String markFavBtnText;

    @Value("${viewer_buttons_unfavourite}")
    private String markNonFavBtnText;

    @Value("${postviewers.markunreadbutton}")
    private String markUnreadButtonText;

    @Value("${viewer_buttons_markfordownload}")
    private String stagePostButtonText;

    @Value("${viewer_buttons_unmarkfordownload}")
    private String unstagePostButtonText;

    private static final int NUM_ITEMS_IN_TABLE = 12; // 9 rows of data, 1 header, 2 footer
    private static final String FIRST_POST_ID = "778563537472";
    private static final int FIRST_POST_ROW_NO = 3;

    private static final int COLUMN_ID = 0;
    private static final int COLUMN_TYPE = 1;
    private static final int COLUMN_STATE = 2;
    private static final int COLUMN_SLUG = 3;
    private static final int COLUMN_HASHTAG = 4;
    private static final int COLUMN_DATE = 5;
    private static final int COLUMN_FAV = 6;
    private static final int COLUMN_READ = 7;

    @Before
    public void setupWC() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        restInitDataForMainBlog(Optional.empty());
        restInitAdditionalBlog(TevTestingHelpers.SECOND_BLOG_NAME);

        mainPage = webClient.getPage(baseUri());
        waitForScript();

        postTable = mainPage.getHtmlElementById("postTable");
    }

    @Test
    public void loadApplication() {
        assertThat(mainPage.getTitleText()).isEqualTo(this.indexPageTitle);

        assertThat(postTable.getRowCount()).isEqualTo(NUM_ITEMS_IN_TABLE);

        // not checking every cell, but a random selection
        HtmlTableCell cell = postTable.getCellAt(FIRST_POST_ROW_NO, COLUMN_ID);
        assertThat(cell.asText()).isEqualToIgnoringWhitespace(FIRST_POST_ID);

        cell = postTable.getCellAt(4, COLUMN_TYPE);
        assertThat(cell.asText()).isEqualToIgnoringWhitespace("Regular");

        cell = postTable.getCellAt(5, COLUMN_STATE);
        assertThat(cell.asText()).isEqualToIgnoringWhitespace("draft");

        cell = postTable.getCellAt(6, COLUMN_SLUG);
        assertThat(cell.asText()).isEqualToIgnoringWhitespace("first post");

        cell = postTable.getCellAt(7, COLUMN_HASHTAG);
        assertThat(cell.asText()).isEqualToIgnoringWhitespace("tag3, tag4");

        cell = postTable.getCellAt(8, COLUMN_DATE);
        assertThat(cell.asText()).isEqualToIgnoringWhitespace("2018-12-05 02:10:22");

        // all posts should be unread, and not favourited
        for (int i = FIRST_POST_ROW_NO; i < 11; i++) {
            cell = postTable.getCellAt(i, COLUMN_FAV);
            assertThat(cell.asText()).isEqualToIgnoringWhitespace(nonFavouriteText);
            cell = postTable.getCellAt(i, COLUMN_READ);
            assertThat(cell.asText()).isEqualToIgnoringWhitespace(nonReadText);
        }
    }

    /**
     * Tests opening a "Regular" post, verifies that the Fav/Non-Fav button works,
     * then verifies that the "mark unread" button works
     * 
     * @throws IOException
     */
    @Test
    public void openPost() throws IOException {
        // click element in table on main page, and get the resultant popup page
        HtmlPage newPage = openPopup();

        // verify that the text on the page matches that of the post that was clicked
        DomNodeList<DomNode> h1Fields = newPage.querySelectorAll("h1");
        assertThat(h1Fields.size()).isEqualTo(1);
        HtmlHeading1 h1 = (HtmlHeading1) h1Fields.get(0);
        assertThat(h1.asText()).isEqualTo("Queued Post");
        DomNodeList<DomNode> divs = newPage.querySelectorAll("div#regularContentContainer");
        assertThat(divs.size()).isEqualTo(1);
        HtmlDivision contentDiv = (HtmlDivision) divs.get(0);
        assertThat(contentDiv.asText()).isEqualTo("post body text here");

        // verify the favourite button exists and has the right text, as well as that
        // the post in question is not currently favourited
        HtmlButton favButton = newPage.querySelector("button#favouriteButton");
        assertThat(favButton).isNotNull();
        assertThat(favButton.getVisibleText()).isEqualTo(markFavBtnText);

        Post post = restTemplate.getForObject(
                String.format("%s/api/posts/%s/%s", baseUri(), TevTestingHelpers.MAIN_BLOG_NAME, FIRST_POST_ID),
                Post.class);
        assertThat(post.getIsFavourite()).isFalse();

        // click the favourite button
        newPage = favButton.click(false, false, false, true);
        waitForScript();
        assertThat(getNumRealWindows()).isEqualTo(2);
        newPage = (HtmlPage) newPage.refresh();
        newPage.initialize();
        waitForScript();
        favButton = newPage.querySelector("button#favouriteButton");
        assertThat(favButton).isNotNull();

        post = restTemplate.getForObject(
                String.format("%s/api/posts/%s/%s", baseUri(), TevTestingHelpers.MAIN_BLOG_NAME, FIRST_POST_ID),
                Post.class);
        assertThat(post.getIsFavourite()).isTrue();
        assertThat(favButton.getVisibleText()).isEqualToNormalizingWhitespace(markNonFavBtnText);

        // un-favourite the post
        newPage = favButton.click(false, false, false, true);
        waitForScript();
        assertThat(getNumRealWindows()).isEqualTo(2);
        newPage = (HtmlPage) newPage.refresh();
        newPage.initialize();
        waitForScript();
        favButton = newPage.querySelector("button#favouriteButton");
        assertThat(favButton).isNotNull();
        post = restTemplate.getForObject(
                String.format("%s/api/posts/%s/%s", baseUri(), TevTestingHelpers.MAIN_BLOG_NAME, FIRST_POST_ID),
                Post.class);
        assertThat(post.getIsFavourite()).isFalse();
        assertThat(favButton.asText()).isEqualTo(markFavBtnText);

        // verify that the staging button exists and has the right text, and that there
        // are no staged posts
        HtmlButton stagingBtn = newPage.querySelector("button#stageForDownloadButton");
        assertThat(stagingBtn).isNotNull();
        assertThat(stagingBtn.asText()).isEqualTo(stagePostButtonText);

        ResponseEntity<String[]> responseEntity = restTemplate.getForEntity(
                String.format("%s/staging-api/posts/%s", baseUri(), TevTestingHelpers.MAIN_BLOG_NAME), String[].class);
        String[] stagedPosts = responseEntity.getBody();
        assertThat(stagedPosts).isEmpty();

        // click the staging button, and verify that the post is staged
        newPage = stagingBtn.click(false, false, false, true);
        waitForScript();
        newPage = (HtmlPage) newPage.refresh();
        newPage.initialize();
        waitForScript();
        stagingBtn = newPage.querySelector("button#stageForDownloadButton");
        assertThat(stagingBtn.getVisibleText()).isEqualToNormalizingWhitespace(unstagePostButtonText);
        responseEntity = restTemplate.getForEntity(
                String.format("%s/staging-api/posts/%s", baseUri(), TevTestingHelpers.MAIN_BLOG_NAME), String[].class);
        stagedPosts = responseEntity.getBody();
        assertThat(stagedPosts.length).isEqualTo(1);
        assertThat(stagedPosts[0]).isEqualToNormalizingWhitespace(FIRST_POST_ID);
    }

    /**
     * Tests changes to the various reading-related radio buttons.
     * 
     * <ol>
     * <li>Clicks the first post, to mark it read, then marks it as a favourite</li>
     * <li>Verifies that the fields are hidden, then clicks the "show additional
     * options" link to show them, then verifies that they're shown</li>
     * <li>Clicks each option for read/unread, verifies that the server-side setting
     * was changed, and verifies that it has the appropriate</li>
     * <li>Does the same for favourite/nonfavourite options</li>
     * <li>Checks initial state of reading/popup options, and that the reading pane
     * is currently hidden</li>
     * <li>Selects show reading pane; verifies that MD was changed, clicks a post,
     * verifies that the reading pane UI is displayed</li>
     * <li>Selects popups, verifies that MD was changed, verifies that reading pane
     * is now hidden; clicks a post and verifies that reading pane is still
     * hidden</li>
     * </ol>
     * 
     * @throws IOException
     */
    @Test
    public void toggleViewingButtons() throws IOException {
        postTable.getCellAt(FIRST_POST_ROW_NO, COLUMN_ID).getFirstElementChild().click();
        postTable.getCellAt(FIRST_POST_ROW_NO, COLUMN_FAV).getFirstElementChild().click();

        assertThat(postTable.getCellAt(FIRST_POST_ROW_NO, COLUMN_FAV).asText())
                .isEqualToIgnoringWhitespace(favouriteText);

        HtmlDivision additionalOptionsDiv = mainPage.getHtmlElementById("additionalOptionsTable");
        assertThat(additionalOptionsDiv.isDisplayed()).isFalse();
        HtmlAnchor showOptionsLink = mainPage.getHtmlElementById("additionalOptionsLink");
        showOptionsLink.click();
        assertThat(additionalOptionsDiv.isDisplayed()).isTrue();

        HtmlRadioButtonInput filterUnreadButton = mainPage.getHtmlElementById("filterUnread");
        assertThat(filterUnreadButton.isChecked()).isFalse();
        HtmlRadioButtonInput filterReadButton = mainPage.getHtmlElementById("filterRead");
        assertThat(filterReadButton.isChecked()).isFalse();
        HtmlRadioButtonInput filterNoneButton = mainPage.getHtmlElementById("filterNoValues");
        assertThat(filterNoneButton.isChecked()).isTrue();

        filterUnreadButton.click();
        waitForScript();
        assertThat(postTable.getRowCount()).isEqualTo(NUM_ITEMS_IN_TABLE - 8);
        assertThat(filterUnreadButton.isChecked()).isTrue();
        assertThat(filterReadButton.isChecked()).isFalse();
        assertThat(filterNoneButton.isChecked()).isFalse();

        Metadata md = getMDFromServer(Optional.of(TevTestingHelpers.MAIN_BLOG_NAME));
        assertThat(md.getFilter()).isEqualTo("Filter Unread Posts");

        filterReadButton.click();
        waitForScript();
        assertThat(postTable.getRowCount()).isEqualTo(NUM_ITEMS_IN_TABLE - 1);
        assertThat(filterUnreadButton.isChecked()).isFalse();
        assertThat(filterReadButton.isChecked()).isTrue();
        assertThat(filterNoneButton.isChecked()).isFalse();

        md = getMDFromServer(Optional.of(TevTestingHelpers.MAIN_BLOG_NAME));
        assertThat(md.getFilter()).isEqualTo("Filter Read Posts");

        filterNoneButton.click();
        waitForScript();
        assertThat(postTable.getRowCount()).isEqualTo(NUM_ITEMS_IN_TABLE);
        assertThat(filterUnreadButton.isChecked()).isFalse();
        assertThat(filterReadButton.isChecked()).isFalse();
        assertThat(filterNoneButton.isChecked()).isTrue();

        md = getMDFromServer(Optional.of(TevTestingHelpers.MAIN_BLOG_NAME));
        assertThat(md.getFilter()).isEqualTo("Do not Filter");

        HtmlRadioButtonInput showFavsBtn = mainPage.getHtmlElementById("showFavourites");
        assertThat(showFavsBtn.isChecked()).isFalse();
        HtmlRadioButtonInput hideFavsBtn = mainPage.getHtmlElementById("showNonFavourites");
        assertThat(hideFavsBtn.isChecked()).isFalse();
        HtmlRadioButtonInput showAllBtn = mainPage.getHtmlElementById("showAll");
        assertThat(showAllBtn.isChecked()).isTrue();

        showFavsBtn.click();
        waitForScript();
        assertThat(postTable.getRowCount()).isEqualTo(NUM_ITEMS_IN_TABLE - 8);
        assertThat(showFavsBtn.isChecked()).isTrue();
        assertThat(hideFavsBtn.isChecked()).isFalse();
        assertThat(showAllBtn.isChecked()).isFalse();

        md = getMDFromServer(Optional.of(TevTestingHelpers.MAIN_BLOG_NAME));
        assertThat(md.getFavFilter()).isEqualTo("Show Favourites");

        hideFavsBtn.click();
        waitForScript();
        assertThat(postTable.getRowCount()).isEqualTo(NUM_ITEMS_IN_TABLE - 1);
        assertThat(showFavsBtn.isChecked()).isFalse();
        assertThat(hideFavsBtn.isChecked()).isTrue();
        assertThat(showAllBtn.isChecked()).isFalse();

        md = getMDFromServer(Optional.of(TevTestingHelpers.MAIN_BLOG_NAME));
        assertThat(md.getFavFilter()).isEqualTo("Show Non Favourites");

        showAllBtn.click();
        waitForScript();
        assertThat(postTable.getRowCount()).isEqualTo(NUM_ITEMS_IN_TABLE);
        assertThat(showFavsBtn.isChecked()).isFalse();
        assertThat(hideFavsBtn.isChecked()).isFalse();
        assertThat(showAllBtn.isChecked()).isTrue();

        md = getMDFromServer(Optional.of(TevTestingHelpers.MAIN_BLOG_NAME));
        assertThat(md.getFavFilter()).isEqualTo("Show Everything");

        HtmlRadioButtonInput showReadingPaneBtn = mainPage.getHtmlElementById("showReadingPaneSelected");
        assertThat(showReadingPaneBtn.isChecked()).isFalse();
        HtmlRadioButtonInput showPopups = mainPage.getHtmlElementById("showPopupsSelected");
        assertThat(showPopups.isChecked()).isTrue();
        HtmlTableCell readingPaneCell = mainPage.getHtmlElementById("contentDisplayReadingPane");
        assertThat(readingPaneCell.isDisplayed()).isFalse();

        showReadingPaneBtn.click();
        waitForScript();
        postTable.getCellAt(FIRST_POST_ROW_NO, COLUMN_ID).getFirstElementChild().click();
        assertThat(readingPaneCell.isDisplayed()).isTrue();
        List<FrameWindow> frames = mainPage.getFrames();
        String contents = frames.get(0).getEnclosedPage().getWebResponse().getContentAsString();
        assertThat(contents).contains("<h1>Queued Post</h1>");
        assertThat(contents).contains("<div id=\"regularContentContainer\">post body text here</div>");

        showPopups.click();
        waitForScript();
        assertThat(readingPaneCell.isDisplayed()).isFalse();
        postTable.getCellAt(FIRST_POST_ROW_NO, COLUMN_ID).getFirstElementChild().click();
        assertThat(readingPaneCell.isDisplayed()).isFalse();

        showOptionsLink.click();
        assertThat(additionalOptionsDiv.isDisplayed()).isFalse();
    }

    @Test
    public void markPostUnreadFromTable() throws IOException {
        postTable.getCellAt(FIRST_POST_ROW_NO, COLUMN_ID).getFirstElementChild().click();
        waitForScript();
        assertThat(postTable.getCellAt(FIRST_POST_ROW_NO, COLUMN_READ).asText()).isEqualToIgnoringWhitespace(readText);

        Post postFromServer = restTemplate.getForObject(
                String.format("%s/api/posts/%s/%s", baseUri(), TevTestingHelpers.MAIN_BLOG_NAME, FIRST_POST_ID),
                Post.class);
        assertThat(postFromServer.getIsRead()).isTrue();

        postTable.getCellAt(FIRST_POST_ROW_NO, COLUMN_READ).getFirstElementChild().click();
        waitForScript();
        assertThat(postTable.getCellAt(FIRST_POST_ROW_NO, COLUMN_READ).asText())
                .isEqualToIgnoringWhitespace(nonReadText);
        postFromServer = restTemplate.getForObject(
                String.format("%s/api/posts/%s/%s", baseUri(), TevTestingHelpers.MAIN_BLOG_NAME, FIRST_POST_ID),
                Post.class);
        assertThat(postFromServer.getIsRead()).isFalse();
    }

    @Test
    public void changeBlogDropdown() throws IOException {
        HtmlSpan blogSelector = mainPage.getHtmlElementById("headerBlogSelect-button");
        mainPage = blogSelector.click();
        waitForScript();

        HtmlSelect blogSwitcher = mainPage.getHtmlElementById("headerBlogSelect");
        HtmlOption secondBlogOption = blogSwitcher.getOptionByText(TevTestingHelpers.SECOND_BLOG_NAME);
        mainPage = blogSwitcher.setSelectedAttribute(secondBlogOption, true);
        mainPage.executeJavaScript("changeViewedBlog();");
//        String jsScript = String.format("$('#headerBlogSelect').val('%s').selectmenu('refresh').selectmenu('change');",
//                TevTestingHelpers.SECOND_BLOG_NAME);
//        mainPage.executeJavaScript(jsScript);

        waitForScript();
        String url = mainPage.getUrl().toString();

        HtmlPage newPage = (HtmlPage) mainPage.getEnclosingWindow().getEnclosedPage();
        URL newUrl = newPage.getUrl();
        String secondUrl = newPage.getUrl().toString();
        assertThat(true).isFalse();

//        HtmlButton tempBtn = mainPage.getHtmlElementById("tempHiButton");
//        mainPage = tempBtn.click();
//        webClient.waitForBackgroundJavaScript(WAIT_TIME_FOR_JS);
//        //HtmlPage newPage = (HtmlPage)mainPage.getEnclosingWindow().getEnclosedPage();
//        String url = mainPage.getUrl().toString();
//        assertThat(true).isTrue();

//        DomNodeList<DomNode> blogOptions = mainPage.querySelectorAll("li.ui-menu-item");
//        for (DomNode node : blogOptions) {
//            String blogName = node.asText();
//            if (TevTestingHelpers.SECOND_BLOG_NAME.equals(blogName)) {
//                HtmlDivision div = (HtmlDivision) node.getFirstChild();
//                mainPage = div.click(false, false, false, true);
//                webClient.waitForBackgroundJavaScript(WAIT_TIME_FOR_JS);
//                HtmlPage newPage = (HtmlPage)mainPage.getEnclosingWindow().getEnclosedPage();
//                URL theURL = newPage.getUrl();
//                String url = theURL.toString();
//                // TODO window.location changes not supported by HTMLUnit?
//                assertThat(true).isFalse();
//                break;
//            }
//        }
    }

    @Test
    public void escButton() throws IOException {
        HtmlPage newPage = openPopup();

        // press ESC, and verify that the window closed
        newPage.getDocumentElement().type(KeyboardEvent.DOM_VK_ESCAPE);
        assertThat(getNumRealWindows()).isEqualTo(1);
    }

    private HtmlPage openPopup() throws IOException {
        HtmlPage newPage = postTable.getCellAt(FIRST_POST_ROW_NO, COLUMN_ID).getFirstElementChild().click();
        waitForScript();
        assertThat(getNumRealWindows()).isEqualTo(2);

        return newPage;
    }

    @Test
    public void closeButtons() throws IOException {
        HtmlPage newPage = openPopup();

        // click the close button; verify the post is still read
        HtmlButton closeButton = newPage.querySelector("button#closeButton");
        closeButton.click();
        assertThat(getNumRealWindows()).isEqualTo(1);
        Post post = restTemplate.getForObject(
                String.format("%s/api/posts/%s/%s", baseUri(), TevTestingHelpers.MAIN_BLOG_NAME, FIRST_POST_ID),
                Post.class);
        assertThat(post.getIsRead()).isTrue();
        mainPage = (HtmlPage) mainPage.refresh();
        waitForScript();
        postTable = mainPage.getHtmlElementById("postTable");
        HtmlTableCell cell = postTable.getCellAt(FIRST_POST_ROW_NO, COLUMN_READ);
        assertThat(cell.asText()).isEqualToNormalizingWhitespace(readText);

        newPage = openPopup();

        // click the mark unread button, verify the post is unread, and that the window
        // is closed
        HtmlButton markUnreadBtn = newPage.querySelector("button#markReadButton");
        markUnreadBtn.click();
        waitForScript();
        assertThat(getNumRealWindows()).isEqualTo(1);
        post = restTemplate.getForObject(
                String.format("%s/api/posts/%s/%s", baseUri(), TevTestingHelpers.MAIN_BLOG_NAME, FIRST_POST_ID),
                Post.class);
        assertThat(post.getIsRead()).isFalse();
        mainPage = (HtmlPage) mainPage.refresh();
        waitForScript();
        postTable = mainPage.getHtmlElementById("postTable");
        cell = postTable.getCellAt(FIRST_POST_ROW_NO, COLUMN_READ);
        assertThat(cell.asText()).isEqualToNormalizingWhitespace(nonReadText);

        // filter out read posts
        HtmlRadioButtonInput filterReadButton = mainPage.getHtmlElementById("filterRead");
        filterReadButton.click();
        waitForScript();
        assertThat(postTable.getRowCount()).isEqualTo(NUM_ITEMS_IN_TABLE);
        Metadata md = getMDFromServer(Optional.of(TevTestingHelpers.MAIN_BLOG_NAME));
        assertThat(md.getFilter()).isEqualTo("Filter Read Posts");

        // click element in the table on the main page, and get the resultant popup
        newPage = openPopup();
        post = restTemplate.getForObject(
                String.format("%s/api/posts/%s/%s", baseUri(), TevTestingHelpers.MAIN_BLOG_NAME, FIRST_POST_ID),
                Post.class);
        assertThat(post.getIsRead()).isTrue();

        HtmlButton closeAndRefreshBtn = newPage.querySelector("button#closeAndRefreshButton");
        assertThat(closeAndRefreshBtn).isNotNull();
        closeAndRefreshBtn.click();
        waitForScript();
        // TODO location.reload() not handled by HTMLUnit
        assertThat(getNumRealWindows()).isEqualTo(1);
        mainPage = webClient.getPage(baseUri());
        waitForScript();
        postTable = mainPage.getHtmlElementById("postTable");
        assertThat(postTable.getRowCount()).isEqualTo(NUM_ITEMS_IN_TABLE - 1);
    }

    @Test
    public void testSearchParam() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        HtmlPage filteredPage = webClient.getPage(baseUri() + "?hashsearch=tag1");
        waitForScript();
        HtmlTable filteredPostTable = filteredPage.getHtmlElementById("postTable");
        assertThat(filteredPostTable.getRowCount()).isEqualTo(NUM_ITEMS_IN_TABLE - 3); // three posts don't have this
                                                                                       // hashtag
    }

    @Test
    public void testClickingHashtagInViewer() throws IOException {
        HtmlPage popup = openPopup();
        waitForScript();
        
        DomNodeList<DomNode> htSpans = popup.querySelectorAll("span.hashtagspan");
        for(DomNode n : htSpans) {
            if("tag1".equals(n.getVisibleText())) {
                HtmlSpan nAsSpan = (HtmlSpan)n;
                nAsSpan.click();
                waitForScript();
            }
        }
        
        assertThat(getNumRealWindows()).isEqualTo(1);
        List<WebWindow> windows = webClient.getWebWindows();
        boolean pageRefreshed = false;
        HtmlPage newMainPage = null;
        for(WebWindow win : windows) {
            String url = win.getEnclosedPage().getUrl().toString();
            if(url.contains("localhost")) {
                assertThat(url).contains("hashsearch");
                pageRefreshed = true;
                newMainPage = (HtmlPage)win.getEnclosedPage();
            }
        }
        assertThat(pageRefreshed).isTrue();
        assertThat(newMainPage).isNotNull();
        HtmlTable newPostTable = newMainPage.getHtmlElementById("postTable");
        assertThat(newPostTable.getRowCount()).isEqualTo(NUM_ITEMS_IN_TABLE - 3);
    }

}
