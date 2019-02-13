$(document).ready(function() {
	$('#header').load("/header");
	$('#footer').load("/footer");
	
	$('#typesTable').DataTable( {
		"ajax": {
			"url": "/api/types",
			"dataSrc": ""
		},
		"columns": [
			{"data": "id"},
			{"data": "type"}
		]
	});
});