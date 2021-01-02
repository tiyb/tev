package com.tiyb.tev.html.settings;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.javascript.host.event.Event;
import com.tiyb.tev.datamodel.Metadata;

/**
 * Test cases for the settings page for a given blog. In most cases, settings
 * are set to things that are not the default they were sent to; not bothering
 * to re-set things back to the default and re-checking.
 * 
 * @author tiyb
 *
 */
public class BlogSettingPageHtmlTests extends SettingsTester {

    @Value("${metadata.defaultBlog.message}")
    private String blogIsDefaultMessage;
    @Value("${md_submit_success}")
    private String attributeChangedSuccessfullyMessage;

    @Before
    public void setupSite() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        restInitMainBlogSettings(Optional.empty());
        mainPage = getSettingsPage(MAIN_BLOG_NAME);
    }

    @Test
    public void checkInitialSettings() {
        Metadata md = getMDFromServerNotDefault(Optional.of(MAIN_BLOG_NAME));
        assertThat(md.getBlog()).isEqualTo(MAIN_BLOG_NAME);
        assertThat(md.getFilter()).isEqualTo("Do Not Filter");
        assertThat(md.getSortColumn()).isEqualTo("ID");
        assertThat(md.getFavFilter()).isEqualTo("Show Everything");
        assertThat(md.getPageLength()).isEqualTo(10);
        assertThat(md.getShowReadingPane()).isEqualTo(false);
        assertThat(md.getOverwritePostData()).isEqualTo(true);
        assertThat(md.getExportImagesFilePath()).isEqualTo("");
        assertThat(md.getBaseMediaPath()).isBlank();
        assertThat(md.getTheme()).isEqualTo(Metadata.DEFAULT_THEME);
    }

    /**
     * Changes the blog's name, and verifies the MD has been updated appropriately
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
    }

    /**
     * Tests changing a number of properties, and verifying that the local MD object
     * has been updated. (Doesn't send to the server; a separate test tests for
     * that.)
     * 
     * <ol>
     * <li>filter</li>
     * <li>sort by</li>
     * <li>favourites</li>
     * <li>page length</li>
     * <li>reading pane</li>
     * <li>overwrite posts</li>
     * <li>image export path</li>
     * <li>base media path</li>
     * <li>theme</li>
     * </ol>
     */
    @Test
    public void mainBlogSettings() {
        zeroOutUpdateServerFunction();

        setDropdownValue("filterDropdown", "Filter Read Posts");
        checkMDValue("filter", "Filter Read Posts");
        setDropdownValue("filterDropdown", "Do not Filter");
        checkMDValue("filter", "Do not Filter");

        setDropdownValue("sortByDropdown", "State");
        checkMDValue("sortColumn", "State");
        setDropdownValue("sortByDropdown", "ID");
        checkMDValue("sortColumn", "ID");

        setDropdownValue("favsDropdown", "Show Non Favourites");
        checkMDValue("favFilter", "Show Non Favourites");
        setDropdownValue("favsDropdown", "Show Everything");
        checkMDValue("favFilter", "Show Everything");

        setDropdownValue("pageLengthDropdown", "100");
        checkMDValue("pageLength", Integer.toString(100));
        setDropdownValue("pageLengthDropdown", "10");
        checkMDValue("pageLength", Integer.toString(10));

        setDropdownValue("showReadingPaneDropdown", "true");
        checkMDValue("showReadingPane", Boolean.toString(true));
        setDropdownValue("showReadingPaneDropdown", "false");
        checkMDValue("showReadingPane", Boolean.toString(false));

        setDropdownValue("overwritePostsDropdown", "true");
        checkMDValue("overwritePostData", Boolean.toString(true));
        setDropdownValue("overwritePostsDropdown", "false");
        checkMDValue("overwritePostData", Boolean.toString(false));

        setTextboxValue("imageExportPath", "export path");
        checkMDValue("exportImagesFilePath", "export path");
        setTextboxValue("imageExportPath", "");
        checkMDValue("exportImagesFilePath", "");

        setTextboxValue("baseMediaPath", "blah");
        checkMDValue("baseMediaPath", "blah");
        setTextboxValue("baseMediaPath", "");
        checkMDValue("baseMediaPath", "");

        setDropdownValue("themesDropdown", "flick");
        checkMDValue("theme", "flick");
        setDropdownValue("themesDropdown", Metadata.DEFAULT_THEME);
        checkMDValue("theme", Metadata.DEFAULT_THEME);
    }

    /**
     * TODO upload posts 
     * TODO upload convos
     */
    @Test
    public void uploads() {
        assertThat(true).isEqualTo(true);
    }

}
