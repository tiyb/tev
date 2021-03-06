var metadata;

/**
 * IDs for the columns in the table; used for various purposes
 */
var ID_COLUMN_NO = 0;
var TYPE_COLUMN_NO = 1;
var STATE_COLUMN_NO = 2;
var SLUG_COLUMN_NO = 3;
var HASHTAGS_COLUMN_NO = 4;
var DATE_COLUMN_NO = 5;
var FAV_COLUMN_NO = 6;
var READ_COLUMN_NO = 7;

/**
 * Sends updated metadata to the server via REST
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
    }); 
}

/**
 * Indicator as to whether the "additional options" are showing
 */
var additionalOptionsShowing = false;

$.i18n.properties({
	name: 'messages',
	path: '/js/i18n/',
	mode: 'both'
});

/**
 * Logic for filtering the data in the table, by read/unread and
 * favourite/non-favourite.
 */
$.fn.dataTable.ext.search.push(function(settings, data, dataIndex) {
	var filterVal = metadata.filter;
	var favFilterVal = metadata.favFilter;
	
	if(filterVal === null || filterVal === "") {
		filterVal = "Do not Filter";
	}
	if(favFilterVal === null || favFilterVal === "") {
		favFilterVal = "Show Everything";
	}
	
	if(filterVal === "Do not Filter") {
		if(favFilterVal === "Show Everything") {
			return true;
		} else if ((favFilterVal === "Show Favourites") && (data[FAV_COLUMN_NO] === $.i18n.prop('index_posttable_isFavouriteCLEAN'))) {
			return true;
		} else if((favFilterVal === "Show Non Favourites") && (data[FAV_COLUMN_NO] === $.i18n.prop('index_posttable_isNotFavouriteCLEAN'))) {
			return true;
		}
	}
	
	if((filterVal === "Filter Read Posts") && (data[READ_COLUMN_NO] === $.i18n.prop('index_posttable_isNotreadIndicatorCLEAN'))) {
		if(favFilterVal === "Show Everything") {
			return true;
		} else if ((favFilterVal === "Show Favourites") && (data[FAV_COLUMN_NO] === $.i18n.prop('index_posttable_isFavouriteCLEAN'))) {
			return true;
		} else if((favFilterVal === "Show Non Favourites") && (data[FAV_COLUMN_NO] === $.i18n.prop('index_posttable_isNotFavouriteCLEAN'))) {
			return true;
		}
	}
	
	if((filterVal === "Filter Unread Posts") && (data[READ_COLUMN_NO] === $.i18n.prop('index_posttable_isReadIndicatorCLEAN'))) {
		if(favFilterVal === "Show Everything") {
			return true;
		} else if ((favFilterVal === "Show Favourites") && (data[FAV_COLUMN_NO] === $.i18n.prop('index_posttable_isFavouriteCLEAN'))) {
			return true;
		} else if((favFilterVal === "Show Non Favourites") && (data[FAV_COLUMN_NO] === $.i18n.prop('index_posttable_isNotFavouriteCLEAN'))) {
			return true;
		}
	}
	
	return false;
});

/**
 * Figures out the correct sort order, and then has the DataTable sort itself
 * accordingly
 */
function sortTable() {
	postTable = $('#postTable').DataTable();
	
	var sortOrder;
	var sortColumn;
	
	if(metadata.sortOrder === "Ascending") {
		sortOrder = "asc";
	} else {
		sortOrder = "desc";
	}
	
	switch(metadata.sortColumn) {
	case "ID":
		sortColumn = ID_COLUMN_NO;
		break;
	case "Type":
		sortColumn = TYPE_COLUMN_NO;
		break;
	case "State":
		sortColumn = STATE_COLUMN_NO;
		break;
	case "Slug":
		sortColumn = SLUG_COLUMN_NO;
		break;
	case "Hashtags":
		sortColumn = HASHTAGS_COLUMN_NO;
		break;
	case "Date":
		sortColumn = DATE_COLUMN_NO;
		break;
	case "Is Favourite":
		sortColumn = FAV_COLUMN_NO;
		break;
	case "Is Read":
		sortColumn = READ_COLUMN_NO;
		break;
	default:
		sortColumn = ID_COLUMN_NO;
	}
	
	$('#displayPaneIFrame').height($('#contentDisplayTable').height());
	postTable.order([sortColumn, sortOrder]).draw();
}

/**
 * Updates Metadata with new sort order, and then sends the update to the server
 * via REST
 * 
 * @param column
 *            The new column chosen for sorting
 * @param order
 *            The order (asc/desc) for sorting the column
 */
