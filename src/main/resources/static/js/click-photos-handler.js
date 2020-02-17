/**
 * Adds a click handler to any image displayed on the page, to load that image
 * in its own window. Currently conflicts with the way images are already
 * handled for Photo posts (which have anchors to open images in the single
 * image viewer), but initial testing shows that the conflict works out ok in
 * that the image is only opened once, so being left as-is for now.
 */
$(document).ready(function() {
	$('img').click(function() {
		window.open($(this).attr("src"), '_singlephotoviewer')
	});
});