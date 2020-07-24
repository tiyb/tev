package com.tiyb.tev.html;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlHeading1;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.tiyb.tev.datamodel.Conversation;
import com.tiyb.tev.datamodel.Metadata;

/**
 * Unit tests for testing the main conversation screen, as well as the
 * conversation viewer popup
 * 
 * @author tiyb
 *
 */
public class ConvoHtmlTests extends HtmlTestingClass {

    private static final String CONVERSATION_TABLE_ID = "conversationTable";
    private static final int CONVERSATION_TABLE_NUM_ROWS = 10;
    private static final int FIRST_TABLE_ROW = 2;
    private static final int COLUMN_PARTICIPANT = 0;
    @SuppressWarnings("unused")
    private static final int COLUMN_NUMMESSAGES = 1;

    /**
     * Retrieves the main conversation page
     */
    @Before
    public void setupSite() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        restInitDataForMainBlog(Optional.empty());
        restInitConvosForMainBlog();

        mainPage = webClient.getPage(baseUri() + "/conversations");
        waitForScript();
    }

    /**
     * Verifies that the viewing buttons have the desired effect: First reading
     * pane, then conversation viewer (table vs. cloud)
     */
    @Test
    public void toggleViewingButtons() throws IOException {
        // check initial state of reading pane radio, then select it
        HtmlRadioButtonInput showReadingPaneBtn = mainPage.getHtmlElementById("showReadingPaneSelected");
        assertThat(showReadingPaneBtn.isChecked()).isFalse();
        HtmlRadioButtonInput showPopups = mainPage.getHtmlElementById("showPopupsSelected");
        assertThat(showPopups.isChecked()).isTrue();
        HtmlTableCell readingPaneCell = mainPage.getHtmlElementById("contentDisplayReadingPane");
        assertThat(readingPaneCell.isDisplayed()).isFalse();

        // set to show reading pane, and open a conversation
        showReadingPaneBtn.click();
        waitForScript();
        Metadata md = getMDFromServer(Optional.of(MAIN_BLOG_NAME));
        assertThat(md.getShowReadingPane()).isTrue();
        HtmlTable convoTable = mainPage.getHtmlElementById(CONVERSATION_TABLE_ID);
        convoTable.getCellAt(FIRST_TABLE_ROW, COLUMN_PARTICIPANT).getFirstElementChild().click();
        waitForScript();
        assertThat(readingPaneCell.isDisplayed()).isTrue();
        assertThat(getNumRealWindows()).isEqualTo(1);
        List<FrameWindow> frames = mainPage.getFrames();
        String contents = frames.get(0).getEnclosedPage().getWebResponse().getContentAsString();
        assertThat(contents).contains("<title>participant1</title>");
        assertThat(contents).contains("<span class=\"messageBodySpan\">Message 1</span>");

        // set to show popups, and open a conversation
        showPopups.click();
        waitForScript();
        assertThat(readingPaneCell.isDisplayed()).isFalse();
        md = getMDFromServer(Optional.of(MAIN_BLOG_NAME));
        assertThat(md.getShowReadingPane()).isFalse();
        convoTable.getCellAt(FIRST_TABLE_ROW, COLUMN_PARTICIPANT).getFirstElementChild().click();
        assertThat(readingPaneCell.isDisplayed()).isFalse();
        assertThat(getNumRealWindows()).isEqualTo(2);

        // set cloud/table initial settings
        HtmlRadioButtonInput showWordCloud = mainPage.getHtmlElementById("showCloudSelected");
        assertThat(showWordCloud.isChecked()).isFalse();
        HtmlRadioButtonInput showTable = mainPage.getHtmlElementById("showTableSelected");
        assertThat(showTable.isChecked()).isTrue();
        HtmlDivision convoTableContainer = mainPage.getHtmlElementById("conversationTableContainer");
        assertThat(convoTableContainer.isDisplayed()).isTrue();
        HtmlDivision convoCloud = mainPage.getHtmlElementById("conversationWordCloudContainerContainer");
        assertThat(convoCloud.isDisplayed()).isFalse();

        // change cloud/table settings back and forth
        showWordCloud.click();
        waitForScript();
        assertThat(convoTable.isDisplayed()).isFalse();
        assertThat(convoCloud.isDisplayed()).isTrue();
        showTable.click();
        waitForScript();
        assertThat(convoTable.isDisplayed()).isTrue();
        assertThat(convoCloud.isDisplayed()).isFalse();
    }

    /**
     * Opens a conversation viewer (in pop-up), and checks that some of the contents
     * of the first conversation show up properly
     */
    @Test
    public void openConvo() throws IOException {
        // open popup
        HtmlTable convoTable = mainPage.getHtmlElementById(CONVERSATION_TABLE_ID);
        HtmlPage popup = convoTable.getCellAt(FIRST_TABLE_ROW, COLUMN_PARTICIPANT).getFirstElementChild().click();
        assertThat(getNumRealWindows()).isEqualTo(2);

        // check contents of page; not everything, but some stuff
        DomNodeList<DomNode> h1Fields = popup.querySelectorAll("h1");
        assertThat(h1Fields.size()).isEqualTo(1);
        HtmlHeading1 h1 = (HtmlHeading1) h1Fields.get(0);
        assertThat(h1.getVisibleText()).isEqualToNormalizingWhitespace("Conversation with participant1");

        DomNodeList<DomNode> messages = popup.querySelectorAll("div.messageDiv");
        assertThat(messages.size()).isEqualTo(9);

        DomNodeList<DomNode> messageBodies = popup.querySelectorAll("span.messageBodySpan");
        assertThat(messageBodies.size()).isEqualTo(10);
        HtmlSpan firstMessage = (HtmlSpan) messageBodies.get(0);
        assertThat(firstMessage.getVisibleText()).isEqualToNormalizingWhitespace("Message 1");
    }

    /**
     * Tests that the conversation viewer can be used to hide a conversation (and
     * that it properly disappears from the table), and then tests the close anad
     * refresh button
     */
    @Test
    public void hideConvo() throws IOException, InterruptedException {
        assertThat(getNumRealWindows()).isEqualTo(1);
        HtmlTable convoTable = mainPage.getHtmlElementById(CONVERSATION_TABLE_ID);
        assertThat(convoTable.getRowCount()).isEqualTo(CONVERSATION_TABLE_NUM_ROWS);

        // open popup, then mark conversation hidden (which should close the window)
        HtmlPage popup = convoTable.getCellAt(FIRST_TABLE_ROW, COLUMN_PARTICIPANT).getFirstElementChild().click();
        waitForScript();
        assertThat(getNumRealWindows()).isEqualTo(2);

        HtmlButton hideConvoBtn = popup.getHtmlElementById("hideConvoBtn");
        popup = null;
        hideConvoBtn.click(false, false, false, true);
        waitForScript();
        assertThat(getNumRealWindows()).isEqualTo(1);

        Conversation convo1 = getConversation(MAIN_BLOG_NAME, "participant1");
        assertThat(convo1.getHideConversation()).isTrue();

        // update conversation, un-hide it again, then re-open the window and click the
        // hide and refresh button
        convo1.setHideConversation(false);
        updateConversation(convo1);
        popup = convoTable.getCellAt(FIRST_TABLE_ROW, COLUMN_PARTICIPANT).getFirstElementChild().click();
        waitForScript();
        assertThat(getNumRealWindows()).isEqualTo(2);
        HtmlButton hideAndRefresh = popup.getHtmlElementById("hideConvoAndRefreshBtn");
        hideAndRefresh.click(false, false, false, true);
        waitForScript();
        popup = null;
        assertThat(getNumRealWindows()).isEqualTo(1);

        convo1 = getConversation(MAIN_BLOG_NAME, "participant1");
        assertThat(convo1.getHideConversation()).isTrue();

        mainPage = (HtmlPage) mainPage.refresh();
        waitForScript();
        convoTable = mainPage.getHtmlElementById(CONVERSATION_TABLE_ID);
        assertThat(convoTable.getRowCount()).isEqualTo(CONVERSATION_TABLE_NUM_ROWS - 1);
    }

    /**
     * Hides a couple of conversations and verifies that they disappear from the
     * page, then clicks the "unhide all" button to verify that they come back
     */
    @Test
    public void unhideAllConvos() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        HtmlTable convoTable = mainPage.getHtmlElementById(CONVERSATION_TABLE_ID);
        assertThat(convoTable.getRowCount()).isEqualTo(CONVERSATION_TABLE_NUM_ROWS);

        Conversation convo = getConversation(MAIN_BLOG_NAME, "participant1");
        convo.setHideConversation(true);
        updateConversation(convo);

        HtmlPage newPage = webClient.getPage(baseUri() + "/conversations");
        waitForScript();
        convoTable = newPage.getHtmlElementById(CONVERSATION_TABLE_ID);
        assertThat(convoTable.getRowCount()).isEqualTo(CONVERSATION_TABLE_NUM_ROWS - 1);

        convo = getConversation(MAIN_BLOG_NAME, "participant2");
        convo.setHideConversation(true);
        updateConversation(convo);
        newPage = webClient.getPage(baseUri() + "/conversations");
        waitForScript();
        convoTable = newPage.getHtmlElementById(CONVERSATION_TABLE_ID);
        assertThat(convoTable.getRowCount()).isEqualTo(CONVERSATION_TABLE_NUM_ROWS - 2);

        HtmlButton unhideAllButton = newPage.getHtmlElementById("unhideAllConversationsBtn");
        unhideAllButton.click();
        waitForScript();
        newPage = webClient.getPage(baseUri() + "/conversations");
        waitForScript();
        convoTable = newPage.getHtmlElementById(CONVERSATION_TABLE_ID);
        assertThat(convoTable.getRowCount()).isEqualTo(CONVERSATION_TABLE_NUM_ROWS);
    }
}
