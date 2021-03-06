$.i18n.properties({
	name: 'messages',
	path: '/js/i18n/',
	mode: 'both'
});

$(document).ready(function() {
	$('#buttonsDiv').load("/viewerbuttons");
	
	$.ajax({
		url: "/api/posts/" + blogName + "/" + postId,
		dataSrc: "",
		type: "GET"
	}).then(function(postData) {
		if(postData.isFavourite === true) {
			$("#favouriteButton").text($.i18n.prop("viewer_buttons_unfavourite"));
		} else {
			$("#favouriteButton").text($.i18n.prop("viewer_buttons_favourite"));
		}
		
		if(postData.isRead) {
			$('#markReadButton').text($.i18n.prop("viewer_buttons_unread"));
		} else {
			$('#markReadButton').text($.i18n.prop("viewer_buttons_read"));
		}
		
		$('#markReadButton').click(function() {
			var url;
			if(postData.isRead) {
				url = "/api/posts/" + blogName + "/" + postId + "/markUnread";
			} else {
				url = "/api/posts/" + blogName + "/" + postId + "/markread";
			}
			$.ajax({
				url: url,
				dataSrc: "",
				type: "GET"
			}).then(function(data) {
				opener.location.reload();
				window.close();
			});
		});
		
		$('#favouriteButton').click(function() {
			var url;
			if(postData.isFavourite) {
				url = "/api/posts/" + blogName + "/" + postId + "/markNonFavourite";
			} else {
				url = "/api/posts/" + blogName + "/" + postId + "/markFavourite";
			}
			
			$.ajax({
				url: url,
				dataSrc: "",
				type: "GET"
			}).then(function(data) {
				location.reload();
			});
		});
		
		$('#closeButton').click(function() {
			window.close();
		});
		
		$('#closeAndRefreshButton').click(function() {
			opener.location.reload();
			window.close();
		});
		
		if(postData.type === "video") {
			$('#stageForDownloadButton').hide();
		} else {
			$.ajax({
				url: "/staging-api/posts/" + blogName,
				dataSrc: ""
			}).then(function(data) {
				var stagedPost = false;
				data.forEach(function(element) {
					if(element === postId) {
						stagedPost = true;
					}
				});
				
				if(stagedPost) {
					$('#stageForDownloadButton').text($.i18n.prop('viewer_buttons_unmarkfordownload'));
				} else {
					$('#stageForDownloadButton').text($.i18n.prop('viewer_buttons_markfordownload'));
				}
				
				$('#stageForDownloadButton').click(function() {
					$.ajax({
						url: "/staging-api/posts/" + blogName + "/" + postId,
						dataSrc: "",
						type: (stagedPost ? "DELETE" : "POST")
					}).then(function(data) {
						location.reload();
					});
				});
			});
		}
	});
	
});