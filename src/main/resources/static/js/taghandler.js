/**
 * Sets up a click handler for any span with the class 'hashtagspan', to either:
 * re-navigate the parent to the posts page, searching for this hashtag (if
 * there <i>is</i> a parent, or navigate the current page to the same.
 */
$(document).ready(function() {
	$(document).on('click', 'span[class=hashtagspan]', function() {
		var newHostURL = "/index?hashsearch=" + encodeURI($(this).text());
		if (window.opener === null) {
			location.replace(newHostURL);
		} else {
			window.parent.opener.location.replace(newHostURL);
			window.close();
		}
	});
});