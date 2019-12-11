var metadataObject;

$.i18n.properties({
	name: 'messages',
	path: 'js/i18n/',
	mode: 'both'
});

/**
 * Download the static data to populate drop-downs; download metadata to
 * populate the form, set up event handlers
 */
$(document).ready(function () {
	$('#header').load("/header");
	$('#footer').load("/footer");
	
	setUIWidgets();

	$.ajax({
		url: "/api/metadata/staticListData",
		method: "GET",
		data: ""
	}).then(function(data) {
		fillDropdownsWithValues(data);
		
		$.ajax({
			url: "/api/metadata",
			data: ""
		}).then(function(data) {
			metadataObject = data;
			$('#baseMediaPath').val(metadataObject.baseMediaPath);
			$('#sortOrderDropdown').val(metadataObject.sortOrder).selectmenu('refresh');
			$('#conversationSortOrderDropdown').val(metadataObject.conversationSortOrder).selectmenu('refresh');
			$('#sortByDropdown').val(metadataObject.sortColumn).selectmenu('refresh');
			$('#conversationSortColumnDropdown').val(metadataObject.conversationSortColumn).selectmenu('refresh');
			$('#filterDropdown').val(metadataObject.filter).selectmenu('refresh');
			$('#favsDropdown').val(metadataObject.favFilter).selectmenu('refresh');
			$('#pageLengthDropdown').val(metadataObject.pageLength).selectmenu('refresh');
			$('#conversationDisplayDropdown').val(metadataObject.conversationDisplayStyle).selectmenu('refresh');
			$('#themesDropdown').val(metadataObject.theme).selectmenu('refresh');
			$('#mainUser').val(metadataObject.mainTumblrUser);
			$('#mainUserAvatarUrl').val(metadataObject.mainTumblrUserAvatarUrl);
			$('#imageExportPath').val(metadataObject.exportImagesFilePath);
			if(metadataObject.showReadingPane == "true") {
				$('#showReadingPaneDropdown').val('true').selectmenu('refresh');
			} else {
				$('#showReadingPaneDropdown').val('false').selectmenu('refresh');
			}
			if(metadataObject.overwritePostData) {
				$('#overwritePostsDropdown').val('true').selectmenu('refresh');
			} else {
				$('#overwritePostsDropdown').val('false').selectmenu('refresh');
			}
			if(metadataObject.overwriteConvoData) {
				$('#overwriteConvosDropdown').val('true').selectmenu('refresh');
			} else {
				$('#overwriteConvosDropdown').val('false').selectmenu('refresh');
			}
			
		});
				
	});
	
	$('#submitButton').click(function() {
		updateServer();
	});
	
	$('#markAllPostsReadButton').click(function() {
		$.ajax({
			url: '/admintools/posts/markAllRead',
			type: 'GET',
			success: function(data, textStatus, xhr) {
				createAnInfoMessage($.i18n.prop('md_admintools_markAllReadSuccess'));
			},
			error: function(xhr, textStatus, errorThrown) {
				createAnErrorMessage($.i18n.prop('md_admintools_markAllReadFailure'));
			}
		});
	});
	
	$('#markAllPostsUnreadButton').click(function() {
		$.ajax({
			url: '/admintools/posts/markAllUnread',
			type: 'GET',
			success: function(data, textStatus, xhr) {
				createAnInfoMessage($.i18n.prop('md_admintools_markAllUnreadSuccess'));
			},
			error: function(xhr, textStatus, errorThrown) {
				createAnErrorMessage($.i18n.prop('md_admintools_markAllUnreadFailure'));
			}
		});
	});
	
	$('#cleanImagesButton').click(function() {
		$.ajax({
			url: '/admintools/posts/cleanImagesOnHD',
			type: 'GET',
			success: function(data, textStatus, xhr) {
				createAnInfoMessage($.i18n.prop('md_admintools_cleanImagesSuccess'));
			},
			error: function(xhr, textStatus, errorThrown) {
				createAnErrorMessage($.i18n.prop('md_admintools_cleanImagesFailure'));
			}
		});
	});
	
	$('#importImagesButton').click(function() {
		if($('#importImagesPath').val().length < 1) {
			createAnErrorMessage($.i18n.prop('md_admintools_importImagesBadPath'));
			return;
		}
		
		$.ajax({
			url: '/admintools/posts/importImages',
			type: 'POST',
			data: $('#importImagesPath').val(),
			async: false,
			contentType: 'text/plain',
			success: function(data, textStatus, xhr) {
				createAnInfoMessage($.i18n.prop('md_admintools_importImagesSuccess'));
			},
			error: function(xhr, textStatus, errorThrown) {
				createAnErrorMessage($.i18n.prop('md_admintools_importImagesFailure'));
			}
		});
	});
	
});

