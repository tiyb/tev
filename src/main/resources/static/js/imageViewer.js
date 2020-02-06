/**
 * For the post viewer for Photo posts, adds the event handler for the "fix
 * images" button
 */
$(document).ready(function() {
	$('#fixImagesButton').click(function() {
		$.ajax({
			url: "/api/posts/" + blogName + "/" + postId + "/fixPhotos",
			dataSrc: ""
		}).then(function(data) {
			if(data) {
				location.reload();
			} else {
				alert($.i18n.prop('imgvwr_failedToRefresh'));
			}
		});
	});
});