var metadata;

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
		url: "/api/metadata",
		dataSrc: ""
	}).then(function(data) {
		metadata = data;
		
		if(metadata.showReadingPane) {
			$('#showReadingPaneSelected').prop('checked', true);
		} else {
			$('#showPopupsSelected').prop('checked', true);
		}
		
	});
		
	$('input[type=radio][name=showReadingPaneRadio]').change(function() {
		if(this.id == "showReadingPaneSelected") {
			metadata.showReadingPane = true;
		} else {
			$('#contentDisplayReadingPane').hide();
			metadata.showReadingPane = false;
		}
		
		updateMDAPI();
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
					if(metadata.showReadingPane) {
						$('#contentDisplayReadingPane').show();
						$('#displayPaneIFrame').prop('src', "/conversationViewer?participant=" + item.target.textContent, "viewer");
					} else {
						window.open("/conversationViewer?participant=" + item.target.textContent, "viewer", "menubar=no,status=no,toolbar=no,height=700,width=1000");
					}
				}
			}
		}));
		
		$('#conversationWordCloudContainer').jQCloud(words, {width:900,height:500,fontSize:{from:0.1,to:0.02},autoResize:false,removeOverflowing:false});
	});
	
	var iframeOffset = $('#displayPaneIFrame').offset();
	$(window).scroll(function() {
		var scrollTop = $(window).scrollTop();
		
		if(iframeOffset.top < scrollTop) {
			$('#displayPaneIFrame').addClass('fixed');
		} else {
			$('#displayPaneIFrame').removeClass('fixed');
		}
	});
});

function updateMDAPI() {
	$.ajax({
		url: '/api/metadata',
		type: 'PUT',
		data: JSON.stringify(metadata),
		contentType: 'application/json',
		error: function(xhr, textStatus, errorThrown) {
			alert($.i18n.prop('index_errorsubmittingdata'));
		}
	});	
}

