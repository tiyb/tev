$(document).ready(function () {
	$('#header').load("/header");
	$('#footer').load("/footer");
	
	$.ajax({
		url: "/api/metadata"
	}).then(function(data) {
		$('#baseMediaPath').val(data.baseMediaPath);
	});
	
	$('#submitButton').click(function() {
		var dataObject = new Object();
		dataObject.id = 1;
		dataObject.baseMediaPath = $('#baseMediaPath').val();
		
		$.ajax({
			url: '/api/metadata',
			type: 'PUT',
			data: JSON.stringify(dataObject),
			contentType: 'application/json',
			success: function(data, textStatus, xhr) {
				alert("data successfully submitted");
			},
			error: function(xhr, textStatus, errorThrown) {
				alert("error submitting data");
			}
		});
	});	
});