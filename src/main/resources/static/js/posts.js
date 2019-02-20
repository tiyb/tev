var typesMap;
var metadata;

$.i18n.properties({
	name: 'messages',
	path: 'js/i18n/',
	mode: 'both'
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
		} else if (favFilterVal == "Show Favourites" && data[5] == $.i18n.prop('index_posttable_isFavouriteCLEAN')) {
			return true
		} else if(favFilterVal == "Show Non Favourites" && data[5] == $.i18n.prop('index_posttable_isNotFavouriteCLEAN')) {
			return true;
		}
	}
	
	if(filterVal == "Filter Read Posts" && data[6] == $.i18n.prop('index_posttable_isNotreadIndicatorCLEAN')) {
		if(favFilterVal == "Show Everything") {
			return true;
		} else if (favFilterVal == "Show Favourites" && data[5] == $.i18n.prop('index_posttable_isFavouriteCLEAN')) {
			return true
		} else if(favFilterVal == "Show Non Favourites" && data[5] == $.i18n.prop('index_posttable_isNotFavouriteCLEAN')) {
			return true;
		}
	}
	
	if(filterVal == "Filter Unread Posts" && data[6] == $.i18n.prop('index_posttable_isReadIndicatorCLEAN')) {
		if(favFilterVal == "Show Everything") {
			return true;
		} else if (favFilterVal == "Show Favourites" && data[5] == $.i18n.prop('index_posttable_isFavouriteCLEAN')) {
			return true
		} else if(favFilterVal == "Show Non Favourites" && data[5] == $.i18n.prop('index_posttable_isNotFavouriteCLEAN')) {
			return true;
		}
	}
	
	return false;
});

$(document).ready(function() {
	$('#header').load("/header");
	$('#footer').load("/footer");
	
	$.ajax({
		url: "/api/types",
		dataSrc: ""
	}).then(function(data) {
		typesMap = new Map();
		
		data.forEach(element => typesMap.set(element.id, element.type));
	});
	
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
	});
	
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
		"orderCellsTop": true,
		"ajax": {
			"url": "api/posts",
			"dataSrc": ""
		},
		"columns": [
			{
				"data": "id"
			},
			{
				"data": "type",
				"render": function(data, type, row, meta) {
					return getTypeFromID(data);
				}
			},
			{
				"data": "slug"
			},
			{
				"data": "tags"
			},
			{
				"data": "date"
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
			$('#postTable tbody').on('click', 'tr', function () {
				var postID = $(this).children('td:first-child').text();
				$(this).children('td:last-child').html($.i18n.prop('index_posttable_isReadIndicator'));
				postTable.draw();
				$.ajax({
					url: "/api/posts/" + postID + "/markRead",
					type: "PUT"
				});
				window.open("/postViewer?id=" + postID, "viewer", "menubar=no,status=no,toolbar=no,height=700,width=1000");
			});
			$('#postTable').on('order.dt', function() {
				var dataTable = $('#postTable').DataTable();
				var order = dataTable.order();
				updateSortOrderInMD(order[0][0], order[0][1]);
			});
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
		$(this).parents('tr').children('td:last-child').html($.i18n.prop('index_posttable_isNotreadIndicator'));
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
		$(this).parents('tr').children('td:nth-child(6)').html($.i18n.prop('index_posttable_isFavourite'));
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
		$(this).parents('tr').children('td:nth-child(6)').html($.i18n.prop('index_posttable_isNotFavourite'));
		postTable.draw();
		return false;
	});
	
	$('#postTable thead tr').clone(true).appendTo('#postTable thead');
	$('#postTable thead tr:eq(1) th').each(function(i) {
		if(i < 5) {
			var title = $(this).text();
			$(this).html('<input type="text" placeholder="' + $.i18n.prop('index_search') + title + '" />');
			
			$('input', this).on('keyup change', function() {
				if(postTable.column(i).search() !== this.value) {
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
	
	$('input[type=radio][name=filterRead]').change(function() {
		if(this.id == "filterRead") {
			metadata.filter = "Filter Read Posts";
		} else if (this.id == "filterUnread") {
			metadata.filter = "Filter Unread Posts";
		} else if(this.id == "filterNoValues") {
			metadata.filter = "Do not Filter";
		}
		
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
		
		postTable.draw();
		updateMDAPI();
	});
	
	var urlParams = new URLSearchParams(window.location.search);
	if(urlParams.has("hashsearch")) {
		//$('#postTable thead tr:eq(1) th').children('input[type=text][nth-child(3)').val(urlParams("hashsearch"));
		$('#postTable thead tr:eq(1) th:eq(3) input').val(urlParams.get("hashsearch"));
		$('#postTable thead tr:eq(1) th:eq(3) input').change();
	}
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
		sortColumn = 0;
		break;
	case "Type":
		sortColumn = 1;
		break;
	case "Slug":
		sortColumn = 2;
		break;
	case "Hashtags":
		sortColumn = 3;
		break;
	case "Date":
		sortColumn = 4;
		break;
	case "Is Favourite":
		sortColumn = 5;
		break;
	case "Is Read":
		sortColumn = 6;
		break;
	default:
		sortColumn = 0;
	}
	
	postTable.order([sortColumn, sortOrder]).draw();
}

function updateSortOrderInMD(column, order) {
	switch(column) {
	case 0:
		metadata.sortColumn = "ID";
		break;
	case 1:
		metadata.sortColumn = "Type";
		break;
	case 2:
		metadata.sortColumn = "Slug";
		break;
	case 3:
		metadata.sortColumn = "Hashtags";
		break;
	case 4:
		metadata.sortColumn = "Date";
		break;
	case 5:
		metadata.sortColumn = "Is Favourite";
		break;
	case 6:
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

function getTypeFromID(typeID) {
	return typesMap.get(typeID);
}