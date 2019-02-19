var metadataObject;

$(document).ready(function () {
	$('#header').load("/header");
	$('#footer').load("/footer");
	
	$.ajax({
		url: "/api/metadata/staticListData",
		method: "GET",
		data: ""
	}).then(function(data) {
		$.each(data.sortOrders, function(i, obj) {
			var divData = "<option value='" + obj + "'>" + obj + "</option>";
			$(divData).appendTo('#sortOrderDropdown');
		});
		$.each(data.sortColumns, function(i, obj) {
			var divData = "<option value='" + obj + "'>" + obj + "</option>";
			$(divData).appendTo('#sortByDropdown');
		});
		$.each(data.filterTypes, function(i, obj) {
			var divData = "<option value='" + obj + "'>" + obj + "</option>";
			$(divData).appendTo('#filterDropdown');
		});
		
		$.ajax({
			url: "/api/metadata",
			data: ""
		}).then(function(data) {
			metadataObject = data;
			$('#baseMediaPath').val(metadataObject.baseMediaPath);
			$('#sortOrderDropdown').val(metadataObject.sortOrder);
			$('#sortByDropdown').val(metadataObject.sortColumn);
			$('#filterDropdown').val(metadataObject.filter);
			$('#mainUser').val(metadataObject.mainTumblrUser);
			$('#mainUserAvatarUrl').val(metadataObject.mainTumblrUserAvatarUrl);
		});
				
	});
	
	$('#submitButton').click(function() {
		var dataObject = new Object();
		metadataObject.baseMediaPath = $('#baseMediaPath').val();
		metadataObject.mainTumblrUser = $('#mainUser').val();
		metadataObject.mainTumblrUserAvatarUrl = $('#mainUserAvatarUrl').val();
		metadataObject.sortOrder = $('#sortOrderDropdown').val();
		metadataObject.sortColumn = $('#sortByDropdown').val();
		metadataObject.filter = $('#filterDropdown').val();
		
		$.ajax({
			url: '/api/metadata',
			type: 'PUT',
			data: JSON.stringify(metadataObject),
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