package com.tiyb.tev.html;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.javascript.host.dom.NodeList;
import com.gargoylesoftware.htmlunit.javascript.host.event.Event;
import com.tiyb.tev.datamodel.Metadata;

/**
 * Test cases for the settings page for a given blog. For "performance" reasons
 * (so that Unit tests don't take forever to execute), functions are typically
 * grouped together into one test, however, in cases where the page would have
 * to be refreshed anyway, a separate case is created.
 * 
 * @author tiyb
 *
 */
public class BlogSettingPageHtmlTests extends HtmlTestingClass {

    private static final String DELETE_BLOG_BUTTON = "deleteBlogButton";

    private static final String SET_DEFAULT_BLOG_BUTTON = "setDefaultBlogButton";

    private static final String BLOG_IS_DEFAULT_MESSAGE = "blogIsDefaultMessage";

    private Logger logger = LoggerFactory.getLogger(BlogSettingPageHtmlTests.class);

    @Rule
    public TemporaryFolder mainBlogMediaFolder = new TemporaryFolder();

    @Value("${metadata.defaultBlog.message}")
    private String blogIsDefaultMessage;
    @Value("${md_submit_success}")
    private String attributeChangedSuccessfullyMessage;

    @Before
    public void setupSite() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        restInitMainBlogSettings(Optional.of(mainBlogMediaFolder.getRoot().getAbsolutePath()));
        restInitAdditionalBlog(SECOND_BLOG_NAME);
        restInitConvosForMainBlog();

        // TODO remove this alert handler
        webClient.setAlertHandler(new AlertHandler() {

            @Override
            public void handleAlert(Page page, String message) {
                logger.info("JS ALERT: " + message);
            }

        });

