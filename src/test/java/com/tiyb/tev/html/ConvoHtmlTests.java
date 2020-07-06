package com.tiyb.tev.html;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.tiyb.tev.TevTestingHelpers;
import com.tiyb.tev.datamodel.Metadata;

public class ConvoHtmlTests extends HtmlTestingClass {

    private static final String CONVERSATION_TABLE_ID = "conversationTable";
    private static final int FIRST_TABLE_ROW = 2;
    private static final int COLUMN_PARTICIPANT = 0;
    private static final int COLUMN_NUMMESSAGES = 1;

    HtmlTable convoTable;

    @Before
    public void setupSite() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        restInitDataForMainBlog(Optional.empty());
        restInitConvosForMainBlog();
        
        mainPage = webClient.getPage(baseUri() + "/conversations");
        webClient.waitForBackgroundJavaScript(WAIT_TIME_FOR_JS);
        convoTable = mainPage.getHtmlElementById(CONVERSATION_TABLE_ID);
    }

    @Test
    public void toggleViewingButtons() throws IOException {
        // check initial state of reading pane radio, then select it
        HtmlRadioButtonInput showReadingPaneBtn = mainPage.getHtmlElementById("showReadingPaneSelected");
        assertThat(showReadingPaneBtn.isChecked()).isFalse();
        HtmlRadioButtonInput showPopups = mainPage.getHtmlElementById("showPopupsSelected");
        assertThat(showPopups.isChecked()).isTrue();
        HtmlTableCell readingPaneCell = mainPage.getHtmlElementById("contentDisplayReadingPane");
        assertThat(readingPaneCell.isDisplayed()).isFalse();

        showReadingPaneBtn.click();
        webClient.waitForBackgroundJavaScript(WAIT_TIME_FOR_JS);
        Metadata md = getMDFromServer(Optional.of(TevTestingHelpers.MAIN_BLOG_NAME));
        assertThat(md.getShowReadingPane()).isTrue();
        convoTable = mainPage.getHtmlElementById(CONVERSATION_TABLE_ID);
        convoTable.getCellAt(FIRST_TABLE_ROW, COLUMN_PARTICIPANT).getFirstElementChild().click();
        webClient.waitForBackgroundJavaScript(WAIT_TIME_FOR_JS);
        assertThat(readingPaneCell.isDisplayed()).isTrue();
        assertThat(getNumRealWindows(webClient.getWebWindows())).isEqualTo(1);
        List<FrameWindow> frames = mainPage.getFrames();
        String contents = frames.get(0).getEnclosedPage().getWebResponse().getContentAsString();
        assertThat(contents).contains("<title>participant1</title>");
        assertThat(contents).contains("<span class=\"messageBodySpan\">Message 1</span>");

        showPopups.click();
        webClient.waitForBackgroundJavaScript(WAIT_TIME_FOR_JS);
        assertThat(readingPaneCell.isDisplayed()).isFalse();
        convoTable.getCellAt(FIRST_TABLE_ROW, COLUMN_PARTICIPANT).getFirstElementChild().click();
        assertThat(readingPaneCell.isDisplayed()).isFalse();
        assertThat(getNumRealWindows(webClient.getWebWindows())).isEqualTo(2);
    }

//    @Test
//    public void openConvo() {
//        assertThat(true).isEqualTo(false);
//    }
//    
//    @Test
//    public void hideConvo() {
//        // TODO hide/unhide convo button
//        // TODO hide convo and refresh button
//        assertThat(true).isEqualTo(false);
//    }
//
//    @Test
//    public void unhideAllConvos() {
//        assertThat(true).isEqualTo(false);
//    }
}
