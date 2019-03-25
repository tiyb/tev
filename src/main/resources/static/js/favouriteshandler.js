$(document).ready(function() {
	$('#markUnreadButton').click(function() {
		$.ajax({
			url: "/api/posts/" + postId + "/markUnread",
			dataSrc: "",
			type: "PUT"
		}).then(function(data) {
			opener.location.reload();
			window.close();
		});		
	});
	
	$('#unfavouriteButton').click(function() {
		$.ajax({
			url: "/api/posts/" + postId + "/markNonFavourite",
			dataSrc: "",
			type: "PUT"
		}).then(function(data) {
			opener.location.reload();
			window.close();
		});		
	});
	
	$('#favouriteButton').click(function() {
		$.ajax({
			url: "/api/posts/" + postId + "/markFavourite",
			dataSrc: "",
			type: "PUT"
		}).then(function(data) {
			opener.location.reload();
			window.close();
		});		
	});
	
});