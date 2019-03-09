$.i18n.properties({
	name: 'messages',
	path: 'js/i18n/',
	mode: 'both'
});

$(document).ready(function() {
	$('#header').load("/header");
	$('#footer').load("/footer");
	
	$('#unhideAllConversationsBtn').click(function() {
		$.ajax({
			url: "/api/conversations/unignoreAllConversations",
			dataSrc: ""
		}).then(function(data) {
			alert($.i18n.prop('convos_markAllUnhidden'));
			location.reload();
		});
	});
	
	$.ajax({
		url: "/api/conversations/unhidden",
		dataSrc: ""
	}).then(function(data) {
		var words = [];
		
		data.forEach(element => words.push({
			text: element.participant, 
			weight: element.numMessages, 
			handlers: {
				click: function(item) {
					window.open("/conversationViewer?participant=" + item.target.textContent, "viewer", "menubar=no,status=no,toolbar=no,height=700,width=1000");
				}
			}
		}));
		
		$('#conversationWordCloudContainer').jQCloud(words, {width:900,height:500,fontSize:{from:0.1,to:0.02},autoResize:true});
	});
	
});