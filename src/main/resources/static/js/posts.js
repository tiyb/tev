var metadata;

var ID_COLUMN_NO = 0;
var TYPE_COLUMN_NO = 1;
var STATE_COLUMN_NO = -1;
var SLUG_COLUMN_NO = 2;
var HASHTAGS_COLUMN_NO = 3;
var DATE_COLUMN_NO = 4;
var FAV_COLUMN_NO = 5;
var READ_COLUMN_NO = 6;

$.i18n.properties({
	name: 'messages',
	path: 'js/i18n/',
	mode: 'both'
});

$(document).ready(function() {
	$('#header').load("/header");
	$('#footer').load("/footer");
	
	$.ajax({
		url: "/api/metadata",
		dataSrc: ""
	}).then(function(data) {
		metadata = data;
		
		if(metadata.filter == "Filter Read Posts") {
			$("#filterRead").prop('checked', true);
		} else if(metadata.filter == "Filter Unread Posts") {
			$("#filterUnread").prop('checked', true);
		} else {
			$("#filterNoValues").prop('checked', true);
		}
		
		if(metadata.favFilter == "Show Favourites") {
			$('#showFavourites').prop('checked', true);
		} else if(metadata.favFilter == "Show Non Favourites") {
			$('#showNonFavourites').prop('checked', true);
		} else {
			$('#showAll').prop('checked', true);
		}
		
		if(metadata.showReadingPane) {
			$('#showReadingPaneSelected').prop('checked', true);
		} else {
			$('#showPopupsSelected').prop('checked', true);
		}
		
		var postTable = $('#postTable').DataTable( {
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
			"ajax": {
				"url": "api/posts",
				"dataSrc": ""
			},
			"columns": [
				{
					"data": "id",
					"render": function(data,type,row,meta) {
						return "<div class='clickableTableValue'>" + data + "</div>";
					}
				},
				{
					"data": "type",
					"render": function(data, type, row, meta) {
						return "<div class='clickableTableValue'>" + getReadableType(data) + "</div>";
					}
				},
				{
					"data": "slug",
					"render": function(data,type,row,meta) {
						return "<div class='clickableTableValue'>" + data.replace(/\-/g, ' ') + "</div>";
						}
				},
				{
					"data": "tags",
					"render": function(data,type,row,meta) {
						return "<div class='clickableTableValue'>" + data + "</div>";
						}
				},
				{
					"data": "date",
					"render": function(data,type,row,meta) {
						return "<div class='clickableTableValue'>" + data + "</div>";
					}
				},
				{
					"data": "isFavourite",
					"render": function(data, type, row, meta) {
						if(data) {
							return $.i18n.prop('index_posttable_isFavourite');
						} else {
							return $.i18n.prop('index_posttable_isNotFavourite');;
						}
					}
				},
				{
					"data": "isRead",
					"render": function(data, type, row, meta) {
						if(data) {
							return $.i18n.prop('index_posttable_isReadIndicator');
						} else {
							return $.i18n.prop('index_posttable_isNotreadIndicator');
						}
					}
				}
			],
			"initComplete": function() {
				$('#postTable tbody').on('click', 'div[class=clickableTableValue]', function () {
					var postID = $(this).parent().parent().children('td:first-child').text();
					$(this).parent().parent().children('td:last-child').html($.i18n.prop('index_posttable_isReadIndicator'));
					postTable.draw();
					$.ajax({
						url: "/api/posts/" + postID + "/markRead",
						type: "PUT"
					});
					if(metadata.showReadingPane) {
						$('#contentDisplayReadingPane').show();
						$('#displayPaneIFrame').prop('src', "/postViewer?id=" + postID, "viewer");
					} else {
						window.open("/postViewer?id=" + postID, "viewer", "menubar=no,status=no,toolbar=no,height=700,width=1000");
					}
				});
				$('#postTable').on('order.dt', function() {
					var dataTable = $('#postTable').DataTable();
					var order = dataTable.order();
					updateSortOrderInMD(order[0][0], order[0][1]);
				});
				dataTable = $('#postTable').DataTable();
				dataTable.on('length.dt', function(e,settings,len) {
					metadata.pageLength = len;
					updateMDAPI();
				});
				dataTable.page.len(metadata.pageLength);
				sortTable();
			}
		});
		
		$('#postTable tbody').on('click', 'div[class=readSpan]', function() {
			var data = postTable.row( $(this).parents('tr') ).data();
			var postID = data.id;
			$.ajax({
				url: "/api/posts/" + postID + "/markUnread",
				type: "PUT"
			});
			$(this).parent().parent('tr').children('td:last-child').html($.i18n.prop('index_posttable_isNotreadIndicator'));
			$('#displayPaneIFrame').height($('#contentDisplayTable').height());
			postTable.draw();
			return false;
		});
		
		$('#postTable tbody').on('click', 'div[class=notFavSpan]', function() {
			var data = postTable.row($(this).parents('tr')).data();
			var postID = data.id;
			$.ajax({
				url: "/api/posts/" + postID + "/markFavourite",
				type: "PUT"
			});
			$(this).parents('tr').children('td:nth-child(' + (FAV_COLUMN_NO + 1) + ')').html($.i18n.prop('index_posttable_isFavourite'));
			$('#displayPaneIFrame').height($('#contentDisplayTable').height());
			postTable.draw();
			return false;
		});
		
		$('#postTable tbody').on('click', 'div[class=favSpan]', function() {
			var data = postTable.row($(this).parents('tr')).data();
			var postID = data.id;
			$.ajax({
				url: "/api/posts/" + postID + "/markNonFavourite",
				type: "PUT"
			});
			$(this).parents('tr').children('td:nth-child(' + (FAV_COLUMN_NO + 1) + ')').html($.i18n.prop('index_posttable_isNotFavourite'));
			$('#displayPaneIFrame').height($('#contentDisplayTable').height());
			postTable.draw();
			return false;
		});
		
		$('#postTable tfoot tr').clone(true).appendTo('#postTable tfoot');
		$('#postTable tfoot tr:eq(1) th').each(function(i) {
			if(i < FAV_COLUMN_NO) {
				var title = $(this).text();
				$(this).html('<input type="text" placeholder="' + $.i18n.prop('index_search') + title + '" />');
				
				$('input', this).on('keyup change', function() {
					if(postTable.column(i).search() !== this.value) {
						$('#displayPaneIFrame').height($('#contentDisplayTable').height());
						postTable
							.column(i)
							.search(this.value)
							.draw();
					}
				});
			} else {
				$(this).text('');
			}
		});
		
	});
	
	$('input[type=radio][name=filterRead]').change(function() {
		if(this.id == "filterRead") {
			metadata.filter = "Filter Read Posts";
		} else if (this.id == "filterUnread") {
			metadata.filter = "Filter Unread Posts";
		} else if(this.id == "filterNoValues") {
			metadata.filter = "Do not Filter";
		}
		
		$('#displayPaneIFrame').height($('#contentDisplayTable').height());
		postTable.draw();
		updateMDAPI();
	});	
	
	$('input[type=radio][name=showFavs]').change(function() {
		if(this.id == "showFavourites") {
			metadata.favFilter = "Show Favourites";
		} else if (this.id == "showNonFavourites") {
			metadata.favFilter = "Show Non Favourites";
		} else if (this.id == "showAll") {
			metadata.favFilter = "Show Everything";
		}
		
		$('#displayPaneIFrame').height($('#contentDisplayTable').height());
		postTable.draw();
		updateMDAPI();
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
	
	var urlParams = new URLSearchParams(window.location.search);
	if(urlParams.has("hashsearch")) {
		$('#postTable thead tr:eq(1) th:eq(3) input').val(urlParams.get("hashsearch"));
		$('#postTable thead tr:eq(1) th:eq(3) input').change();
	}
	
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

$.fn.dataTable.ext.search.push(function(settings, data, dataIndex) {
	var filterVal = metadata.filter;
	var favFilterVal = metadata.favFilter;
	
	if(filterVal == null || filterVal == "") {
		filterVal = "Do not Filter";
	}
	if(favFilterVal == null || favFilterVal == "") {
		favFilterVal = "Show Everything";
	}
	
	if(filterVal == "Do not Filter") {
		if(favFilterVal == "Show Everything") {
			return true;
		} else if ((favFilterVal == "Show Favourites") && (data[FAV_COLUMN_NO] == $.i18n.prop('index_posttable_isFavouriteCLEAN'))) {
			return true
		} else if((favFilterVal == "Show Non Favourites") && (data[FAV_COLUMN_NO] == $.i18n.prop('index_posttable_isNotFavouriteCLEAN'))) {
			return true;
		}
	}
	
	if((filterVal == "Filter Read Posts") && (data[READ_COLUMN_NO] == $.i18n.prop('index_posttable_isNotreadIndicatorCLEAN'))) {
		if(favFilterVal == "Show Everything") {
			return true;
		} else if ((favFilterVal == "Show Favourites") && (data[FAV_COLUMN_NO] == $.i18n.prop('index_posttable_isFavouriteCLEAN'))) {
			return true
		} else if((favFilterVal == "Show Non Favourites") && (data[FAV_COLUMN_NO] == $.i18n.prop('index_posttable_isNotFavouriteCLEAN'))) {
			return true;
		}
	}
	
	if((filterVal == "Filter Unread Posts") && (data[READ_COLUMN_NO] == $.i18n.prop('index_posttable_isReadIndicatorCLEAN'))) {
		if(favFilterVal == "Show Everything") {
			return true;
		} else if ((favFilterVal == "Show Favourites") && (data[FAV_COLUMN_NO] == $.i18n.prop('index_posttable_isFavouriteCLEAN'))) {
			return true
		} else if((favFilterVal == "Show Non Favourites") && (data[FAV_COLUMN_NO] == $.i18n.prop('index_posttable_isNotFavouriteCLEAN'))) {
			return true;
		}
	}
	
	return false;
});

function sortTable() {
	postTable = $('#postTable').DataTable();
	
	var sortOrder;
	var sortColumn;
	
	if(metadata.sortOrder == "Ascending") {
		sortOrder = "asc";
	} else {
		sortOrder = "desc";
	}
	
	switch(metadata.sortColumn) {
	case "ID":
		sortColumn = ID_COLUMN_NO;
		break;
	case "Type":
		sortColumn = TYPE_COLUMN_NO;
		break;
	case "State":
		sortColumn = STATE_COLUMN_NO;
		break;
	case "Slug":
		sortColumn = SLUG_COLUMN_NO;
		break;
	case "Hashtags":
		sortColumn = HASHTAGS_COLUMN_NO;
		break;
	case "Date":
		sortColumn = DATE_COLUMN_NO;
		break;
	case "Is Favourite":
		sortColumn = FAV_COLUMN_NO;
		break;
	case "Is Read":
		sortColumn = READ_COLUMN_NO;
		break;
	default:
		sortColumn = ID_COLUMN_NO;
	}
	
	$('#displayPaneIFrame').height($('#contentDisplayTable').height());
	postTable.order([sortColumn, sortOrder]).draw();
}

function updateSortOrderInMD(column, order) {
	switch(column) {
	case ID_COLUMN_NO:
		metadata.sortColumn = "ID";
		break;
	case TYPE_COLUMN_NO:
		metadata.sortColumn = "Type";
		break;
	case STATE_COLUMN_NO:
		metadata.sortColumn = "State";
		break;
	case SLUG_COLUMN_NO:
		metadata.sortColumn = "Slug";
		break;
	case HASHTAGS_COLUMN_NO:
		metadata.sortColumn = "Hashtags";
		break;
	case DATE_COLUMN_NO:
		metadata.sortColumn = "Date";
		break;
	case FAV_COLUMN_NO:
		metadata.sortColumn = "Is Favourite";
		break;
	case READ_COLUMN_NO:
		metadata.sortColumn = "Is Read";
		break;
	default:
		metadata.sortColumn = "ID";
		break;
	}
	
	if(order == "asc") {
		metadata.sortOrder = "Ascending";
	} else {
		metadata.sortOrder = "Descending";
	}
	
	updateMDAPI();
}

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

function getReadableType(typeValue) {
	switch(typeValue) {
	case "answer":
		return $.i18n.prop('index_types_answer');
		break;
	case "link":
		return $.i18n.prop('index_types_link');
		break;
	case "photo":
		return $.i18n.prop('index_types_photo');
		break;
	case "regular":
		return $.i18n.prop('index_types_regular');
		break;
	case "video":
		return $.i18n.prop('index_types_video');
		break;
	default:
		return "";
		break;
	}
} 