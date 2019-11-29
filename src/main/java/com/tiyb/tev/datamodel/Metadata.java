package com.tiyb.tev.datamodel;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Encapsulates metadata stored in the database for use by the application.
 * There are a number of different fields stored here:
 * 
 * <ul>
 * <li>baseMediaPath: The directory on the user's computer where media (images
 * and videos) are stored</li>
 * <li>sortColumn: The column that should be used for sorting the data in the
 * table</li>
 * <li>sortOrder: The order (ascending or descending) that should be used for
 * sorting the table</li>
 * <li>filter: How data should be filtered (only read posts, only unread posts,
 * or all posts)</li>
 * </ul>
 * 
 * In addition, there are a number of static constants used for some of these
 * values.
 * 
 * @author tiyb
 */
@Entity
@Table(name = "metadata")
public class Metadata implements Serializable {

	private static final long serialVersionUID = -2517986171637243590L;

	/**
	 * Static constant list of ways data can be filtered (show only read posts, show
	 * only unread posts, show all posts)
	 */
	public static final List<String> FILTER_TYPES = Arrays.asList("Filter Read Posts", "Filter Unread Posts",
			"Do not Filter");
	/**
	 * Static constant list of the different columns by which data can be sorted
	 */
	public static final List<String> SORT_COLUMNS = Arrays.asList("ID", "Type", "State", "Slug", "Date", "Is Read",
			"Is Favourite", "Hashtags");

	/**
	 * Static constant list of the different ways data can be sorted (ascending or
	 * descending). Used for both Post column sort orders and Conversation column
	 * sort orders.
	 */
	public static final List<String> SORT_ORDERS = Arrays.asList("Ascending", "Descending");

	/**
	 * Static constant list of the different filters that can be applied to
	 * favourited posts
	 */
	public static final List<String> FAV_FILTERS = Arrays.asList("Show Favourites", "Show Non Favourites",
			"Show Everything");

	/**
	 * Static list of lengths the "number of records shown" drop-down can be set to
	 */
	public static final List<Integer> PAGE_LENGTHS = Arrays.asList(10, 25, 50, 100, -1);

	/**
	 * Static list of different display style options for the Conversations page
	 */
	public static final List<String> CONVERSATION_DISPLAY_STYLES = Arrays.asList("cloud", "table");

	/**
	 * Static list of columns that can be sorted on the Conversations page
	 */
	public static final List<String> CONVERSATION_SORT_COLUMNS = Arrays.asList("participantName", "numMessages");

	@Id
	private Integer id;
	private String baseMediaPath;
	private String sortColumn;
	private String sortOrder;
	private String filter;
	private String mainTumblrUser;
	private String mainTumblrUserAvatarUrl;
	private String favFilter;
	private Integer pageLength;
	private Boolean showReadingPane;
	private Boolean overwritePostData;
	private Boolean overwriteConvoData;
	private String conversationDisplayStyle;
	private String conversationSortColumn;
	private String conversationSortOrder;

	/**
	 * Helper function to generate a new Metadata object, with some defaults filled
	 * in. baseMediaPath, mainTumblrUser, and mainTumblrUserAvatarUrl not set, since
	 * no defaults make sense for these values.
	 * 
	 * @return Metadata object, with an ID (1), and some reasonable defaults filled
	 *         in.
	 */
	public static Metadata newDefaultMetadata() {
		Metadata md = new Metadata();
		md.setId(1);
		md.setSortColumn(SORT_COLUMNS.get(0));
		md.setSortOrder(SORT_ORDERS.get(1));
		md.setFilter(FILTER_TYPES.get(2));
		md.setFavFilter(FAV_FILTERS.get(2));
		md.setPageLength(10);
		md.setShowReadingPane(false);
		md.setOverwritePostData(false);
		md.setOverwriteConvoData(false);
		md.setConversationDisplayStyle(CONVERSATION_DISPLAY_STYLES.get(0));
		md.setConversationSortColumn(CONVERSATION_SORT_COLUMNS.get(0));
		md.setConversationSortOrder(SORT_ORDERS.get(0));

		return md;
	}

