package com.tiyb.tev.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for handling errors within the application. Displays a generic
 * error page in cases where "something" went wrong, as well as a specific page
 * for cases where XML file uploads have failed.
 * 
 * @author tiyb
 * @apiviz.landmark
 */
@Controller
public class TEVErrorController implements ErrorController {

	/**
	 * Method for handling requests for the generic error page. This is a simplistic
	 * implementation; there isn't any logic for trying to figure out the error and
	 * redirect to a more specific page.
	 * 
	 * @return Mapping to the generic error page
	 */
	@RequestMapping("/error")
	public String handleError() {
		return "error";
	}

	/**
	 * Method for handling requests for the "bad XML" error page (from errors in
	 * parsing Conversation or Post export files).
	 * 
	 * @return Mapping to the "bad XML" error page
	 */
	@RequestMapping("/errorbadxml")
	public String handleXMLError() {
		return "errorbadxml";
	}

	/**
	 * Override of the <code>ErrorController</code> method for getting the path to
	 * the generic error page
	 */
	@Override
	public String getErrorPath() {
		return "/error";
	}

}