        mainPage = getSettingsPage(MAIN_BLOG_NAME);
    }

    /**
     * Tests settings around the main blog
     * 
     * <ol>
     * <li>Checks that the correct messaging about default or non-default shows up,
     * along with the "make default blog" button only showing up when the blog is
     * <i>not</i> the default</li>
     * <li>Makes a non-default blog the default, then back, asserting that metadata
     * is changed appropriately</li>
     * </ol>
     */
    @Test
    public void setDefaultBlog() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        HtmlParagraph mainBlogMessage = mainPage.getHtmlElementById(BLOG_IS_DEFAULT_MESSAGE);
        assertThat(mainBlogMessage.isDisplayed()).isTrue();
        assertThat(mainBlogMessage.asText()).isEqualToNormalizingWhitespace(blogIsDefaultMessage);
        HtmlButtonInput makeDefaultButton = mainPage.getHtmlElementById(SET_DEFAULT_BLOG_BUTTON);
        assertThat(makeDefaultButton.isDisplayed()).isFalse();
        HtmlPage alternateBlogSettingsPage = getSettingsPage(SECOND_BLOG_NAME);
        mainBlogMessage = alternateBlogSettingsPage.getHtmlElementById(BLOG_IS_DEFAULT_MESSAGE);
        assertThat(mainBlogMessage.isDisplayed()).isFalse();
        makeDefaultButton = alternateBlogSettingsPage.getHtmlElementById(SET_DEFAULT_BLOG_BUTTON);
        assertThat(makeDefaultButton.isDisplayed()).isTrue();

        makeDefaultButton.click();
        waitForScript();
        Metadata md = getMDFromServer(Optional.of(MAIN_BLOG_NAME));
        assertThat(md.getIsDefault()).isFalse();
        md = getMDFromServer(Optional.of(SECOND_BLOG_NAME));
        assertThat(md.getIsDefault()).isTrue();
        mainPage = getSettingsPage(MAIN_BLOG_NAME);
        makeDefaultButton = mainPage.getHtmlElementById(SET_DEFAULT_BLOG_BUTTON);
        assertThat(makeDefaultButton.isDisplayed()).isTrue();
        makeDefaultButton.click();
        waitForScript();

        md = getMDFromServer(Optional.of(MAIN_BLOG_NAME));
        assertThat(md.getIsDefault()).isTrue();
        md = getMDFromServer(Optional.of(SECOND_BLOG_NAME));
        assertThat(md.getIsDefault()).isFalse();
    }

    /**
     * Tests deleting the blog
     */
    @Test
    public void deleteBlog() throws IOException {
        HtmlButtonInput deleteButton = mainPage.getHtmlElementById(DELETE_BLOG_BUTTON);
        mainPage = deleteButton.click();
        waitForScript();

        Metadata[] allMDObjects = getAllMDObjects();
        assertThat(allMDObjects.length).isEqualTo(1);
    }

    /**
     * Tests changing the blog name (and changing it back)
     */
    @Test
    public void changeBlogName() {
        HtmlTextInput blogNameInput = mainPage.getHtmlElementById("blogNameInput");
        assertThat(blogNameInput.getText()).isEqualTo(MAIN_BLOG_NAME);
        blogNameInput.setText("blah");
        blogNameInput.fireEvent(Event.TYPE_CHANGE);
        waitForScript();

        Metadata md = getMDFromServerNotDefault(Optional.of("blah"));
        assertThat(md).isNotNull();
        Metadata[] allMDObjects = getAllMDObjects();
        assertThat(allMDObjects.length).isEqualTo(2);

        blogNameInput.focus();
        blogNameInput.setText(MAIN_BLOG_NAME);
        blogNameInput.fireEvent(Event.TYPE_CHANGE);
        waitForScript();

        md = getMDFromServerNotDefault(Optional.of(MAIN_BLOG_NAME));
        assertThat(md).isNotNull();
        allMDObjects = getAllMDObjects();
        assertThat(allMDObjects.length).isEqualTo(2);
    }

    /**
     * Tests settings around post viewing. The following settings are tested, one by
     * one:
     * 
     * <ol>
     * <li>base media path</li>
     * <li>filter</li>
     * <li>sort order</li>
     * <li>show favs</li>
     * <li>num items to show</li>
     * <li>reading pane</li>
     * <li>overwrite posts</li>
     * <li>export path</li>
     * <li>theme</li>
     * </ol>
     */
    @Test
    public void postViewSettings() {
        setTextboxValue("baseMediaPath", "blah");
        checkMDStringValue("BaseMediaPath", "blah", MAIN_BLOG_NAME);
        setTextboxValue("baseMediaPath", "");
        checkMDStringValue("BaseMediaPath", "", MAIN_BLOG_NAME);

        setDropdownValue("filterDropdown", "Filter Read Posts");
        checkMDStringValue("Filter", "Filter Read Posts", MAIN_BLOG_NAME);
        setDropdownValue("filterDropdown", "Do not Filter");
        checkMDStringValue("Filter", "Do not Filter", MAIN_BLOG_NAME);

        setDropdownValue("sortByDropdown", "State");
        checkMDStringValue("SortColumn", "State", MAIN_BLOG_NAME);
        setDropdownValue("sortByDropdown", "ID");
        checkMDStringValue("SortColumn", "ID", MAIN_BLOG_NAME);

        setDropdownValue("favsDropdown", "Show Non Favourites");
        checkMDStringValue("FavFilter", "Show Non Favourites", MAIN_BLOG_NAME);
        setDropdownValue("favsDropdown", "Show Everything");
        checkMDStringValue("FavFilter", "Show Everything", MAIN_BLOG_NAME);

        setDropdownValue("pageLengthDropdown", "100");
        checkMDIntValue("PageLength", 100, MAIN_BLOG_NAME);
        setDropdownValue("pageLengthDropdown", "10");
        checkMDIntValue("PageLength", 10, MAIN_BLOG_NAME);

        setDropdownValue("showReadingPaneDropdown", "true");
        checkMDBoolValue("ShowReadingPane", true, MAIN_BLOG_NAME);
        setDropdownValue("showReadingPaneDropdown", "false");
        checkMDBoolValue("ShowReadingPane", false, MAIN_BLOG_NAME);

        setDropdownValue("overwritePostsDropdown", "true");
        checkMDBoolValue("OverwritePostData", true, MAIN_BLOG_NAME);
        setDropdownValue("overwritePostsDropdown", "false");
        checkMDBoolValue("OverwritePostData", false, MAIN_BLOG_NAME);

        setTextboxValue("imageExportPath", "export path");
        checkMDStringValue("ExportImagesFilePath", "export path", MAIN_BLOG_NAME);
        setTextboxValue("imageExportPath", "");
        checkMDStringValue("ExportImagesFilePath", "", MAIN_BLOG_NAME);

        setDropdownValue("themesDropdown", "flick");
        checkMDStringValue("Theme", "flick", MAIN_BLOG_NAME);
    }

    /**
     * Tests settings around conversation viewing. The following settings are tested, one by one:
     * 
     * <ol>
     * <li>verify user name and avatar show up</li>
     * <li>conversation display style</li>
     * <li>conversation sorting</li>
     * <li>overwrite convos</li>
     * </ol>
     */
    @Test
    public void conversationSettings() {
        HtmlTextInput tumblrUserName = mainPage.getHtmlElementById("mainUser");
        assertThat(tumblrUserName.getText()).isEqualTo(MAIN_BLOG_NAME);
        HtmlTextInput mainAvatar = mainPage.getHtmlElementById("mainUserAvatarUrl");
        assertThat(mainAvatar.getText()).isEqualTo("http://mainblog/avatar");
        
        setDropdownValue("conversationDisplayDropdown", "cloud");
        checkMDStringValue("ConversationDisplayStyle", "cloud", MAIN_BLOG_NAME);
        setDropdownValue("conversationDisplayDropdown", "table");
        checkMDStringValue("ConversationDisplayStyle", "table", MAIN_BLOG_NAME);
        
        setDropdownValue("conversationSortColumnDropdown", "participantName");
        checkMDStringValue("ConversationSortColumn", "participantName", MAIN_BLOG_NAME);
        setDropdownValue("conversationSortColumnDropdown", "numMessages");
        checkMDStringValue("ConversationSortColumn", "numMessages", MAIN_BLOG_NAME);
        
        setDropdownValue("overwriteConvosDropdown", "true");
        checkMDBoolValue("OverwriteConvoData", true, MAIN_BLOG_NAME);
        setDropdownValue("overwriteConvosDropdown", "false");
        checkMDBoolValue("OverwriteConvoData", false, MAIN_BLOG_NAME);
    }