	public void updateData(Metadata newDataObject) {
		this.baseMediaPath = newDataObject.baseMediaPath;
		this.favFilter = newDataObject.favFilter;
		this.filter = newDataObject.filter;
		// this.id = newDataObject.id;
		this.mainTumblrUser = newDataObject.mainTumblrUser;
		this.mainTumblrUserAvatarUrl = newDataObject.mainTumblrUserAvatarUrl;
		this.overwriteConvoData = newDataObject.overwriteConvoData;
		this.overwritePostData = newDataObject.overwritePostData;
		this.pageLength = newDataObject.pageLength;
		this.showReadingPane = newDataObject.showReadingPane;
		this.sortColumn = newDataObject.sortColumn;
		this.sortOrder = newDataObject.sortOrder;
		this.conversationDisplayStyle = newDataObject.conversationDisplayStyle;
		this.conversationSortColumn = newDataObject.conversationSortColumn;
		this.conversationSortOrder = newDataObject.conversationSortOrder;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Metadata [");
		if (id != null) {
			builder.append("id=");
			builder.append(id);
			builder.append(", ");
		}
		if (baseMediaPath != null) {
			builder.append("baseMediaPath=");
			builder.append(baseMediaPath);
			builder.append(", ");
		}
		if (sortColumn != null) {
			builder.append("sortColumn=");
			builder.append(sortColumn);
			builder.append(", ");
		}
		if (sortOrder != null) {
			builder.append("sortOrder=");
			builder.append(sortOrder);
			builder.append(", ");
		}
		if (filter != null) {
			builder.append("filter=");
			builder.append(filter);
			builder.append(", ");
		}
		if (mainTumblrUser != null) {
			builder.append("mainTumblrUser=");
			builder.append(mainTumblrUser);
			builder.append(", ");
		}
		if (mainTumblrUserAvatarUrl != null) {
			builder.append("mainTumblrUserAvatarUrl=");
			builder.append(mainTumblrUserAvatarUrl);
			builder.append(", ");
		}
		if (favFilter != null) {
			builder.append("favFilter=");
			builder.append(favFilter);
			builder.append(", ");
		}
		if (pageLength != null) {
			builder.append("pageLength=");
			builder.append(pageLength);
			builder.append(", ");
		}
		if (showReadingPane != null) {
			builder.append("showReadingPane=");
			builder.append(showReadingPane);
			builder.append(", ");
		}
		if (overwritePostData != null) {
			builder.append("overwritePostData=");
			builder.append(overwritePostData);
			builder.append(", ");
		}
		if (overwriteConvoData != null) {
			builder.append("overwriteConvoData=");
			builder.append(overwriteConvoData);
			builder.append(", ");
		}
		if (conversationDisplayStyle != null) {
			builder.append("conversationDisplayStyle=");
			builder.append(conversationDisplayStyle);
			builder.append(", ");
		}
		if (conversationSortColumn != null) {
			builder.append("conversationSortColumn=");
			builder.append(conversationSortColumn);
			builder.append(", ");
		}
		if (conversationSortOrder != null) {
			builder.append("conversationSortOrder=");
			builder.append(conversationSortOrder);
		}
		builder.append("]");
		return builder.toString();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getBaseMediaPath() {
		return baseMediaPath;
	}

	public void setBaseMediaPath(String baseMediaPath) {
		this.baseMediaPath = baseMediaPath;
	}

	public String getSortColumn() {
		return sortColumn;
	}

	public void setSortColumn(String sortColumn) {
		this.sortColumn = sortColumn;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getMainTumblrUser() {
		return mainTumblrUser;
	}

	public void setMainTumblrUser(String mainTumblrUser) {
		this.mainTumblrUser = mainTumblrUser;
	}

	public String getMainTumblrUserAvatarUrl() {
		return mainTumblrUserAvatarUrl;
	}

	public void setMainTumblrUserAvatarUrl(String mainTumblrUserAvatarUrl) {
		this.mainTumblrUserAvatarUrl = mainTumblrUserAvatarUrl;
	}

	public String getFavFilter() {
		return favFilter;
	}

	public void setFavFilter(String favFilter) {
		this.favFilter = favFilter;
	}

	public Integer getPageLength() {
		return pageLength;
	}

	public void setPageLength(Integer pageLength) {
		this.pageLength = pageLength;
	}

	public Boolean getShowReadingPane() {
		return showReadingPane;
	}

	public void setShowReadingPane(Boolean showReadingPane) {
		this.showReadingPane = showReadingPane;
	}

	public Boolean getOverwritePostData() {
		return overwritePostData;
	}

	public void setOverwritePostData(Boolean overwritePostData) {
		this.overwritePostData = overwritePostData;
	}

	public Boolean getOverwriteConvoData() {
		return overwriteConvoData;
	}

	public void setOverwriteConvoData(Boolean overwriteConvoData) {
		this.overwriteConvoData = overwriteConvoData;
	}

	public String getConversationDisplayStyle() {
		return conversationDisplayStyle;
	}

	public void setConversationDisplayStyle(String conversationDisplayStyle) {
		this.conversationDisplayStyle = conversationDisplayStyle;
	}

	public String getConversationSortColumn() {
		return conversationSortColumn;
	}

	public void setConversationSortColumn(String conversationSortColumn) {
		this.conversationSortColumn = conversationSortColumn;
	}

	public String getConversationSortOrder() {
		return conversationSortOrder;
	}

	public void setConversationSortOrder(String conversationSortOrder) {
		this.conversationSortOrder = conversationSortOrder;
	}

}
