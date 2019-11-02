$.i18n.properties({
	name: 'messages',
	path: 'js/i18n/',
	mode: 'both'
});

var imagesExportPath;

$(document).ready(function() {
	$('#header').load("/header");
	$('#footer').load("/footer");
	
	var stagedPostTable = $('#stagedPostsTable').DataTable({
		"language": {
			"emptyTable": 	  $.i18n.prop('staging_posttable_emptytable'),
		    "info":           $.i18n.prop('staging_posttable_info'),
		    "infoEmpty":      $.i18n.prop('staging_posttable_infoempty'),
		    "infoFiltered":   $.i18n.prop('staging_posttable_infofiltered'),
		    "lengthMenu":     $.i18n.prop('staging_posttable_lengthmenu'),
		    "loadingRecords": $.i18n.prop('staging_posttable_loadingrecords'),
		    "processing":     $.i18n.prop('staging_posttable_processing'),
		    "search":         $.i18n.prop('staging_posttable_search'),
		    "zeroRecords":    $.i18n.prop('staging_posttable_zerorecords'),
		    "paginate": {
		        "first":      $.i18n.prop('staging_posttable_paginate_first'),
		        "last":       $.i18n.prop('staging_posttable_paginate_last'),
		        "next":       $.i18n.prop('staging_posttable_paginate_next'),
		        "previous":   $.i18n.prop('staging_posttable_paginate_previous')
		    },
		    "aria": {
		        "sortAscending":  $.i18n.prop('staging_posttable_aria_sortasc'),
		        "sortDescending": $.i18n.prop('staging_posttable_aria_sortdesc')
		    }		
	    },
	    "autoWidth": false,
	    //"lengthMenu": [[10, 25, 50, 100, -1], [$.i18n.prop('md_pagelengths_10'), $.i18n.prop('md_pagelengths_25'), $.i18n.prop('md_pagelengths_50'), $.i18n.prop('md_pagelengths_100'), $.i18n.prop('md_pagelengths_all')]],
		"orderCellsTop": true,
	});
	
	$.ajax({
		url: "/staging-api/posts",
		dataSrc: ""
	}).then(function(data) {
		data.forEach(function(element) {
			$.ajax({
				url: "/api/posts/" + element,
				dataSrc: ""
			}).then(function(postData) {
				var downloadImageButton;
				if(postData.type == "photo") {
					downloadImageButton = "<button class='downloadImagesButton'>" + $.i18n.prop('staging_downloadImagesButtonText') + "</button>";
				} else {
					downloadImageButton = "";
				}
				stagedPostTable.row.add([buildClickableItem(postData.id), buildClickableItem(postData.type), buildClickableItem(postData.slug), downloadImageButton, "<button class='removeBtn'>" + $.i18n.prop('staging_removeButtonText') + "</button>"]).draw();
			});
		});
		
		$('#stagedPostsTable tbody').on('click', 'div[class=clickableTableValue]', function () {
			var postID = $(this).parent().parent().children('td:first-child').text();
			window.open("/postViewer?id=" + postID, "viewer", "menubar=no,status=no,toolbar=no,height=700,width=1000");
		});
		
		$('#stagedPostsTable tbody').on('click', 'button[class=removeBtn]', function() {
			var data = stagedPostTable.row($(this).parents('tr')).data();
			var postID = data[0];
			
			$.ajax({
				url: "/staging-api/posts/" + postID,
				dataSrc: "",
				type: "DELETE"
			}).then(function(data) {
				location.reload();
			});
		});
		
		$('#stagedPostsTable tbody').on('click', 'button[class=downloadImagesButton]', function() {
			var data = stagedPostTable.row($(this).parents('tr')).data();
			var postID = $(data[0]).first().text();
			
			imagesExportPath = prompt("Image Output Path:", imagesExportPath);
			if((imagesExportPath == null) || (imagesExportPath.length < 1)) {
				alert("please enter a destination");
				return;
			}
			
			$.ajax({
				url: "/staging-api/posts/" + postID + "/exportImages",
				type: "POST",
				data: imagesExportPath,
				contentType: "text/plain",
				dataSrc: "",
				success: function(data, textStatus, xhr) {
					alert($.i18n.prop('staging_imageexport_success'));
				},
				error: function(xhr, textStatus, errorThrown) {
					alert($.i18n.prop('staging_imageexport_failure', xhr.responseText));
				}
			});
		});
	});
	
	$('#downloadButton').click(function() {
		window.open("/stagedPostsDownload");
	});
	
	$('#removeAllButton').click(function() {
		$.ajax({
			url: "/staging-api/posts",
			dataSrc: "",
			type: "DELETE"
		}).then(function(data) {
			location.reload();
		});
	});
});

function buildClickableItem(text) {
	return "<div class='clickableTableValue'>" + text + "</div>";
}
