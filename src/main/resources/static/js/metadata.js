var metadataObject;

$.i18n.properties({
	name: 'messages',
	path: '/js/i18n/',
	mode: 'both'
});

var convoFileUploading = false;
var postFileUploading = true;

var mdReadyExecuted = false;

var FILE_UPLOADING_INTERVAL = 4500;

/**
 * Sends values from the form to the server to update the Metadata
 */
function updateServer() {
    alert("entering update server function"); // TODO
    metadataObject.baseMediaPath = $('#baseMediaPath').val();
    metadataObject.blog = $('#blogNameInput').val();
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
        url: '/api/metadata/' + metadataObject.id,
        method: 'PUT',
        data: JSON.stringify(metadataObject),
        contentType: 'application/json',
        success: function(data, textStatus, xhr) {
            alert("server updated successfully"); // TODO
            createAnInfoMessage($.i18n.prop('md_submit_success'));
        },
        error: function(xhr, textStatus, errorThrown) {
            alert("server not updated successfully"); // TODO
            creaeAnErrorMessage($.i18n.prop('md_submit_failure'));
        }
    }); 
    alert("leaving update server function"); // TODO
}

/**
 * Sets any Themeroller widgets that need to be instantiated (in this case, just
 * select menus), and sets their change event to automatically update the
 * server. Special case is the Themes drop-down, which causes the page to
 * instantly refresh.
 */
function setUIWidgets() {
    $('#filterDropdown').selectmenu({change: function(event,ui) {updateServer();}});
    $('#sortByDropdown').selectmenu({change: function(event,ui) {updateServer();}});
    $('#sortOrderDropdown').selectmenu({change: function(event,ui) {updateServer();}});
    $('#favsDropdown').selectmenu({change: function(event,ui) {updateServer();}});
    $('#pageLengthDropdown').selectmenu({change: function(event,ui) {updateServer();}});
    $('#showReadingPaneDropdown').selectmenu({change: function(event,ui) {updateServer();}});
    $('#overwritePostsDropdown').selectmenu({change: function(event,ui) {updateServer();}});
    $('#overwriteConvosDropdown').selectmenu({change: function(event,ui) {updateServer();}});
    $('#conversationDisplayDropdown').selectmenu({change: function(event,ui) {updateServer();}});
    $('#conversationSortColumnDropdown').selectmenu({change: function(event,ui) {updateServer();}});
    $('#conversationSortOrderDropdown').selectmenu({change: function(event,ui) {updateServer();}});
    $('#themesDropdown').selectmenu({change: function(event,ui) {
        updateServer();
        setTimeout(function() {parent.location.reload();}, 1500);
    }});
}

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
	case "Type":
		return $.i18n.prop('md_columnnames_type');
	case "Slug":
		return $.i18n.prop('md_columnnames_slug');
	case "Date":
		return $.i18n.prop('md_columnnames_date');
	case "Is Read":
		return $.i18n.prop('md_columnnames_isread');
	case "Is Favourite":
		return $.i18n.prop('md_columnnames_isfavourite');
	case "State":
		return $.i18n.prop('md_columnnames_state');
	case "Hashtags":
		return $.i18n.prop('md_columnnames_hashtags');
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
	case "numMessages":
		return $.i18n.prop('md_convocolumnnames_nummessages');
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
	case "Descending":
		return $.i18n.prop('md_sortorders_desc');
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
	case "Filter Unread Posts":
		return $.i18n.prop('md_filters_filterunread');
	case "Do not Filter":
		return $.i18n.prop('md_filters_filternothing');
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
	case "Show Non Favourites":
		return $.i18n.prop('md_favfilters_shownonfavourited');
	case "Show Everything":
		return $.i18n.prop('md_favfilters_showeverything');
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
	case 25:
		return $.i18n.prop('md_pagelengths_25');
	case 50:
		return $.i18n.prop('md_pagelengths_50');
	case 100:
		return $.i18n.prop('md_pagelengths_100');
	case -1:
		return $.i18n.prop('md_pagelengths_all');
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
	case "table":
		return $.i18n.prop('md_conversationStyles_table');
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
	addOptionToSelect("true", "showReadingPaneDropdown", $.i18n.prop('md_showReadingPaneYes'));
	addOptionToSelect("false", "showReadingPaneDropdown", $.i18n.prop('md_showReadingPaneNo'));
	
	addOptionToSelect("true", "overwritePostsDropdown", $.i18n.prop('md_overwritePostsYes'));
	addOptionToSelect("false", "overwritePostsDropdown", $.i18n.prop('md_overwritePostsNo'));
	
	addOptionToSelect("true", "overwriteConvosDropdown", $.i18n.prop('md_overwriteConvosYes'));
	addOptionToSelect("false", "overwriteConvosDropdown", $.i18n.prop('md_overwriteConvosNo'));	
}

/**
 * Displays a periodic message in the message area, to indicate that a file is
 * still uploading. Works with both post and convo files; no error checking done
 * on the param, since it would have been checked earlier in the calling
 * function.
 * 
 * First checks the appropriate flag (convoFileUploading or postFileUploading)
 * to see if the upload is still occurring; if so, a message is displayed, and
 * setTimeout() is used to call this function again in a few seconds (controlled
 * by FILE_UPLOADING_INTERVAL).
 * 
 * @param fileType
 *            Type of file being uploaded, either post or convo
 */