/**
 * Returns a translated name for a column
 * 
 * @param columnName
 *            ID / code name of column
 * @returns User-friendly column name
 */
function getTranslatedNameForColumn(columnName) {
	switch(columnName) {
	case "ID":
		return $.i18n.prop('md_columnnames_id');
		break;
	case "Type":
		return $.i18n.prop('md_columnnames_type');
		break;
	case "Slug":
		return $.i18n.prop('md_columnnames_slug');
		break;
	case "Date":
		return $.i18n.prop('md_columnnames_date');
		break;
	case "Is Read":
		return $.i18n.prop('md_columnnames_isread');
		break;
	case "Is Favourite":
		return $.i18n.prop('md_columnnames_isfavourite');
		break;
	case "State":
		return $.i18n.prop('md_columnnames_state');
		break;
	case "Hashtags":
		return $.i18n.prop('md_columnnames_hashtags');
		break;
	}
}

/**
 * Returns a translated name for a conversation column
 * 
 * @param columnName
 *            ID / code name of column
 * @returns User-friendly column name
 */
function getTranslatedNameForConversationColumn(columnName) {
	switch(columnName) {
	case "participantName":
		return $.i18n.prop('md_convocolumnnames_participant');
		break;
	case "numMessages":
		return $.i18n.prop('md_convocolumnnames_nummessages');
		break;
	}
}

/**
 * Returns a translated text for a sort order value
 * 
 * @param sortOrder
 *            ID / code name of value
 * @returns User-friendly value
 */
function getTranslatedSortOrder(sortOrder) {
	switch(sortOrder) {
	case "Ascending":
		return $.i18n.prop('md_sortorders_asc');
		break;
	case "Descending":
		return $.i18n.prop('md_sortorders_desc');
		break;
	}
}

/**
 * Returns a translated text for a filter type
 * 
 * @param filterType
 *            ID / code name of value
 * @returns User-friendly value
 */
function getTranslatedFilterTypes(filterType) {
	switch(filterType) {
	case "Filter Read Posts":
		return $.i18n.prop('md_filters_filterread');
		break;
	case "Filter Unread Posts":
		return $.i18n.prop('md_filters_filterunread');
		break;
	case "Do not Filter":
		return $.i18n.prop('md_filters_filternothing');
		break;
	}
}

/**
 * Returns a translated text for a favourite filter
 * 
 * @param favFilter
 *            ID / code name of value
 * @returns User-friendly value
 */
function getTranslatedFavFilters(favFilter) {
	switch(favFilter) {
	case "Show Favourites":
		return $.i18n.prop('md_favfilters_showfavourited');
		break;
	case "Show Non Favourites":
		return $.i18n.prop('md_favfilters_shownonfavourited');
		break;
	case "Show Everything":
		return $.i18n.prop('md_favfilters_showeverything');
		break;
	}
}

/**
 * Returns a translated text for a page length value
 * 
 * @param pageLength
 *            ID / code name of value
 * @returns User-friendly value
 */
function getTranslatedPageLength(pageLength) {
	switch(pageLength) {
	case 10:
		return $.i18n.prop('md_pagelengths_10');
		break;
	case 25:
		return $.i18n.prop('md_pagelengths_25');
		break;
	case 50:
		return $.i18n.prop('md_pagelengths_50');
		break;
	case 100:
		return $.i18n.prop('md_pagelengths_100');
		break;
	case -1:
		return $.i18n.prop('md_pagelengths_all');
		break;
	}
}

/**
 * Returns a translated text for a conversation display style
 * 
 * @param conversationStyle
 *            ID / code name of value
 * @returns User-friendly value
 */
function getTranslatedConversationStyle(conversationStyle) {
	switch(conversationStyle) {
	case "cloud":
		return $.i18n.prop('md_conversationStyles_cloud');
		break;
	case "table":
		return $.i18n.prop('md_conversationStyles_table');
		break;
	}
}

/**
 * Returns a translated text for a theme name
 * 
 * @param themeID
 *            ID / code name of value
 * @returns User-friendly value
 */
function getTranslatedTheme(themeID) {
	return $.i18n.prop('md_themes_' + themeID);
}

/**
 * Sets any Themeroller widgets that need to be instantiated (in this case, just
 * select menus)
 */
