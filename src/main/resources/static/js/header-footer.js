/**
 * Loads the header and footer HTML from the server, and then builds the
 * links/text in the header based on the URL of the current page. (i.e. links
 * for every page other than the current one, which is given a span with the
 * navbar-selected class.)
 */
$(document).ready(function() {
	$('#header').load("/header", null, function() {
		var isIndexPage = true;
		var currentPath = window.location.pathname;
		
		if(currentPath.includes("/metadata")) {
			addSelectedTextToHeader("navbar-link-md", $.i18n.prop('header_metadataTitle'));
			isIndexPage = false;
		} else {
			addLinkToHeader('navbar-link-md', $.i18n.prop('header_metadataTitle'), '/metadata');
		}
		
		if(currentPath.includes("/hashtagViewer")) {
			addSelectedTextToHeader("navbar-link-hashtags", $.i18n.prop('header_hashtags'));
			isIndexPage = false;
		} else {
			addLinkToHeader('navbar-link-hashtags', $.i18n.prop('header_hashtags'), '/hashtagViewer');
		}
		
		if(currentPath.includes("/staged")) {
			addSelectedTextToHeader("navbar-link-staged", $.i18n.prop('header_stagedPosts'));
			isIndexPage = false;
		} else {
			addLinkToHeader('navbar-link-staged', $.i18n.prop('header_stagedPosts'), '/staged');
		}
		
		if(currentPath.includes("/conversations")) {
			addSelectedTextToHeader("navbar-link-conversations", $.i18n.prop('header_conversationsTitle'));
			isIndexPage = false;
		} else {
			addLinkToHeader('navbar-link-conversations', $.i18n.prop('header_conversationsTitle'), '/conversations');
		}
		
		if(isIndexPage) {
			addSelectedTextToHeader("navbar-link-index", $.i18n.prop('header_indexTitle'));
		} else {
			addLinkToHeader('navbar-link-index', $.i18n.prop('header_indexTitle'), '/');
		}
	});
	$('#footer').load("/footer");
	
});

/**
 * Creates a link within the header, with given text for a given page / URL
 * 
 * @param spanName
 *            The span to which this link should be added
 * @param linkText
 *            The text to display
 * @param linkUrl
 *            The URL for the link
 */
function addLinkToHeader(spanName, linkText, linkUrl) {
	var linkContent = "<a href='" + linkUrl + "'>" + linkText + "</a>";
	$(linkContent).appendTo('#' + spanName);
}

/**
 * Creates a span within the header for the current page, with a navbar-selected
 * class applied to it.
 * 
 * @param spanName
 *            The span to which this text should be added
 * @param textValue
 *            The text to display
 */
function addSelectedTextToHeader(spanName, textValue) {
	var textContent = "<span class='navbar-selected'>" + textValue + "</span>";
	$(textContent).appendTo('#' + spanName);
}