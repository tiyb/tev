var typesMap;
var tableReadFilter = "no filter";

$.fn.dataTable.ext.search.push(function(settings, data, dataIndex) {
	if(tableReadFilter == "no filter") {
		return true;
	}
	
	if(tableReadFilter == "true" && data[5] == "Read") {
		return true;
	}
	
	if(tableReadFilter == "false" && data[5] == "Unread") {
		return true;
	}
	
	return false;
}
);

$(document).ready(function() {
	$('#header').load("/header");
	$('#footer').load("/footer");
	
	$.ajax({
		url: "/api/types"
	}).then(function(data) {
		typesMap = new Map();
		
		data.forEach(element => typesMap.set(element.id, element.type));
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
	
	var postTable = $('#postTable').DataTable( {
		"language": {
			"emptyTable": "No posts in the database"
		},
		"order": [[0, 'asc']],
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
			})
		}
	});
	
	$('input[type=radio][name=filterRead]').change(function() {
		if(this.id == "filterTrueValues") {
			tableReadFilter = "true";
		} else if (this.id == "filterFalseValues") {
			tableReadFilter = "false";
		} else if(this.id == "filterNoValues") {
			tableReadFilter = "no filter";
		}
		
		postTable.draw();
	});
	
//	$('#postTable tbody').on('click', 'tr', function() {
//		var data = postTable.row(this).data();
//		alert(data[1]);
//	});
	
});

function getTypeFromID(typeID) {
	return typesMap.get(typeID);
}