function updateSortOrderInMD(column, order) {
	switch(column) {
	case ID_COLUMN_NO:
		metadata.sortColumn = "ID";
		break;
	case TYPE_COLUMN_NO:
		metadata.sortColumn = "Type";
		break;
	case STATE_COLUMN_NO:
		metadata.sortColumn = "State";
		break;
	case SLUG_COLUMN_NO:
		metadata.sortColumn = "Slug";
		break;
	case HASHTAGS_COLUMN_NO:
		metadata.sortColumn = "Hashtags";
		break;
	case DATE_COLUMN_NO:
		metadata.sortColumn = "Date";
		break;
	case FAV_COLUMN_NO:
		metadata.sortColumn = "Is Favourite";
		break;
	case READ_COLUMN_NO:
		metadata.sortColumn = "Is Read";
		break;
	default:
		metadata.sortColumn = "ID";
		break;
	}
	
	if(order === "asc") {
		metadata.sortOrder = "Ascending";
	} else {
		metadata.sortOrder = "Descending";
	}
	
	updateMDAPI();
}

/**
 * Gets a user-friendly value for a post type
 * 
 * @param typeValue
 *            The ID/code for the post type
 * @returns A user-friendly string of the post type
 */
function getReadableType(typeValue) {
	switch(typeValue) {
	case "answer":
		return $.i18n.prop('index_types_answer');
	case "link":
		return $.i18n.prop('index_types_link');
	case "photo":
		return $.i18n.prop('index_types_photo');
	case "regular":
		return $.i18n.prop('index_types_regular');
	case "video":
		return $.i18n.prop('index_types_video');
	default:
		return "";
	}
} 



/**
 * Helper function for getting a formatted date, appropriate for sorting the
 * data in the table (which is different from a date in the user's locale). Very
 * manual in nature, but a good JavaScript-based alternative wasn't found.
 * 
 * @param inputDate
 *            The date to be formatted
 * @returns A date formatted as YYYY-MM-DD HH:MM:SS, in that specific order
 */
function getFormattedDate(inputDate) {
	var newDate = new Date(inputDate);
	/*jshint -W053*/
	var formattedDate = newDate.getFullYear() + "-" +
			new String(newDate.getMonth() + 1).padStart(2, '0') + "-" +
			new String(newDate.getDate() + 1).padStart(2, '0') + " " +
			new String(newDate.getHours() + 1).padStart(2, '0') + ":" +
			new String(newDate.getMinutes() + 1).padStart(2, '0') + ":" +
			new String(newDate.getSeconds() + 1).padStart(2, '0');
	return formattedDate;
}

/**
 * Sets any Themeroller widgets that need to be instantiated
 */
function setUIWidgets() {
	$("input[type='radio']").checkboxradio();
}

/**
 * Helper function to generate a URL for the viewer post, taking into account 
 * whether the main blog is selected or an alternate is selected.
 *
 * @param blogName Name of default blog
 * @param postID   ID of post to view
 * @returns A string with a URL for the post viewer
 */
function getUrlForItem(blogName, postID) {
    var postUrl = "/postViewer/" + blogName + "?id=" + postID;
    
    var urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has("tempBlogName")) {
        postUrl = "/postViewer/" + urlParams.get("tempBlogName") + "?id=" + postID;
    } else {
        postUrl = "/postViewer/" + blogName + "?id=" + postID;
    }
    
    return postUrl;
}

/**
 * Set up Themeroller widgets; load metadata from the server (to set radio
 * buttons to their defaults); initialize DataTable (which will download its own
 * data, via REST); set up event handlers; check to see if an external search
 * was performed (via URL) and filter table accordingly.
 */
