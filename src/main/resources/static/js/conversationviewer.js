$.i18n.properties({
	name: 'messages',
	path: '/js/i18n/',
	mode: 'both'
});

$(document).ready(function() {
	$('#hideConvoBtn').click(function() {
		$.ajax({
			url: "/api/conversations/" + blogName + "/byParticipant/" + participant + "/ignoreConvo",
			dataSrc: "",
			type: "PUT"
		}).then(function(data) {
			window.close();
		});
	});
	
	$('#hideConvoAndRefreshBtn').click(function() {
		$.ajax({
			url: "/api/conversations/" + blogName + "/byParticipant/" + participant + "/ignoreConvo",
			dataSrc: "",
			type: "PUT"
		}).then(function(data) {
			opener.location.reload();
			window.close();
		});
	});
});