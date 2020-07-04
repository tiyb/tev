package com.tiyb.tev.html;

import com.gargoylesoftware.htmlunit.IncorrectnessListener;

public class SilentHtmlIncorrectnessListener implements IncorrectnessListener {

    @Override
    public void notify(String message, Object origin) {
        // do nothing
    }

}
