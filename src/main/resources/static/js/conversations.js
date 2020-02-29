/**
 * Holder variable for metadata information
 */
var metadata;

/**
 * Columns in the conversation table
 */
var PARTICIPANT_COLUMN_NO = 0;
var NUMMESSAGES_COLUMN_NO = 1;


$.i18n.properties({
	name: 'messages',
	path: '/js/i18n/',
	mode: 'both'
});

$(document).ready(function() {
	setUIWidgets();
	
	// load the metadata from the server, populate the radio buttons, and show
	// the table or reading pane accordingly
	$.ajax({
		url: "/api/metadata/byBlog/" + getCurrentBlogName(),
		dataSrc: ""
	}).then(function(data) {
		metadata = data;
		
		if(metadata.showReadingPane) {
			$('#showReadingPaneSelected').prop('checked', true).checkboxradio("refresh");
		} else {
			$('#showPopupsSelected').prop('checked', true).checkboxradio("refresh");
		}
		if(metadata.conversationDisplayStyle == "cloud") {
			$('#showCloudSelected').prop('checked', true).checkboxradio("refresh");
			$('#conversationWordCloudContainerContainer').show();
			$('#conversationTableContainer').hide();
		} else {
			$('#showTableSelected').prop('checked', true).checkboxradio("refresh");
			$('#conversationWordCloudContainerContainer').hide();
			$('#conversationTableContainer').show();
		}
		
		// load the conversations from the server
		$.ajax({
			url: "/api/conversations/" + metadata.blog + "/unhidden",
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
							$('#displayPaneIFrame').prop('src', "/conversationViewer?participant=" + item.target.textContent + "&blog=" + metadata.blog, "viewer");
						} else {
							window.open("/conversationViewer?participant=" + item.target.textContent + "&blog=" + metadata.blog, "viewer", "menubar=no,status=no,toolbar=no,height=700,width=1000");
						}
					}
				}
			}));
			
			$('#conversationWordCloudContainer').jQCloud(words, {width:900,height:500,fontSize:{from:0.01,to:0.002},autoResize:false,removeOverflowing:true});
			
			var convoTable = $('#conversationTable').DataTable( {
				"language": {
					"emptyTable": 	  $.i18n.prop('index_posttable_emptytable'),
				    "info":           $.i18n.prop('index_posttable_info'),
				    "infoEmpty":      $.i18n.prop('index_posttable_infoempty'),
				    "infoFiltered":   $.i18n.prop('index_posttable_infofiltered'),
				    "lengthMenu":     $.i18n.prop('index_posttable_lengthmenu'),
				    "loadingRecords": $.i18n.prop('index_posttable_loadingrecords'),
				    "processing":     $.i18n.prop('index_posttable_processing'),
				    "search":         $.i18n.prop('index_posttable_search'),
				    "zeroRecords":    $.i18n.prop('index_posttable_zerorecords'),
				    "paginate": {
				        "first":      $.i18n.prop('index_posttable_paginate_first'),
				        "last":       $.i18n.prop('index_posttable_paginate_last'),
				        "next":       $.i18n.prop('index_posttable_paginate_next'),
				        "previous":   $.i18n.prop('index_posttable_paginate_previous')
				    },
				    "aria": {
				        "sortAscending":  $.i18n.prop('index_posttable_aria_sortasc'),
				        "sortDescending": $.i18n.prop('index_posttable_aria_sortdesc')
				    }		
			    },
			    "autoWidth": false,
			    "lengthMenu": [[10, 25, 50, 100, -1], [$.i18n.prop('md_pagelengths_10'), $.i18n.prop('md_pagelengths_25'), $.i18n.prop('md_pagelengths_50'), $.i18n.prop('md_pagelengths_100'), $.i18n.prop('md_pagelengths_all')]],
				"orderCellsTop": true,
				"data": data,
				"columns": [
					{
						"data": "participant",
						"render": function(data,type,row,meta) {
							return "<div class='clickableTableValue'>" + data + "</div>";
						}
					},
					{
						"data": "numMessages"
					}
				],
				"initComplete": function() {
					$('#conversationTable tbody').on('click', 'div[class=clickableTableValue]', function () {
						var participant = $(this).parent().parent().children('td:first-child').text();
						if(metadata.showReadingPane) {
							$('#contentDisplayReadingPane').show();
							$('#displayPaneIFrame').prop('src', "/conversationViewer?participant=" + participant + "&blog=" + metadata.blog, "viewer");
						} else {
							window.open("/conversationViewer?participant=" + participant + "&blog=" + metadata.blog, "viewer", "menubar=no,status=no,toolbar=no,height=700,width=1000");
						}
					});
					$('#conversationTable').on('order.dt', function() {
						var dataTable = $('#conversationTable').DataTable();
						var order = dataTable.order();
						updateSortOrderInMD(order[0][0], order[0][1]);
					});
					sortTable();
				}
			});
		
	});
		
	$('#unhideAllConversationsBtn').click(function() {
		$.ajax({
			url: "/api/conversations/" + metadata.blog + "/unignoreAllConversations",
			dataSrc: ""
		}).then(function(data) {
			createAnInfoMessage($.i18n.prop('convos_markAllUnhidden'));
			location.reload();
		});
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
	
	$('input[type=radio][name=chooseDisplayStyle]').change(function() {
		if(this.id == "showCloudSelected") {
			metadata.conversationDisplayStyle = "cloud";
			$('#conversationWordCloudContainerContainer').show();
			$('#conversationTableContainer').hide();			
		} else {
			metadata.conversationDisplayStyle = "table";
			$('#conversationWordCloudContainerContainer').hide();
			$('#conversationTableContainer').show();			
		}
		
		updateMDAPI();
	});
	
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

/**
 * Used to sort the table, based on metadata and/or user selection
 */
function sortTable() {
	conversationTable = $('#conversationTable').DataTable();
	
	var sortOrder;
	var sortColumn;
	
	if(metadata.conversationSortOrder == "Ascending") {
		sortOrder = "asc";
	} else {
		sortOrder = "desc";
	}
	
	switch(metadata.conversationSortColumn) {
	case "participantName":
		sortColumn = PARTICIPANT_COLUMN_NO;
		break;
	case "numMessages":
		sortColumn = NUMMESSAGES_COLUMN_NO;
		break;
	default:
		sortColumn = PARTICIPANT_COLUMN_NO;
	}
	
	conversationTable.order([sortColumn, sortOrder]).draw();
}

/**
 * Update server metadata with updated sort order (based on user selection)
 * 
 * @param column
 *            The ID of the column to be sorted
 * @param order
 *            The sort order (ascending or descending)
 */
function updateSortOrderInMD(column, order) {
	switch(column) {
	case PARTICIPANT_COLUMN_NO:
		metadata.conversationSortColumn = "participantName";
		break;
	case NUMMESSAGES_COLUMN_NO:
		metadata.conversationSortColumn = "numMessages";
		break;
	default:
		metadata.sortColumn = "participantName";
		break;
	}
	
	if(order == "asc") {
		metadata.conversationSortOrder = "Ascending";
	} else {
		metadata.conversationSortOrder = "Descending";
	}
	
	updateMDAPI();
}

/**
 * Send updated metadata to the server
 */
function updateMDAPI() {
	$.ajax({
		url: '/api/metadata/' + metadata.id,
		type: 'PUT',
		data: JSON.stringify(metadata),
		contentType: 'application/json',
		error: function(xhr, textStatus, errorThrown) {
			createAnErrorMessage($.i18n.prop('index_errorsubmittingdata'));
		}
	});	
}

/**
 * Sets up any Themeroller widgets that need to be pre-set
 */
function setUIWidgets() {
	$("input[type='radio']").checkboxradio();
}