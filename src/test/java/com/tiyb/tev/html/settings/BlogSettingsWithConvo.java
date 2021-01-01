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

/**
 * Test cases for the settings page for a given blog, for cases where
 * conversations also need to be set up.
 * 
 * @author tiyb
 *
 */
public class BlogSettingsWithConvo extends SettingsTester {

    @Value("${metadata.defaultBlog.message}")
    private String blogIsDefaultMessage;
    @Value("${md_submit_success}")
    private String attributeChangedSuccessfullyMessage;

    @Before
    public void setupSite() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        restInitMainBlogSettings(Optional.empty());
        restInitConvosForMainBlog();

        mainPage = getSettingsPage(MAIN_BLOG_NAME);
    }

    /**
     * Tests settings around conversation viewing. The following settings are
     * tested, one by one:
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
        zeroOutUpdateServerFunction();
        
        HtmlTextInput tumblrUserName = mainPage.getHtmlElementById("mainUser");
        assertThat(tumblrUserName.getText()).isEqualTo(MAIN_BLOG_NAME);
        HtmlTextInput mainAvatar = mainPage.getHtmlElementById("mainUserAvatarUrl");
        assertThat(mainAvatar.getText()).isEqualTo("http://mainblog/avatar");

        setDropdownValue("conversationDisplayDropdown", "cloud");
        checkMDValue("conversationDisplayStyle", "cloud");
        setDropdownValue("conversationDisplayDropdown", "table");
        checkMDValue("conversationDisplayStyle", "table");
        
        setDropdownValue("conversationSortColumnDropdown", "participantName");
        checkMDValue("conversationSortColumn", "participantName");
        setDropdownValue("conversationSortColumnDropdown", "numMessages");
        checkMDValue("conversationSortColumn", "numMessages");
        
        setDropdownValue("overwriteConvosDropdown", "true");
        checkMDValue("overwriteConvoData", Boolean.toString(true));
        setDropdownValue("overwriteConvosDropdown", "false");
        checkMDValue("overwriteConvoData", Boolean.toString(false));
    }

}
