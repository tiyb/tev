package com.tiyb.tev.html.settings;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.javascript.host.event.Event;
import com.tiyb.tev.html.HtmlTestingClass;

/**
 * Helper methods used for testing the settings page for a given blog.
 * 
 * @author tiyb
 *
 */
public abstract class SettingsTester extends HtmlTestingClass {

    /**
     * Helper function to get the settings page for a particular blog
     * 
     * @param blogName Name of the blog for which to retrieve the settings page
     * @return The HtmlPage object, <i>after</i> the scripts have finished running
     */
    protected HtmlPage getSettingsPage(String blogName)
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
    protected void setTextboxValue(String inputId, String newValue) {
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
    protected void setDropdownValue(String inputId, String newValue) {
        HtmlSelect select = mainPage.getHtmlElementById(inputId);
        HtmlOption option = select.getOptionByValue(newValue);
        select.setSelectedAttribute(option, true);
        select.fireEvent("selectmenuselect");
        waitForScript();
    }

    /**
     * Helper method to check that the metadata for a given blog has the right value
     * in one of its values
     * 
     * @param fieldName     Name of the property to check
     * @param expectedValue The value that should be in that property
     */
    protected void checkMDValue(String fieldName, String expectedValue) {
        String mdValue = mainPage.executeJavaScript("metadataObject." + fieldName).getJavaScriptResult().toString();
        assertThat(mdValue).isEqualToNormalizingWhitespace(expectedValue);
    }

    /**
     * Helper function to eliminate the JS function responsible for sending data to
     * the server
     */
    protected void zeroOutUpdateServerFunction() {
        mainPage.executeJavaScript("sendMDData = new function(){alert('updateServer called')};");
    }

}
