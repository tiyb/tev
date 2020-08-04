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
 * Holds the list of hashtag objects
 */
var htObjects;

/**
 * Initializes the page; gets metadata, gets the Hashtag data, and loads it all
 * into the page
 */
$(document).ready(function() {
	setupUIWidgets();
	
	$.ajax({
		url: '/api/metadata/byBlog/' + getCurrentBlogName(),
		dataSrc: ''
	}).then(function(data) {
		metadata = data;
		
		initRadios();
		
		var theUrl = metadata.showHashtagsForAllBlogs ? "/api/hashtags" : "api/hashtags/" + blogName;
		
		$.ajax({
			url: theUrl,
			dataSrc: ""
		}).then(function (htResponseData) {
            htObjects = htResponseData;
			htData = htResponseData.map(function(val) {
				return val['tag'];
			});
			
			var tagsTable = initializeTableUI();
			loadDataIntoTable(htResponseData, tagsTable);
			addRemoveBtnClickHandlers(tagsTable);
			setupAutoComplete();
		});
	});
	
	addRadioChangeHandler();	
});

/**
 * Initializes radio buttons on the page, based on whether the metadata
 * indicates that all blogs should be shown, or just the current
 */
function initRadios() {
	if (metadata.showHashtagsForAllBlogs == true) {
		$('#showAllBlogsRadio').prop('checked', true).checkboxradio("refresh");
	} else {
		$('#showDefaultBlogRadio').prop('checked', true).checkboxradio("refresh");
	}
}

/**
 * Initializes the DataTable with its UI settings
 * 
 * @returns DataTable object
 */
function initializeTableUI() {
	return $('#tagsTable').DataTable( {
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
		scrollCollapse: true,
		paging: false,
	    autoWidth: false,
		orderCellsTop: true
	});
}

/**
 * Takes data from the array of Hashtag objects and loads it into the table,
 * with appropriate logic around which tags can be removed/clicked (based on
 * whether they appear in multiple blogs).
 * 
 * @param hashtagArray
 *            Array of hashtag objects
 * @param tableObject
 *            Table into which the data should be loaded
 */
function loadDataIntoTable(hashtagArray, tableObject) {
	hashtagArray.forEach(function(element) {
		var tagCell = "";
		var blogCell = element.blog;
		var countCell = element.count;
		var deleteBtnCell = "";
		if (element.blog.includes(", ")) {
			tagCell = buildNonclickableSpan(element.tag);
		} else {
			var tagCell = buildClickableSpan(element.tag);
			deleteBtnCell = "<button class='removeBtn ui-button ui-widget ui-corner-all'>"
					+ $.i18n.prop('htviewer_table_removeBtn')
					+ "</button>";
		}

		tableObject.row.add([ tagCell, blogCell, countCell, deleteBtnCell ]).draw();
	});
}

/**
 * Adds a click event handler to any remove buttons that have been created on
 * the page
 * 
 * @param tableObject
 *            DataTable in which the button exists
 */
function addRemoveBtnClickHandlers(tableObject) {
	$('#tagsTable tbody').on('click', 'button.removeBtn', function() {
		var htObject = tableObject.row($(this).parents('tr')).data();
        var htText = $(htObject[0]).text();
        var htID = -1;
        
        for (i = 0; i < htObjects.length; i++) {
            if (htObjects[i].tag == htText) {
                htID = htObjects[i].id;
                break;
            }
        }
        if (htID == -1) {
            createAnErrorMessage($.i18n.prop('htviewer_deleteht_error', htText));
            return;
        }
		
		var url = "/api/hashtags/" + htID;
		
		$.ajax({
			url: url,
			type: "DELETE",
			error: function(xhr,textStatus,errorThrown) {
				createAnErrorMessage($.i18n.prop('htviewer_deleteht_error', htText));
			}
		}).then(function(data) {
			createAnInfoMessage($.i18n.prop('htviewer_deleteht_success', htText));
			window.location.reload();
		});
	});	
}

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
        createAnInfoMessage($.i18n.prop('htviewer_metadataupdate_success', hashtag.tag));
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
 * Sets up the autocomplete field for new hashtags, first to initialize it with
 * data and then to set up the onChange event to post the new hashtag to the
 * server. Pre-populating the list is only a convenience function, so that the
 * user can see that the HT already exists; choosing an existing item won't
 * change anything.
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
				url: '/api/hashtags/',
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

/**
 * Returns a "hashtagspan" for the given text
 * 
 * @param textToShow
 *            Text to show in the span
 * @returns Span with the appropriate class, showing the text
 */
function buildClickableSpan(textToShow) {
	return "<span class='hashtagspan'>" + textToShow + "</span>";
}

/**
 * Returns a "noclickhashtagspan" for the given text
 * 
 * @param textToShow
 *            Text to show in the span
 * @returns Span with the appropriate class, showing the text
 */
function buildNonclickableSpan(textToShow) {
	return "<span class='noclickhashtagspan'>" + textToShow + "</span>";
}

/**
 * Add change handlers to the Radio buttons
 */
function addRadioChangeHandler() {
	$('input[type=radio][name=showBlogsRadios]').change(function() {
		if(this.id == "showAllBlogsRadio") {
			metadata.showHashtagsForAllBlogs = true;
		} else {
			metadata.showHashtagsForAllBlogs = false;
		}
		
		updateMDAPI();
	});	
}