$(document).ready(function() {
	$('#fixImagesButton').click(function() {
		$.ajax({
			url: "/api/posts/" + postId + "/fixPhotos",
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