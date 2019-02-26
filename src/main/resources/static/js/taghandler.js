$(document).ready(function() {
	$(document).on('click', 'span[class=hashtagspan]', function() {
		var newHostURL = window.location.protocol + "//" + window.location.hostname + ":" + window.location.port + "/?hashsearch=" + encodeURI($(this).text());
		window.parent.opener.location.replace(newHostURL);
		window.close();
	});
	
	$(document).keydown(function(e) {
		//ESC
		if(e.keyCode == 27) {
			window.close();
		}
	});
});