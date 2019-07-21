$.i18n.properties({
	name: 'messages',
	path: 'js/i18n/',
	mode: 'both'
});

$(document).ready(function() {
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
			"url": "api/hashtags",
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