function stillUploadingMessage(fileType) {
    if (fileType === "post") {
        if (postFileUploading === false) {
            return;
        }

        createAnInfoMessage($.i18n
                .prop('md_uploadfile_stilluploading', "Posts"));
        setTimeout(function() {
            stillUploadingMessage("post");
        }, FILE_UPLOADING_INTERVAL);

        return;
    }

    if (convoFileUploading === false) {
        return;
    }

    createAnInfoMessage($.i18n.prop('md_uploadfile_stilluploading',
            "Conversations"));
    setTimeout(function() {
        stillUploadingMessage("convo");
    }, FILE_UPLOADING_INTERVAL);
}

/**
 * Helper function used to asynchronously upload a file to the server (either a
 * post file or a conversation file)
 * 
 * @param url
 *            The URL to which the file should be sent
 * @param fileType
 *            The type of file being uploaded: "post" or "convo"
 */
function uploadFile(fileType, postUploadForm) {
	var url;
	if (fileType === "post") {
		url = '/postDataUpload/' + blogName;
		postFileUploading = true;
	} else if (fileType === "convo") {
		url = '/conversationDataUpload/' + blogName;
		convoFileUploading = true;
	} else {
		createAnErrorMessage("Tehnical error: Invalid file type");
		return;
	}
	
	setTimeout(function() {
		stillUploadingMessage(fileType);
	}, FILE_UPLOADING_INTERVAL);
	
	$.ajax({
		url : url,
		type : 'POST',
		data : new FormData($('#' + postUploadForm)[0]),
		cache : false,
		contentType : false,
		processData : false,
		xhr : function() {
			var myXhr = $.ajaxSettings.xhr();

			if (myXhr.upload) {
				// for handling the progress of the upload
				myXhr.upload.addEventListener('progress',
						function(e) {
							if (e.lengthComputable) {
								createAnInfoMessage($.i18n.prop(
										'md_uploadfile_inprogress', e.loaded,
										e.total));
							}
						}, false);
			}
			return myXhr;
		},
		success : function(data, textStatus) {
			if (fileType === "post") {
				postFileUploading = false;
				$('#postUploadSubmitButton').show();
			} else {
				convoFileUploading = false;
				$('#convoUploadSubmitButton').show();
			}
			createAnInfoMessage($.i18n.prop('md_uploadfile_success'));
		},
		error : function(xhr, textStatus, errorThrown) {
			if (fileType === "post") {
				postFileUploading = false;
			} else {
				convoFileUploading = false;
			}
			createAnErrorMessage($.i18n.prop('md_uploadfile_failure'));
		}
	});

}

/**
 * Download the static data to populate drop-downs; download metadata to
 * populate the form, set up event handlers
 */
$(document).ready(function () {
    if (mdReadyExecuted === true) {
        return;
    }
    
    setUIWidgets();

    $.ajax({
        url: "/api/metadata/staticListData",
        method: "GET",
        data: ""
    }).then(function(data) {
        fillDropdownsWithValues(data);
        
        $.ajax({
            url: "/api/metadata/byBlog/" + blogName,
            data: ""
        }).then(function(data) {
            metadataObject = data;
            $('#baseMediaPath').val(metadataObject.baseMediaPath);
            $('#blogNameInput').val(metadataObject.blog);
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
            if(metadataObject.showReadingPane === "true") {
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
            
            if(metadataObject.isDefault) {
                $('#setDefaultBlogButton').hide();
            } else {
                $('#blogIsDefaultMessage').hide();
                $('#setDefaultBlogButton').click(function() {
                    $.ajax({
                        url: '/api/metadata/' + metadataObject.id + '/markAsDefault',
                        method: 'PUT'
                    }).then(function(data) {
                        createAnInfoMessage($.i18n.prop('md_submit_success'));
                        location.reload();
                    });
                });
            }
        });
        
        $('#headerBlogSelect').selectmenu("disable");   
    });
    
    $.ajax({
        url: '/api/metadata',
        method: 'GET',
        data: ''
    }).then(function(data) {
        if(data.length === 1) {
            $('#deleteBlogButton').hide();
        } else {
            $('#deleteBlogButton').click(function() {
                $.ajax({
                    url: '/api/metadata/' + metadataObject.id,
                    method: 'DELETE',
                    success: function(data, textStatus, hxr) {
                        createAnInfoMessage($.i18n.prop('md_submit_success'));
                        window.location = "/metadata";
                    },
                    error: function(xhr, textStatus, errorThrown) {
                        createAnErrorMessage($.i18n.prop('md_deleteBlog_errorMessage'));
                    }
                });
            });         
        }
    });
    
    $('#markAllPostsReadButton').click(function() {
        $.ajax({
            url: '/admintools/posts/' + metadataObject.blog + '/markAllRead',
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
            url: '/admintools/posts/' + metadataObject.blog + '/markAllUnread',
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
            url: '/admintools/posts/' + metadataObject.blog + '/cleanImagesOnHD',
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
            url: '/admintools/posts/' + metadataObject.blog + '/importImages',
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
    
    $('#postUploadSubmitButton').click(function() {
        $('#postUploadSubmitButton').hide();
        uploadFile("post", "postUploadForm");
        
        return false;
    });
    
    $('#convoUploadSubmitButton').click(function() {
        $('#convoUploadSubmitButton').hide();
        uploadFile("convo", "convoUploadForm");
        
        return false;
    });
    
    alert("about to set change handler");
    $(".autoUpdateSetting").change(function() {
        alert("change event fired"); // TODO remove
        //updateServer();
        alert("finished calling updateServer"); // TODO remove
    });
    alert("change handler was set");
    
    mdReadyExecuted = true;
});
