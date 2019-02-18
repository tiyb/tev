var typesMap;
var metadata;

$.fn.dataTable.ext.search.push(function(settings, data, dataIndex) {
	var filterVal = metadata.filter;
	if(filterVal == null || filterVal == "") {
		filterVal = "Do not Filter";
	}
	
	if(filterVal == "Do not Filter") {
		return true;
	}
	
	if(filterVal == "Filter Read Posts" && data[4] == "Unread") {
		return true;
	}
	
	if(filterVal == "Filter Unread Posts" && data[4] == "Read") {
		return true;
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
	});
	
	var postTable = $('#postTable').DataTable( {
		"language": {
			"emptyTable": "No posts in the database"
		},
		"orderCellsTop": true,
		"ajax": {
			"url": "api/posts",
			"dataSrc": ""
		},
		"columns": [
			{"data": "id"},
			{
				"data": "type",
				"render": function(data, type, row, meta) {
					return getTypeFromID(data);
				}
			},
			{"data": "slug"},
			{"data": "date"},
			{
				"data": "isRead",
				"render": function(data, type, row, meta) {
					if(data) {
						return "Read";
					} else {
						return "Unread";
					}
				}
			}
		],
		"initComplete": function() {
			$('#postTable tbody').on('click', 'tr', function () {
				var postID = $(this).children('td:first-child').text();
				$(this).children('td:last-child').text("Read");
				postTable.draw();
				$.ajax({
					url: "/api/posts/" + postID + "/markRead",
					type: "PUT"
				});
				window.open("/postViewer?id=" + postID, "_blank", "menubar=no,status=no,toolbar=no,height=700,width=1000");
			});
			$('#postTable').on('order.dt', function() {
				var dataTable = $('#postTable').DataTable();
				var order = dataTable.order();
				updateSortOrderInMD(order[0][0], order[0][1]);
			});
			sortTable();
		}
	});
	
	$('#postTable thead tr').clone(true).appendTo('#postTable thead');
	$('#postTable thead tr:eq(1) th').each(function(i) {
		var title = $(this).text();
		$(this).html('<input type="text" placeholder="Search '+title+'" />');
		
		$('input', this).on('keyup change', function() {
			if(postTable.column(i).search() !== this.value) {
				postTable
					.column(i)
					.search(this.value)
					.draw();
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
		
		postTable.draw();
		updateMDAPI();
	});
	
	$("input[type=radio][value=filterNoValues]").prop('checked', true);
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
	case "Date":
		sortColumn = 3;
		break;
	case "Is Read":
		sortColumn = 4;
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
		metadata.sortColumn = "Date";
		break;
	case 4:
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
			alert("error submitting data");
		}
	});	
}

function getTypeFromID(typeID) {
	return typesMap.get(typeID);
}