//    @Test
//    public void uploads() {
//        // TODO upload posts
//        // TODO upload convos
//        assertThat(true).isEqualTo(true);
//    }

//    @Test
//    public void otherSettings() {
//        // TODO mark all posts read
//        // TODO mark all posts unread
//        // TODO clean up images
//        // TODO import images
//        assertThat(true).isEqualTo(true);
//    }

    /**
     * Helper function to get the settings page for a particular blog
     * 
     * @param blogName Name of the blog for which to retrieve the settings page
     * @return The HtmlPage object, <i>after</i> the scripts have finished running
     */
    private HtmlPage getSettingsPage(String blogName)
            throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        HtmlPage page = webClient.getPage(baseUri() + "/metadata/" + blogName);
        waitForScript();
        return page;
    }

    /**
     * Helper function to set the value of an HTML input box, and wait for
     * events/scripts to complete
     * 
     * @param inputId  ID of the HTML input
     * @param newValue New value to put in the input
     */
    private void setTextboxValue(String inputId, String newValue) {
        HtmlTextInput input = mainPage.getHtmlElementById(inputId);
        input.setText(newValue);
        input.fireEvent(Event.TYPE_CHANGE);
        waitForScript();
    }

    /**
     * Helper function to set the value of an HTML drop-down, and wait for
     * events/scripts to complete
     * 
     * @param inputId  ID of the HTML input
     * @param newValue New value to put in the input
     */
    private void setDropdownValue(String inputId, String newValue) {
        HtmlSelect select = mainPage.getHtmlElementById(inputId);
        HtmlOption option = select.getOptionByValue(newValue);
        select.setSelectedAttribute(option, true);
        select.fireEvent("selectmenuselect");
        waitForScript();
    }

    /**
     * Helper method to check that the metadata for a given blog has the right value
     * in one of its string values
     * 
     * @param fieldName     Name of the property to check (without the "get" prefix)
     * @param expectedValue The value that should be in that property
     * @param blogName      Name of the blog for which the metadata should be
     *                      retrieved
     */
    private void checkMDStringValue(String fieldName, String expectedValue, String blogName) {
        try {
            Metadata md = getMDFromServer(Optional.of(blogName));
            Method mdMethod = Metadata.class.getMethod("get" + fieldName);
            String returnValue = (String) mdMethod.invoke(md);

            assertThat(returnValue).isEqualTo(expectedValue);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            assertThat(true).isFalse();
        }
    }

    /**
     * Helper method to check that the metadata for a given blog has the right value
     * in one of its int values
     * 
     * @param fieldName     Name of the property to check (without the "get" prefix)
     * @param expectedValue The value that should be in that property
     * @param blogName      Name of the blog for which the metadata should be
     *                      retrieved
     */
    private void checkMDIntValue(String fieldName, Integer expectedValue, String blogName) {
        try {
            Metadata md = getMDFromServer(Optional.of(blogName));
            Method mdMethod = Metadata.class.getMethod("get" + fieldName);
            Integer returnValue = (Integer) mdMethod.invoke(md);
            assertThat(returnValue).isEqualTo(expectedValue);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            assertThat(true).isFalse();
        }
    }

    /**
     * Helper method to check that the metadata for a given blog has the right value
     * in one of its bool values
     * 
     * @param fieldName     Name of the property to check (without the "get" prefix)
     * @param expectedValue The value that should be in that property
     * @param blogName      Name of the blog for which the metadata should be
     *                      retrieved
     */
    private void checkMDBoolValue(String fieldName, Boolean expectedValue, String blogName) {
        try {
            Metadata md = getMDFromServer(Optional.of(blogName));
            Method mdMethod = Metadata.class.getMethod("get" + fieldName);
            Boolean returnValue = (Boolean) mdMethod.invoke(md);
            assertThat(returnValue).isEqualTo(expectedValue);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            assertThat(true).isFalse();
        }
    }

}