function setUIWidgets() {
	$('#filterDropdown').selectmenu();
	$('#sortByDropdown').selectmenu();
	$('#sortOrderDropdown').selectmenu();
	$('#favsDropdown').selectmenu();
	$('#pageLengthDropdown').selectmenu();
	$('#showReadingPaneDropdown').selectmenu();
	$('#overwritePostsDropdown').selectmenu();
	$('#overwriteConvosDropdown').selectmenu();
	$('#themesDropdown').selectmenu();
	$('#conversationDisplayDropdown').selectmenu();
	$('#conversationSortColumnDropdown').selectmenu();
	$('#conversationSortOrderDropdown').selectmenu();
}

/**
 * Goes through the static data and uses it to populate all of the drop-downs on
 * the page
 * 
 * @param data
 *            The static list data
 */
function fillDropdownsWithValues(data) {
	$.each(data.sortOrders, function(i, obj) {
		addOptionToSelect(obj, "sortOrderDropdown", getTranslatedSortOrder(obj));
		addOptionToSelect(obj, "conversationSortOrderDropdown", getTranslatedSortOrder(obj));
	});
	$.each(data.sortColumns, function(i, obj) {
		addOptionToSelect(obj, "sortByDropdown", getTranslatedNameForColumn(obj));
	});
	$.each(data.conversationSortColumns, function(i, obj) {
		addOptionToSelect(obj, "conversationSortColumnDropdown", getTranslatedNameForConversationColumn(obj));
	});
	$.each(data.filterTypes, function(i, obj) {
		addOptionToSelect(obj, "filterDropdown", getTranslatedFilterTypes(obj));
	});
	$.each(data.favFilters, function(i, obj) {
		addOptionToSelect(obj, "favsDropdown", getTranslatedFavFilters(obj));
	});
	$.each(data.pageLengths, function(i, obj) {
		addOptionToSelect(obj, "pageLengthDropdown", getTranslatedPageLength(obj));
	});
	$.each(data.conversationStyles, function(i, obj) {
		addOptionToSelect(obj, "conversationDisplayDropdown", getTranslatedConversationStyle(obj));
	});
	$.each(data.themes, function(i, obj) {
		addOptionToSelect(obj, "themesDropdown", getTranslatedTheme(obj));
	});
	addOptionToSelect("true", "showReadingPaneDropdown", $.i18n.prop('md_showReadingPaneYes'))
	addOptionToSelect("false", "showReadingPaneDropdown", $.i18n.prop('md_showReadingPaneNo'))
	
	addOptionToSelect("true", "overwritePostsDropdown", $.i18n.prop('md_overwritePostsYes'))
	addOptionToSelect("false", "overwritePostsDropdown", $.i18n.prop('md_overwritePostsNo'))
	
	addOptionToSelect("true", "overwriteConvosDropdown", $.i18n.prop('md_overwriteConvosYes'));
	addOptionToSelect("false", "overwriteConvosDropdown", $.i18n.prop('md_overwriteConvosNo'));	
}

/**
 * Sends values from the form to the server to update the Metadata
 */
function updateServer() {
	metadataObject.baseMediaPath = $('#baseMediaPath').val();
	metadataObject.mainTumblrUser = $('#mainUser').val();
	metadataObject.mainTumblrUserAvatarUrl = $('#mainUserAvatarUrl').val();
	metadataObject.sortOrder = $('#sortOrderDropdown').val();
	metadataObject.conversationSortOrder = $('#conversationSortOrderDropdown').val();
	metadataObject.sortColumn = $('#sortByDropdown').val();
	metadataObject.conversationSortColumn = $('#conversationSortColumnDropdown').val();
	metadataObject.filter = $('#filterDropdown').val();
	metadataObject.favFilter = $('#favsDropdown').val();
	metadataObject.pageLength = $('#pageLengthDropdown').val();
	metadataObject.showReadingPane = $('#showReadingPaneDropdown').val();
	metadataObject.overwritePostData = $('#overwritePostsDropdown').val();
	metadataObject.overwriteConvoData = $('#overwriteConvosDropdown').val();
	metadataObject.conversationDisplayStyle = $('#conversationDisplayDropdown').val();
	metadataObject.imageExportPath = $('#imageExportPath').val();
	metadataObject.theme = $('#themesDropdown').val();
	
	$.ajax({
		url: '/api/metadata',
		type: 'PUT',
		data: JSON.stringify(metadataObject),
		contentType: 'application/json',
		success: function(data, textStatus, xhr) {
			createAnInfoMessage($.i18n.prop('md_submit_success'));
		},
		error: function(xhr, textStatus, errorThrown) {
			creaeAnErrorMessage($.i18n.prop('md_submit_failure'));
		}
	});	
}