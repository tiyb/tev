package com.tiyb.tev.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tiyb.tev.datamodel.Metadata;

/**
 * Controller for handling errors within the application. Displays a generic error page in cases
 * where "something" went wrong, as well as a specific page for cases where XML file uploads have
 * failed.
 *
 * @author tiyb
 */
@Controller
public class TEVErrorController implements ErrorController {

    /**
     * Name of the param added to the model for a participant's name
     */
    private static final String MODEL_ATT_PARTICIPANT = "participantName";

    /**
     * REST controller for working with metadata
     */
    @Autowired
    private TEVMetadataRestController mdController;

    /**
     * Method for handling requests for the generic error page. This is a simplistic implementation;
     * there isn't any logic for trying to figure out the error and redirect to a more specific
     * page.
     *
     * @param model The model used by Thymeleaf
     * @return Mapping to the generic error page
     */
    @RequestMapping("/error")
    public String handleError(final Model model) {
        updateModelWithBlogName(model);
        updateModelWithTheme(model);
        return "error";
    }

    /**
     * Method for handling requests for the "bad XML" error page (from errors in parsing
     * Conversation or Post export files).
     *
     * @param model The model used by Thymeleaf
     * @return Mapping to the "bad XML" error page
     */
    @RequestMapping("/errorbadxml")
    public String handleXMLError(final Model model) {
        updateModelWithBlogName(model);
        updateModelWithTheme(model);
        return "errorbadxml";
    }

    /**
     * Method for handling requests for the "blog name / participant name mismatch" error page.
     *
     * @param blogName        Name of the blog from the Metadata
     * @param participantName Name of the partipant from the Conversation XML
     * @param model           Model for Thymeleaf's usage
     * @return Mapping to the error page
     */
    @RequestMapping("/errorblogmismatch")
    public String handleBlogMismatchError(@RequestParam("blogName") final String blogName,
            @RequestParam(MODEL_ATT_PARTICIPANT) final String participantName, final Model model) {
        model.addAttribute(TEVUIController.MODEL_ATTRIBUTE_BLOGNAME, blogName);
        model.addAttribute(MODEL_ATT_PARTICIPANT, participantName);
        updateModelWithBlogName(model);
        updateModelWithTheme(model);
        return "errorblognamemismatch";
    }

    /**
     * Override of the
     * {@link org.springframework.boot.web.servlet.error.ErrorController#getErrorPath()
     * ErrorController#getErrorPath()} method for getting the path to the generic error page
     */
    @Override
    public String getErrorPath() {
        return "/error";
    }

    /**
     * Used to update the model object with the name of the current in-use blog. Could have simply
     * added this logic to the {@link #updateModelWithTheme(Model)} method, but broke it out
     * separately in case the logic for determining the current blog gets hairy.
     *
     * @param model The model used by Thymeleaf
     */
    private void updateModelWithBlogName(final Model model) {
        final Metadata m = mdController.getDefaultMetadata();
        if (m == null) {
            model.addAttribute(TEVUIController.MODEL_ATTRIBUTE_BLOGNAME, "");
        } else {
            model.addAttribute(TEVUIController.MODEL_ATTRIBUTE_BLOGNAME, m.getBlog());
        }
    }

    /**
     * Used to set the theme, so that Thymeleaf pages can set the correct CSS
     *
     * @param model The model to be updated
     */
    private void updateModelWithTheme(final Model model) {
        final String blogName = (String) model.getAttribute(TEVUIController.MODEL_ATTRIBUTE_BLOGNAME);
        final Metadata md = mdController.getMetadataForBlog(blogName);
        String theme = md.getTheme();
        if (theme == null || theme.equals(StringUtils.EMPTY)) {
            theme = Metadata.DEFAULT_THEME;
            md.setTheme(theme);
            mdController.updateMetadata(md.getId(), md);
        }

        model.addAttribute(TEVUIController.MODEL_ATTRIBUTE_THEME, theme);
    }

}
