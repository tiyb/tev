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
			$('#favouriteButton').show();
			$('#unfavouriteButton').hide();
		});		
	});
	
	$('#favouriteButton').click(function() {
		$.ajax({
			url: "/api/posts/" + postId + "/markFavourite",
			dataSrc: "",
			type: "PUT"
		}).then(function(data) {
			$('#unfavouriteButton').show();
			$('#favouriteButton').hide();
		});		
	});
	
	$('#closeButton').click(function() {
		window.close();
	});
	
	$('#closeAndRefresh').click(function() {
		opener.location.reload();
		window.close();
	});
	
});