$(document).ready(function() {
    setUIWidgets();
    
    $.ajax({
        url: "/api/metadata/byBlog/" + getCurrentBlogName(),
        dataSrc: ""
    }).then(function(data) {
        metadata = data;
        
        if(metadata.filter === "Filter Read Posts") {
            $("#filterRead").prop('checked', true).checkboxradio("refresh");
        } else if(metadata.filter === "Filter Unread Posts") {
            $("#filterUnread").prop('checked', true).checkboxradio("refresh");
        } else {
            $("#filterNoValues").prop('checked', true).checkboxradio("refresh");
        }
        
        if(metadata.favFilter === "Show Favourites") {
            $('#showFavourites').prop('checked', true).checkboxradio("refresh");
        } else if(metadata.favFilter === "Show Non Favourites") {
            $('#showNonFavourites').prop('checked', true).checkboxradio("refresh");
        } else {
            $('#showAll').prop('checked', true).checkboxradio("refresh");
        }
        
        if(metadata.showReadingPane) {
            $('#showReadingPaneSelected').prop('checked', true).checkboxradio("refresh");
        } else {
            $('#showPopupsSelected').prop('checked', true).checkboxradio("refresh");
        }
        
        var postTable = $('#postTable').DataTable( {
            "language": {
                "emptyTable":     $.i18n.prop('index_posttable_emptytable'),
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
            "autoWidth": false,
            "lengthMenu": [[10, 25, 50, 100, -1], [$.i18n.prop('md_pagelengths_10'), $.i18n.prop('md_pagelengths_25'), $.i18n.prop('md_pagelengths_50'), $.i18n.prop('md_pagelengths_100'), $.i18n.prop('md_pagelengths_all')]],
            "orderCellsTop": true,
            "ajax": {
                "url": "api/posts/" + metadata.blog,
                "dataSrc": ""
            },
            "columns": [
                {
                    "data": "id",
                    "render": function(data,type,row,meta) {
                        return "<div class='clickableTableValue'>" + data + "</div>";
                    }
                },
                {
                    "data": "type",
                    "render": function(data, type, row, meta) {
                        return "<div class='clickableTableValue'>" + getReadableType(data) + "</div>";
                    }
                },
                {
                    "data": "state",
                    "render": function(data,type,row,meta) {
                        return "<div class='clickableTableValue'>" + data + "</div>";
                    }
                },
                {
                    "data": "slug",
                    "render": function(data,type,row,meta) {
                        return "<div class='clickableTableValue'>" + data.replace(/\-/g, ' ') + "</div>";
                        }
                },
                {
                    "data": "tags",
                    "render": function(data,type,row,meta) {
                        return "<div class='clickableTableValue'>" + data + "</div>";
                        }
                },
                {
                    "data": "date",
                    "render": function(data,type,row,meta) {
                        return "<div class='clickableTableValue'>" + getFormattedDate(data) + "</div>";
                    }
                },
                {
                    "data": "isFavourite",
                    "render": function(data, type, row, meta) {
                        if(data) {
                            return $.i18n.prop('index_posttable_isFavourite');
                        } else {
                            return $.i18n.prop('index_posttable_isNotFavourite');
                        }
                    }
                },
                {
                    "data": "isRead",
                    "render": function(data, type, row, meta) {
                        if(data) {
                            return $.i18n.prop('index_posttable_isReadIndicator');
                        } else {
                            return $.i18n.prop('index_posttable_isNotreadIndicator');
                        }
                    }
                }
            ],
            "initComplete": function() {
                $('#postTable tbody').on('click', 'div[class=clickableTableValue]', function () {
                    var postID = $(this).parent().parent().children('td:first-child').text();
                    $(this).parent().parent().children('td:last-child').html($.i18n.prop('index_posttable_isReadIndicator'));
                    postTable.draw();
                    $.ajax({
                        url: "/api/posts/" + metadata.blog + "/" + postID + "/markRead",
                        type: "GET"
                    });
                    if(metadata.showReadingPane) {
                        $('#contentDisplayReadingPane').show();
                        $('#displayPaneIFrame').prop('src', getUrlForItem(metadata.blog, postID), "viewer");
                    } else {
                        window.open(getUrlForItem(metadata.blog, postID), "viewer", "menubar=no,status=no,toolbar=no,height=700,width=1000");
                    }
                });
                $('#postTable').on('order.dt', function() {
                    var dataTable = $('#postTable').DataTable();
                    var order = dataTable.order();
                    updateSortOrderInMD(order[0][0], order[0][1]);
                });
                dataTable = $('#postTable').DataTable();
                dataTable.on('length.dt', function(e,settings,len) {
                    metadata.pageLength = len;
                    updateMDAPI();
                });
                dataTable.page.len(metadata.pageLength);
                sortTable();
            }
        });
        
        $('#postTable tbody').on('click', 'div[class=readSpan]', function() {
            var data = postTable.row( $(this).parents('tr') ).data();
            var postID = data.id;
            $.ajax({
                url: "/api/posts/" + metadata.blog + "/" + postID + "/markUnread",
                type: "GET"
            });
            $(this).parent().parent('tr').children('td:last-child').html($.i18n.prop('index_posttable_isNotreadIndicator'));
            $('#displayPaneIFrame').height($('#contentDisplayTable').height());
            postTable.draw();
            return false;
        });
        
        $('#postTable tbody').on('click', 'div[class=notFavSpan]', function() {
            var data = postTable.row($(this).parents('tr')).data();
            var postID = data.id;
            $.ajax({
                url: "/api/posts/" + metadata.blog + "/" + postID + "/markFavourite",
                type: "GET"
            });
            $(this).parents('tr').children('td:nth-child(' + (FAV_COLUMN_NO + 1) + ')').html($.i18n.prop('index_posttable_isFavourite'));
            $('#displayPaneIFrame').height($('#contentDisplayTable').height());
            postTable.draw();
            return false;
        });
        
        $('#postTable tbody').on('click', 'div[class=favSpan]', function() {
            var data = postTable.row($(this).parents('tr')).data();
            var postID = data.id;
            $.ajax({
                url: "/api/posts/" + metadata.blog + "/" + postID + "/markNonFavourite",
                type: "GET"
            });
            $(this).parents('tr').children('td:nth-child(' + (FAV_COLUMN_NO + 1) + ')').html($.i18n.prop('index_posttable_isNotFavourite'));
            $('#displayPaneIFrame').height($('#contentDisplayTable').height());
            postTable.draw();
            return false;
        });
        
        $('#postTable tfoot tr').clone(true).appendTo('#postTable tfoot');
        $('#postTable tfoot tr:eq(1) th').each(function(i) {
            if(i < FAV_COLUMN_NO) {
                var title = $(this).text();
                $(this).addClass('postFilterBoxContainer');
                $(this).html('<input type="text" class="postFilterBox" placeholder="' + $.i18n.prop('index_search') + title + '" />');
                
                $('input', this).on('keyup change', function() {
                    if(postTable.column(i).search() !== this.value) {
                        $('#displayPaneIFrame').height($('#contentDisplayTable').height());
                        postTable
                            .column(i)
                            .search(this.value)
                            .draw();
                    }
                });
            } else {
                $(this).text('');
            }
        });
        
        var urlParams = new URLSearchParams(window.location.search);
        if(urlParams.has("hashsearch")) {
            $('#postTable tfoot tr:eq(1) th:eq(' + HASHTAGS_COLUMN_NO + ') input').val(urlParams.get("hashsearch"));
            $('#postTable tfoot tr:eq(1) th:eq(' + HASHTAGS_COLUMN_NO + ') input').change();
        }
        
    });
    
    $('input[type=radio][name=filterRead]').change(function() {
        if(this.id === "filterRead") {
            metadata.filter = "Filter Read Posts";
        } else if (this.id === "filterUnread") {
            metadata.filter = "Filter Unread Posts";
        } else if(this.id === "filterNoValues") {
            metadata.filter = "Do not Filter";
        }
        
        postTable.ajax.reload();
        $('#displayPaneIFrame').height($('#contentDisplayTable').height());
        postTable.draw();
        updateMDAPI();
    }); 
    
    $('input[type=radio][name=showFavs]').change(function() {
        if(this.id === "showFavourites") {
            metadata.favFilter = "Show Favourites";
        } else if (this.id === "showNonFavourites") {
            metadata.favFilter = "Show Non Favourites";
        } else if (this.id === "showAll") {
            metadata.favFilter = "Show Everything";
        }
        
        postTable.ajax.reload();
        $('#displayPaneIFrame').height($('#contentDisplayTable').height());
        postTable.draw();
        updateMDAPI();
    });
    
    $('input[type=radio][name=showReadingPaneRadio]').change(function() {
        if(this.id === "showReadingPaneSelected") {
            metadata.showReadingPane = true;
        } else {
            $('#contentDisplayReadingPane').hide();
            metadata.showReadingPane = false;
        }
        
        updateMDAPI();
    });
    
    var iframeOffset = $('#displayPaneIFrame').offset();
    $(window).scroll(function() {
        var scrollTop = $(window).scrollTop();
        
        if(iframeOffset.top < scrollTop) {
            $('#displayPaneIFrame').addClass('fixed');
        } else {
            $('#displayPaneIFrame').removeClass('fixed');
        }
    });
    
    $('#additionalOptionsLink').text($.i18n.prop('index_showOtherOptions'));
    
    $('#additionalOptionsLink').click(function() {
        if(additionalOptionsShowing) {
            $('#additionalOptionsTable').hide();
            $('#additionalOptionsLink').text($.i18n.prop('index_showOtherOptions'));
            additionalOptionsShowing = false;
        } else {
            $('#additionalOptionsTable').show();
            $('#additionalOptionsLink').text($.i18n.prop('index_hideOtherOptions'));
            additionalOptionsShowing = true;
        }
    });
    
});
