$.i18n.properties({
	name: 'messages',
	path: 'js/i18n/',
	mode: 'both'
});

$(document).ready(function() {
	$('#hideConvoBtn').click(function() {
		$.ajax({
			url: "/api/conversations/" + participant + "/ignoreConvo",
			dataSrc: "",
			type: "PUT"
		}).then(function(data) {
			alert($.i18n.prop('convo_markedConvoHidden'));
		});
	});
});