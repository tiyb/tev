/**
 * Load i18n data
 */
$.i18n.properties({
	name: 'messages',
	path: '/js/i18n/',
	mode: 'both'
});

/**
 * Holds metadata from the server
 */
var metadata;

/**
 * Sets up UI widgets (i.e. sets them up as 'checkbox widgets'); loads
 * settings/metadata from the server; loads data into the table; sets up
 * handlers for the radio buttons
 */
$(document).ready(function() {
	setupUIWidgets();
	
	$.ajax({
		url: '/api/metadata/byBlog/' + getCurrentBlogName(),
		dataSrc: ''
	}).then(function(data) {
		metadata = data;
		
		if(metadata.showHashtagsForAllBlogs) {
			$('#showAllBlogsRadio').prop('checked', true).checkboxradio("refresh");
		} else {
			$('#showDefaultBlogRadio').prop('checked', true).checkboxradio("refresh");
		}
		
		var tagsTable = $('#tagsTable').DataTable( {
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
			"ajax": {
				"url": metadata.showHashtagsForAllBlogs ? "/api/hashtags" : "api/hashtags/" + blogName,
				"dataSrc": ""
			},
			"scrollCollapse": true,
			"paging": false,
			"columns": [
				{
					"data": "tag",
					"render": function(data,type,row,meta) {
						return "<span class='hashtagspan'>" + data + "</span>";
					}
				},
				{
					"data": "count"
				}
			],
		    "autoWidth": false,
			"orderCellsTop": true
		});
	});
	
	$('input[type=radio][name=showBlogsRadios]').change(function() {
		if(this.id == "showAllBlogsRadio") {
			metadata.showHashtagsForAllBlogs = true;
		} else {
			metadata.showHashtagsForAllBlogs = false;
		}
		
		updateMDAPI();
	});
	
	
});

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
	}).then(function(data) {
		window.location.reload();
	});	
}

/**
 * Sets up all radio buttons as "checkbox radio" buttons, per the styles being
 * used
 */
function setupUIWidgets() {
	$("input[type='radio']").checkboxradio();
}
