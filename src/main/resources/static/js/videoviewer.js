/**
 * For the post viewer for Video posts, adds the event handler for the "show
 * videos" button. Goes through the videoPlayer attributes of the video post,
 * and opens a new window for each (assuming they are different).
 */
$(document).ready(function() {
	$('#showVideosButton').click(function() {
		$.ajax({
			url : "/api/posts/" + blogName + "/" + postId + "/video",
			dataSrc : ""
		}).then(function(data) {
			var result = data.videoPlayer.match(/src="([^"]*)"/);
			var videoURL = result[1];

			var result250 = data.videoPlayer250.match(/src="([^"]*)"/);
			var videoURL250 = result250[1];

			var result500 = data.videoPlayer500.match(/src="([^"]*)"/);
			var videoURL500 = result500[1];

			window.open(videoURL);
			if (videoURL !== videoURL250) {
				window.open(videoURL250);
			}
			if ((videoURL !== videoURL500) && (videoURL250 !== videoURL500)) {
				window.open(videoURL500);
			}
		});
	});
});