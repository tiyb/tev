package com.tiyb.tev.html;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;

public class StagedHtmlTests extends HtmlTestingClass {

    private static final String STAGED_POSTS_TABLE = "stagedPostsTable";

    private static final String FIRST_POST_ID = "778563537472";
    private static final String SECOND_POST_ID = "190894436671";
    private static final String VIDEO_POST_ID = "180782992914";
    private static final String PHOTO_POST_ID = "180784644740";

    @Value("${staging_posttable_emptytable}")
    private String noPostsInTableMessage;
    @Value("${staging_downloadImagesButtonText}")
    private String downloadButtonText;

    /**
     * Sets up main and secondary blogs' data
     */
    @Before
    public void setupSite() {
        restInitDataForMainBlog(Optional.empty());
        restInitDataForSecondBlog();
    }

    /**
     * Stages a post, then verifies that it shows up properly on the staged posts
     * page. Stages a second post, and verifies that both are now showing on the
     * staged posts page.
     */
    @Test
    public void stagePosts() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        // open the main page, open a post, stage it
        stageAPostViaTheUI(FIRST_POST_ID);

        String[] stagedPosts = getStagedPostsForBlog(MAIN_BLOG_NAME);
        assertThat(stagedPosts.length).isEqualTo(1);

        // open the staged posts page and verify that it shows up
        HtmlPage stagedPostsPage = getStagedPostsPage();
        HtmlTable stagedPostsTable = stagedPostsPage.getHtmlElementById(STAGED_POSTS_TABLE);
        assertThat(stagedPostsTable.getRowCount()).isEqualTo(3);
        HtmlTableCell idCell = stagedPostsTable.getCellAt(2, 0);
        assertThat(idCell.asText()).isEqualToNormalizingWhitespace(FIRST_POST_ID);

        // stage another one
        stageAPostViaTheUI(SECOND_POST_ID);

        stagedPosts = getStagedPostsForBlog(MAIN_BLOG_NAME);
        assertThat(stagedPosts.length).isEqualTo(2);

