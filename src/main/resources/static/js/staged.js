$.i18n.properties({
	name: 'messages',
	path: '/js/i18n/',
	mode: 'both'
});

var metadataObject;

$(document).ready(function() {
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
		"orderCellsTop": true,
	});
	
	$.ajax({
		url: "/api/metadata/default",
		dataSrc: ""
	}).then(function(data) {
		metadataObject = data;
		
		$.ajax({
			url: "/staging-api/posts/" + metadataObject.blog,
			dataSrc: ""
		}).then(function(data) {
			if(data.length < 1) {
				$('#downloadButton').attr('disabled', true).addClass('disabled');
				$('#removeAllButton').attr('disabled', true).addClass('disabled');
			}
			data.forEach(function(element) {
				$.ajax({
					url: "/api/posts/" + metadataObject.blog + "/" + element,
					dataSrc: ""
				}).then(function(postData) {
					var downloadImageButton;
					if(postData.type == "photo") {
						downloadImageButton = "<button class='downloadImagesButton ui-button ui-widget ui-corner-all'>" + $.i18n.prop('staging_downloadImagesButtonText') + "</button>";
					} else {
						downloadImageButton = "";
					}
					stagedPostTable.row.add([buildClickableItem(postData.id), buildClickableItem(postData.type), buildClickableItem(postData.slug), downloadImageButton, "<button class='removeBtn ui-button ui-widget ui-corner-all'>" + $.i18n.prop('staging_removeButtonText') + "</button>"]).draw();
				});
			});
			
			$('#stagedPostsTable tbody').on('click', 'div[class=clickableTableValue]', function () {
				var postID = $(this).parent().parent().children('td:first-child').text();
				window.open("/postViewer?id=" + postID, "viewer", "menubar=no,status=no,toolbar=no,height=700,width=1000");
			});
			
			$('#stagedPostsTable tbody').on('click', 'button.removeBtn', function() {
				var data = stagedPostTable.row($(this).parents('tr')).data();
				var postID = $(data[0]).first().text();
				
				$.ajax({
					url: "/staging-api/posts/" + metadataObject.blog + "/" + postID,
					dataSrc: "",
					type: "DELETE"
				}).then(function(data) {
					location.reload();
				});
			});
			
			$('#stagedPostsTable tbody').on('click', 'button.downloadImagesButton', function() {
				var data = stagedPostTable.row($(this).parents('tr')).data();
				var postID = $(data[0]).first().text();
				
				var imagesExportPath = metadataObject.exportImagesFilePath;
				
				imagesExportPath = prompt($.i18n.prop('staging_imageExort_prompt'), imagesExportPath);
				if((imagesExportPath == null) || (imagesExportPath.length < 1)) {
					createAnErrorMessage($.i18n.prop('staging_imageExort_error'));
					return;
				}
				
				$.ajax({
					url: "/staging-api/posts/" + metadataObject.blog + "/" + postID + "/exportImages",
					type: "POST",
					data: imagesExportPath,
					contentType: "text/plain",
					dataSrc: "",
					success: function(data, textStatus, xhr) {
						createAnInfoMessage($.i18n.prop('staging_imageexport_success'));
					},
					error: function(xhr, textStatus, errorThrown) {
						createAnErrorMessage($.i18n.prop('staging_imageexport_failure', xhr.responseText));
					}
				});
				
				if(metadataObject.exportImagesFilePath != imagesExportPath) {
					metadataObject.exportImagesFilePath = imagesExportPath;
					
					$.ajax({
						url: '/api/metadata/' + metadataObject.id,
						type: 'PUT',
						data: JSON.stringify(metadataObject),
						contentType: 'application/json'
					});
				}
			});
		});
		
	});
	
	$('#downloadButton').click(function() {
		window.open("/exportViewer");
	});
	
	$('#removeAllButton').click(function() {
		$.ajax({
			url: "/staging-api/posts/" + metadataObject.blog,
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
