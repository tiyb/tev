package com.tiyb.tev.html;

import com.gargoylesoftware.htmlunit.IncorrectnessListener;

/**
 * Implements the HtmlUnit IncorrectnessListener interface, which handles errors for incorrect HTML.
 * @author tiyb
 *
 */
public class SilentHtmlIncorrectnessListener implements IncorrectnessListener {

    /**
     * Do-nothing implementation
     */
    @Override
    public void notify(String message, Object origin) {
        // do nothing
    }

}
