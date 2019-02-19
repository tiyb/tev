
$(document).ready(function() {
	$('#header').load("/header");
	$('#footer').load("/footer");
	
	$.ajax({
		url: "/api/conversations",
		dataSrc: ""
	}).then(function(data) {
		var words = [];
		
		data.forEach(element => words.push({text: element.participant, weight: element.numMessages, handlers: {click: function(item) {window.open("/conversationViewer?participant=" + item.target.textContent, "_blank", "menubar=no,status=no,toolbar=no,height=700,width=1000");}}}));
		
		$('#conversationWordCloudContainer').jQCloud(words, {width:700,height:500,fontSize:{from:0.1,to:0.02},autoResize:true});
	});
	
});