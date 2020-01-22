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
			addLinkToHeader('navbar-link-md', $.i18n.prop('header_metadataTitle'), '/metadata', true);
			isIndexPage = false;
		} else {
			addLinkToHeader('navbar-link-md', $.i18n.prop('header_metadataTitle'), '/metadata', false);
		}
		
		if(currentPath.includes("/hashtagViewer")) {
			addLinkToHeader('navbar-link-hashtags', $.i18n.prop('header_hashtags'), '/hashtagViewer', true);
			isIndexPage = false;
		} else {
			addLinkToHeader('navbar-link-hashtags', $.i18n.prop('header_hashtags'), '/hashtagViewer', false);
		}
		
		if(currentPath.includes("/staged")) {
			addLinkToHeader('navbar-link-staged', $.i18n.prop('header_stagedPosts'), '/staged', true);
			isIndexPage = false;
		} else {
			addLinkToHeader('navbar-link-staged', $.i18n.prop('header_stagedPosts'), '/staged', false);
		}
		
		if(currentPath.includes("/conversations")) {
			addLinkToHeader('navbar-link-conversations', $.i18n.prop('header_conversationsTitle'), '/conversations', true);
			isIndexPage = false;
		} else {
			addLinkToHeader('navbar-link-conversations', $.i18n.prop('header_conversationsTitle'), '/conversations', false);
		}
		
		if(currentPath.includes("/exportViewer")) {
			isIndexPage = false;
		}
		
		if(isIndexPage) {
			addLinkToHeader('navbar-link-index', $.i18n.prop('header_indexTitle'), '/', true);
		} else {
			addLinkToHeader('navbar-link-index', $.i18n.prop('header_indexTitle'), '/', false);
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
 * @param isSelected
 *            Indicates whether the link should be shown as 'selected' or not
 */
function addLinkToHeader(spanName, linkText, linkUrl, isSelected) {
	var linkContent = "";
	if(isSelected) {
		linkContent = "<span class='navbar-selected'><a href='" + linkUrl + "'>" + linkText + "</a></span>";
	} else {
		var linkContent = "<a href='" + linkUrl + "'>" + linkText + "</a>";
	}
	
	$(linkContent).appendTo('#' + spanName);
}
