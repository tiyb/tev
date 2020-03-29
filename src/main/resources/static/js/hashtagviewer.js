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
 * Holds the list of hashtags (names only), for use in autocomplete
 */
var htData;

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
		
		$.ajax({
			url: metadata.showHashtagsForAllBlogs ? "/api/hashtags" : "api/hashtags/" + blogName,
			dataSrc: ""
		}).then(function (htResponseData) {
			htData = htResponseData.map(function(val) {
				return val['tag'];
			});
			
			setupAutoComplete();
			
			var tagsTable = $('#tagsTable').DataTable( {
				language: {
					emptyTable: 	  $.i18n.prop('index_posttable_emptytable'),
				    info:           $.i18n.prop('index_posttable_info'),
				    infoEmpty:      $.i18n.prop('index_posttable_infoempty'),
				    infoFiltered:   $.i18n.prop('index_posttable_infofiltered'),
				    lengthMenu:     $.i18n.prop('index_posttable_lengthmenu'),
				    loadingRecords: $.i18n.prop('index_posttable_loadingrecords'),
				    processing:     $.i18n.prop('index_posttable_processing'),
				    search:         $.i18n.prop('index_posttable_search'),
				    zeroRecords:    $.i18n.prop('index_posttable_zerorecords'),
				    paginate: {
				        first:      $.i18n.prop('index_posttable_paginate_first'),
				        last:       $.i18n.prop('index_posttable_paginate_last'),
				        next:       $.i18n.prop('index_posttable_paginate_next'),
				        previous:   $.i18n.prop('index_posttable_paginate_previous')
				    },
				    aria: {
				        sortAscending:  $.i18n.prop('index_posttable_aria_sortasc'),
				        sortDescending: $.i18n.prop('index_posttable_aria_sortdesc')
				    }		
			    },
			    data: htResponseData,
				scrollCollapse: true,
				paging: false,
				columns: [
					{
						data: "tag",
						render: function(data,type,row,meta) {
							return "<span class='hashtagspan'>" + data + "</span>";
						}
					},
					{
						data: "count"
					},
					{
						render: function(data,type,row,meta) {
							return "<button class='removeBtn ui-button ui-widget ui-corner-all'>" + $.i18n.prop('htviewer_table_removeBtn') + "</button>";
						}
					}
				],
			    autoWidth: false,
				orderCellsTop: true
			});
			
			$('#tagsTable tbody').on('click', 'button.removeBtn', function() {
				var htObject = tagsTable.row($(this).parents('tr')).data();
				console.log(metadata);
				
				var url = "/api/hashtags";
				if (metadata.showHashtagsForAllBlogs) {
					url += "?removeAll=true";
				}
				
				$.ajax({
					url: url,
					data: JSON.stringify(htObject),
					contentType: 'application/json',
					type: "DELETE",
					error: function(xhr,textStatus,errorThrown) {
						createAnErrorMessage($.i18n.prop('htviewer_deleteht_error', htObject.tag));
					}
				}).then(function(data) {
					createAnInfoMessage($.i18n.prop('htviewer_deleteht_success', htObject.tag));
					window.location.reload();
				});
			});
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

/**
 * Sets up the autocomplete field for new hashtags, first to initialize it with data and then to set up the onChange event to post the new hashtag to the server. Pre-populating the list is only a convenience function, so that the user can see that the HT already exists; choosing an existing item won't change anything.
 */
function setupAutoComplete() {
	$('#newTagTextBox').autocomplete({
		source: htData,
		change: function(event, ui) {
			var newHT = $('#newTagTextBox').val();
			if(htData.includes(newHT)) {
				createAnInfoMessage($.i18n.prop('htviewer_newht_exists', newHT));
				return;
			}
			
			$.ajax({
				url: '/api/hashtags/' + getCurrentBlogName(),
				type: 'POST',
				data: newHT,
				contentType: 'text/plain',
				error: function(xhr,textStatus,errorThrown) {
					createAnErrorMessage($.i18n.prop('htviewer_newht_error', newHT));
				}
			}).then(function(data) {
				createAnInfoMessage($.i18n.prop('htviewer_newht_success', newHT));
				window.location.reload();
			});
		}
	});
}
