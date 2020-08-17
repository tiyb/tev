/**
 * Sets up a keyboard handler to close the current window if the user presses
 * Esc
 */
$(document).ready(function() {
	$(document).keydown(function(e) {
		// ESC
		if (e.keyCode === 27) {
			window.close();
		}
	});
});