var metadataObject;

$.i18n.properties({
	name: 'messages',
	path: 'js/i18n/',
	mode: 'both'
});

$(document).ready(function () {
	$('#header').load("/header");
	$('#footer').load("/footer");
	
	$.ajax({
		url: "/api/metadata/staticListData",
		method: "GET",
		data: ""
	}).then(function(data) {
		$.each(data.sortOrders, function(i, obj) {
			var divData = "<option value='" + obj + "'>" + getTranslatedSortOrder(obj) + "</option>";
			$(divData).appendTo('#sortOrderDropdown');
		});
		$.each(data.sortColumns, function(i, obj) {
			var divData = "<option value='" + obj + "'>" + getTranslatedNameForColumn(obj) + "</option>";
			$(divData).appendTo('#sortByDropdown');
		});
		$.each(data.filterTypes, function(i, obj) {
			var divData = "<option value='" + obj + "'>" + getTranslatedFilterTypes(obj) + "</option>";
			$(divData).appendTo('#filterDropdown');
		});
		$.each(data.favFilters, function(i, obj) {
			var divData = "<option value='" + obj + "'>" + getTranslatedFavFilters(obj) + "</option>";
			$(divData).appendTo('#favsDropdown');
		});
		$.each(data.pageLengths, function(i, obj) {
			var divData = "<option value='" + obj + "'>" + getTranslatedPageLength(obj) + "</option>";
			$(divData).appendTo('#pageLengthDropdown');
		});
		$.each(data.conversationStyles, function(i, obj) {
			var divData = "<option value='" + obj + "'>" + getTranslatedConversationStyle(obj) + "</option>";
			$(divData).appendTo('#conversationDisplayDropdown');
		});
		var divData = "<option value='true'>" + $.i18n.prop('md_showReadingPaneYes') + "</option>";
		$(divData).appendTo('#showReadingPaneDropdown');
		divData = "<option value='false'>" + $.i18n.prop('md_showReadingPaneNo') + "</option>";
		$(divData).appendTo('#showReadingPaneDropdown');
		
		divData = "<option value='true'>" + $.i18n.prop('md_overwritePostsYes') + "</option>";
		$(divData).appendTo('#overwritePostsDropdown');
		divData = "<option value='false'>" + $.i18n.prop('md_overwritePostsNo') + "</option>";
		$(divData).appendTo('#overwritePostsDropdown');
		
		divData = "<option value='true'>" + $.i18n.prop('md_overwriteConvosYes') + "</option>";
		$(divData).appendTo('#overwriteConvosDropdown');
		divData = "<option value='false'>" + $.i18n.prop('md_overwriteConvosNo') + "</option>";
		$(divData).appendTo('#overwriteConvosDropdown');
		
		$.ajax({
			url: "/api/metadata",
			data: ""
		}).then(function(data) {
			metadataObject = data;
			$('#baseMediaPath').val(metadataObject.baseMediaPath);
			$('#sortOrderDropdown').val(metadataObject.sortOrder);
			$('#sortByDropdown').val(metadataObject.sortColumn);
			$('#filterDropdown').val(metadataObject.filter);
			$('#favsDropdown').val(metadataObject.favFilter);
			$('#pageLengthDropdown').val(metadataObject.pageLength);
			$('#conversationDisplayDropdown').val(metadataObject.conversationDisplayStyle);
			$('#mainUser').val(metadataObject.mainTumblrUser);
			$('#mainUserAvatarUrl').val(metadataObject.mainTumblrUserAvatarUrl);
			if(metadataObject.showReadingPane == "true") {
				$('#showReadingPaneDropdown').val('true');
			} else {
				$('#showReadingPaneDropdown').val('false');
			}
			if(metadataObject.overwritePostData) {
				$('#overwritePostsDropdown').val('true');
			} else {
				$('#overwritePostsDropdown').val('false');
			}
			if(metadataObject.overwriteConvoData) {
				$('#overwriteConvosDropdown').val('true');
			} else {
				$('#overwriteConvosDropdown').val('false');
			}
			
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
		metadataObject.favFilter = $('#favsDropdown').val();
		metadataObject.pageLength = $('#pageLengthDropdown').val();
		metadataObject.showReadingPane = $('#showReadingPaneDropdown').val();
		metadataObject.overwritePostData = $('#overwritePostsDropdown').val();
		metadataObject.overwriteConvoData = $('#overwriteConvosDropdown').val();
		metadataObject.conversationDisplayStyle = $('#conversationDisplayDropdown').val();
		
		$.ajax({
			url: '/api/metadata',
			type: 'PUT',
			data: JSON.stringify(metadataObject),
			contentType: 'application/json',
			success: function(data, textStatus, xhr) {
				alert($.i18n.prop('md_submit_success'));
			},
			error: function(xhr, textStatus, errorThrown) {
				alert($.i18n.prop('md_submit_failure'));
			}
		});
	});
	
	$('#markAllPostsReadButton').click(function() {
		$.ajax({
			url: '/admintools/posts/markAllRead',
			type: 'GET',
			success: function(data, textStatus, xhr) {
				alert($.i18n.prop('md_admintools_markAllReadSuccess'));
			},
			error: function(xhr, textStatus, errorThrown) {
				alert($.i18n.prop('md_admintools_markAllReadFailure'));
			}
		});
	});
	
	$('#markAllPostsUnreadButton').click(function() {
		$.ajax({
			url: '/admintools/posts/markAllUnread',
			type: 'GET',
			success: function(data, textStatus, xhr) {
				alert($.i18n.prop('md_admintools_markAllUnreadSuccess'));
			},
			error: function(xhr, textStatus, errorThrown) {
				alert($.i18n.prop('md_admintools_markAllUnreadFailure'));
			}
		});
	});
	
	$('#cleanImagesButton').click(function() {
		$.ajax({
			url: '/admintools/posts/cleanImagesOnHD',
			type: 'GET',
			success: function(data, textStatus, xhr) {
				alert($.i18n.prop('md_admintools_cleanImagesSuccess'));
			},
			error: function(xhr, textStatus, errorThrown) {
				alert($.i18n.prop('md_admintools_cleanImagesFailure'));
			}
		});
	});
});

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