        stagedPostsPage = getStagedPostsPage();
        stagedPostsTable = stagedPostsPage.getHtmlElementById(STAGED_POSTS_TABLE);
        assertThat(stagedPostsTable.getRowCount()).isEqualTo(4);
    }

    /**
     * Stage a couple of posts, then remove one, and verify that it's removed
     */
    @Test
    public void removeAStagedPost() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        stageAPostViaTheUI(FIRST_POST_ID);
        stageAPostViaTheUI(SECOND_POST_ID);

        HtmlPage stagedPostsPage = getStagedPostsPage();
        HtmlTable stagedPostsTable = stagedPostsPage.getHtmlElementById(STAGED_POSTS_TABLE);
        assertThat(stagedPostsTable.getRowCount()).isEqualTo(4);
        HtmlTableCell firstPostButtonCell = stagedPostsTable.getCellAt(2, 4);
        HtmlButton removeButton = (HtmlButton) firstPostButtonCell.getFirstElementChild();
        removeButton.click();
        waitForScript();

        String[] stagedPosts = getStagedPostsForBlog(MAIN_BLOG_NAME);
        assertThat(stagedPosts.length).isEqualTo(1);

        stagedPostsPage = getStagedPostsPage();
        stagedPostsTable = stagedPostsPage.getHtmlElementById(STAGED_POSTS_TABLE);
        assertThat(stagedPostsTable.getRowCount()).isEqualTo(3);
        HtmlTableCell remainingIdCell = stagedPostsTable.getCellAt(2, 0);
        assertThat(remainingIdCell.asText()).isEqualToNormalizingWhitespace(FIRST_POST_ID);
    }

    /**
     * Stage some posts then verify that the remove all button removes them
     */
    @Test
    public void removeAllStagedPosts() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        stageAPostViaTheUI(FIRST_POST_ID);
        stageAPostViaTheUI(SECOND_POST_ID);

        HtmlPage stagedPostsPage = getStagedPostsPage();

        HtmlButton removeAllButton = stagedPostsPage.getHtmlElementById("removeAllButton");
        stagedPostsPage = removeAllButton.click();
        waitForScript();

        String[] stagedPosts = getStagedPostsForBlog(MAIN_BLOG_NAME);
        assertThat(stagedPosts).isEmpty();

        stagedPostsPage = getStagedPostsPage();
        HtmlTable stagedPostsTable = stagedPostsPage.getHtmlElementById(STAGED_POSTS_TABLE);
        assertThat(stagedPostsTable.getRowCount()).isEqualTo(3);
        HtmlTableCell emptyCell = stagedPostsTable.getCellAt(2, 0);
        assertThat(emptyCell.asText()).isEqualToNormalizingWhitespace(noPostsInTableMessage);
    }

    /**
     * Stage a post and verify that the XML can be viewed
     */
    @Test
    public void getXML() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        stageAPostViaTheUI(FIRST_POST_ID);
        HtmlPage stagedPostsPage = getStagedPostsPage();
        HtmlButton getXmlButton = stagedPostsPage.getHtmlElementById("downloadButton");
        HtmlPage xmlPage = getXmlButton.click();
        waitForScript();
        HtmlTextArea xmlField = xmlPage.getHtmlElementById("exportedXMLText");
        assertThat(xmlField.getText()).contains("<regular-title>Queued Post</regular-title>");
        assertThat(xmlField.getText()).contains("<regular-body>post body text here</regular-body>");
        assertThat(xmlField.getText()).contains("<tag>tag1</tag>");
        assertThat(xmlField.getText()).contains("<tag>tag2</tag>");
    }

    /**
     * Verify that the post viewer for a video doesn't have a download button
     */
    @Test
    public void videoPostHasNoStageButton() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        HtmlPage popup = webClient
                .getPage(String.format("%s/postViewer/%s?id=%s", baseUri(), MAIN_BLOG_NAME, VIDEO_POST_ID));
        waitForScript();
        HtmlButton stageButton = popup.getHtmlElementById("stageForDownloadButton");
        assertThat(stageButton.isDisplayed()).isFalse();
    }

    /**
     * Verify that the entry in the staged posts table for a photo post has a button
     * to download images
     */
    @Test
    public void imagePostHasDownloadLink() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        stageAPostViaTheUI(PHOTO_POST_ID);
        HtmlPage stagedPostsPage = getStagedPostsPage();
        HtmlTable stagedPostsTable = stagedPostsPage.getHtmlElementById(STAGED_POSTS_TABLE);
        HtmlTableCell downloadCell = stagedPostsTable.getCellAt(2, 3);
        HtmlButton downloadButton = (HtmlButton) downloadCell.getFirstElementChild();
        assertThat(downloadButton).isNotNull();
        assertThat(downloadButton.asText()).isEqualToNormalizingWhitespace(downloadButtonText);
    }

    /**
     * Helper function to stage a post via the UI -- that is, it brings up the post
     * viewer for the given post (by Post ID), then clicks the Stage button.
     * 
     * @param postID ID of the post to be staged
     */
    private void stageAPostViaTheUI(String postID)
            throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        HtmlPage popup = webClient.getPage(String.format("%s/postViewer/%s?id=%s", baseUri(), MAIN_BLOG_NAME, postID));
        waitForScript();
        HtmlButton stageButton = popup.getHtmlElementById("stageForDownloadButton");
        stageButton.click();
        waitForScript();
    }
    /**
     * Gets the staged posts page, waits for the JS to finish executing, and returns
     * it
     * 
     * @return The staged posts page, after JS has finished executing
     */
    private HtmlPage getStagedPostsPage() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        HtmlPage stagedPostsPage = webClient.getPage(baseUri() + "/staged");
        waitForScript();

        return stagedPostsPage;
    }

}
