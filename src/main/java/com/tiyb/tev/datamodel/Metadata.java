package com.tiyb.tev.datamodel;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * <p>
 * Encapsulates metadata stored in the database for use by the application. There are a number of
 * different fields stored here:
 * </p>
 *
 * <ul>
 * <li>baseMediaPath: The directory on the user's computer where media (images and videos) are
 * stored</li>
 * <li>sortColumn: The column that should be used for sorting the data in the table</li>
 * <li>sortOrder: The order (ascending or descending) that should be used for sorting the table</li>
 * <li>filter: How data should be filtered (only read posts, only unread posts, or all posts)</li>
 * </ul>
 *
 * <p>
 * In addition, there are a number of static constants used for some of these values.
 * </p>
 *
 * @author tiyb
 */
@Entity
@Table(name = "metadata")
public class Metadata implements Serializable {

    /**
     * Static constant list of ways data can be filtered (show only read posts, show only unread
     * posts, show all posts)
     */
    public static final List<String> FILTER_TYPES =
            Arrays.asList("Filter Read Posts", "Filter Unread Posts", "Do not Filter");

    /**
     * Static constant list of the different columns by which data can be sorted
     */
    public static final List<String> SORT_COLUMNS =
            Arrays.asList("ID", "Type", "State", "Slug", "Date", "Is Read", "Is Favourite", "Hashtags");

    /**
     * Static constant list of the different ways data can be sorted (ascending or descending). Used
     * for both Post column sort orders and Conversation column sort orders.
     */
    public static final List<String> SORT_ORDERS = Arrays.asList("Ascending", "Descending");

    /**
     * Static constant list of the different filters that can be applied to favourited posts
     */
    public static final List<String> FAV_FILTERS =
            Arrays.asList("Show Favourites", "Show Non Favourites", "Show Everything");

    /**
     * Static list of lengths the "number of records shown" drop-down can be set to
     */
    public static final List<Integer> PAGE_LENGTHS = Arrays.asList(10, 25, 50, 100, -1);

    /**
     * Default page length to use, when one hasn't been specified
     */
    public static final int DEFAULT_PAGELENGTH = 10;

    /**
     * Static list of different display style options for the Conversations page
     */
    public static final List<String> CONVERSATION_DISPLAY_STYLES = Arrays.asList("cloud", "table");

    /**
     * Static list of columns that can be sorted on the Conversations page
     */
    public static final List<String> CONVERSATION_SORT_COLUMNS = Arrays.asList("participantName", "numMessages");

    /**
     * Static list of theme names
     */
    public static final List<String> THEMES = Arrays.asList("base", "black-tie", "blitzer", "cupertino", "dark-hive",
            "dot-luv", "eggplant", "excite-bike", "flick", "hot-sneaks", "humanity", "le-frog", "mint-choc", "overcast",
            "pepper-grinder", "redmond", "smoothness", "south-street", "start", "sunny", "swanky-purse", "trontastic",
            "ui-darkness", "ui-lightness", "vader");

    /**
     * Default theme to use, when one isn't supplied
     */
    public static final String DEFAULT_THEME = "base";

    private static final long serialVersionUID = -2517986171637243590L;

    /**
     * Unique ID of the object
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    /**
     * Path on the file system where media (images/videos) should be stored
     */
    private String baseMediaPath;

    /**
     * Column on which to sort the Posts ta ble
     */
    private String sortColumn;

    /**
     * Order by which to sort (asc/desc)
     */
    private String sortOrder;

    /**
     * Whether to filter posts (based on {@link #FILTER_TYPES})
     */
    private String filter;

    /**
     * <p>
     * Name of the Tumblr user for this blog; should always be the same as {@link #blog}.
     * </p>
     *
     * <p>
     * TODO might remove this at some point, or otherwise reconcile the two members.
     * </p>
     */
    private String mainTumblrUser;

    /**
     * URL to the avatar image for the main Tumblr user.
     */
    private String mainTumblrUserAvatarUrl;

    /**
     * Filter to use for favourited/non-favourited posts; value from {@link #FAV_FILTERS}.
     */
    private String favFilter;

    /**
     * Number of posts to show on the posts/index page.
     */
    private Integer pageLength;

    /**
     * Whether to show the reading pane (true) or not (false)
     */
    private Boolean showReadingPane;

    /**
     * Whether data should be <i>overwritten</i> when uploading posts (true) or <i>added</i> to the
     * database (false).
     */
    private Boolean overwritePostData;

    /**
     * Whether data should be <i>overwritten</i> when uploading conversations (true) or <i>added</i>
     * to the database (false).
     */
    private Boolean overwriteConvoData;

    /**
     * Style in which to display conversations, per {@link #CONVERSATION_DISPLAY_STYLES}.
     */
    private String conversationDisplayStyle;

    /**
     * Column to use for sorting the conversation table, per {@link #CONVERSATION_SORT_COLUMNS}.
     */
    private String conversationSortColumn;

    /**
     * Sort order for conversation table, per {@link #SORT_ORDERS}.
     */
    private String conversationSortOrder;

    /**
     * Folder on the file system where images should be exported
     */
    private String exportImagesFilePath;

    /**
     * Theme to use for display, for the given blog, per {@link #THEMES}.
     */
    private String theme;

    /**
     * <p>
     * Name of the blog. Almost always the same as {@link #mainTumblrUser}.
     * </p>
     *
     * <p>
     * TODO these two fields should be reconciled.
     * </p>
     */
    private String blog;

    /**
     * Whether this blog is the default blog for TEV.
     */
    private Boolean isDefault;

    /**
     * Whether the hashtag viewer should show hashtags for all blogs (true) or just the default blog
     * (false)
     */
    private Boolean showHashtagsForAllBlogs;

    /**
     * Helper function to generate a new Metadata object, with some defaults filled in.
     * baseMediaPath, mainTumblrUser, and mainTumblrUserAvatarUrl not set, since no defaults make
     * sense for these values.
     *
     * @return Metadata object, with some reasonable defaults filled in.
     */
    public static Metadata newDefaultMetadata() {
        final Metadata md = new Metadata();
        md.setSortColumn(SORT_COLUMNS.get(0));
        md.setSortOrder(SORT_ORDERS.get(1));
        md.setFilter(FILTER_TYPES.get(2));
        md.setFavFilter(FAV_FILTERS.get(2));
        md.setPageLength(DEFAULT_PAGELENGTH);
        md.setShowReadingPane(false);
        md.setOverwritePostData(false);
        md.setOverwriteConvoData(false);
        md.setConversationDisplayStyle(CONVERSATION_DISPLAY_STYLES.get(0));
        md.setConversationSortColumn(CONVERSATION_SORT_COLUMNS.get(0));
        md.setConversationSortOrder(SORT_ORDERS.get(0));
        md.setTheme(DEFAULT_THEME);
        md.setIsDefault(false);
        md.setShowHashtagsForAllBlogs(true);

        return md;
    }

    /**
     * Helper method to update this object's properties with properties from another copy of the
     * object, <i>including</i> the ID property.
     *
     * @param newDataObject Object from which to copy the properties.
     */
    public void updateData(final Metadata newDataObject) {
        this.baseMediaPath = newDataObject.baseMediaPath;
        this.favFilter = newDataObject.favFilter;
        this.filter = newDataObject.filter;
        this.id = newDataObject.id;
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
        this.exportImagesFilePath = newDataObject.exportImagesFilePath;
        this.theme = newDataObject.theme;
        this.blog = newDataObject.blog;
        this.isDefault = newDataObject.isDefault;
        this.showHashtagsForAllBlogs = newDataObject.showHashtagsForAllBlogs;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Metadata [id=");
        builder.append(id);
        builder.append(", baseMediaPath=");
        builder.append(baseMediaPath);
        builder.append(", sortColumn=");
        builder.append(sortColumn);
        builder.append(", sortOrder=");
        builder.append(sortOrder);
        builder.append(", filter=");
        builder.append(filter);
        builder.append(", mainTumblrUser=");
        builder.append(mainTumblrUser);
        builder.append(", mainTumblrUserAvatarUrl=");
        builder.append(mainTumblrUserAvatarUrl);
        builder.append(", favFilter=");
        builder.append(favFilter);
        builder.append(", pageLength=");
        builder.append(pageLength);
        builder.append(", showReadingPane=");
        builder.append(showReadingPane);
        builder.append(", overwritePostData=");
        builder.append(overwritePostData);
        builder.append(", overwriteConvoData=");
        builder.append(overwriteConvoData);
        builder.append(", conversationDisplayStyle=");
        builder.append(conversationDisplayStyle);
        builder.append(", conversationSortColumn=");
        builder.append(conversationSortColumn);
        builder.append(", conversationSortOrder=");
        builder.append(conversationSortOrder);
        builder.append(", exportImagesFilePath=");
        builder.append(exportImagesFilePath);
        builder.append(", theme=");
        builder.append(theme);
        builder.append(", blog=");
        builder.append(blog);
        builder.append(", isDefault=");
        builder.append(isDefault);
        builder.append(", showHashtagsForAllBlogs=");
        builder.append(showHashtagsForAllBlogs);
        builder.append("]");
        return builder.toString();
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getBaseMediaPath() {
        return baseMediaPath;
    }

    public void setBaseMediaPath(final String baseMediaPath) {
        this.baseMediaPath = baseMediaPath;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(final String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(final String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(final String filter) {
        this.filter = filter;
    }

    public String getMainTumblrUser() {
        return mainTumblrUser;
    }

    public void setMainTumblrUser(final String mainTumblrUser) {
        this.mainTumblrUser = mainTumblrUser;
    }

    public String getMainTumblrUserAvatarUrl() {
        return mainTumblrUserAvatarUrl;
    }

    public void setMainTumblrUserAvatarUrl(final String mainTumblrUserAvatarUrl) {
        this.mainTumblrUserAvatarUrl = mainTumblrUserAvatarUrl;
    }

    public String getFavFilter() {
        return favFilter;
    }

    public void setFavFilter(final String favFilter) {
        this.favFilter = favFilter;
    }

    public Integer getPageLength() {
        return pageLength;
    }

    public void setPageLength(final Integer pageLength) {
        this.pageLength = pageLength;
    }

    public Boolean getShowReadingPane() {
        return showReadingPane;
    }

    public void setShowReadingPane(final Boolean showReadingPane) {
        this.showReadingPane = showReadingPane;
    }

    public Boolean getOverwritePostData() {
        return overwritePostData;
    }

    public void setOverwritePostData(final Boolean overwritePostData) {
        this.overwritePostData = overwritePostData;
    }

    public Boolean getOverwriteConvoData() {
        return overwriteConvoData;
    }

    public void setOverwriteConvoData(final Boolean overwriteConvoData) {
        this.overwriteConvoData = overwriteConvoData;
    }

    public String getConversationDisplayStyle() {
        return conversationDisplayStyle;
    }

    public void setConversationDisplayStyle(final String conversationDisplayStyle) {
        this.conversationDisplayStyle = conversationDisplayStyle;
    }

    public String getConversationSortColumn() {
        return conversationSortColumn;
    }

    public void setConversationSortColumn(final String conversationSortColumn) {
        this.conversationSortColumn = conversationSortColumn;
    }

    public String getConversationSortOrder() {
        return conversationSortOrder;
    }

    public void setConversationSortOrder(final String conversationSortOrder) {
        this.conversationSortOrder = conversationSortOrder;
    }

    public String getExportImagesFilePath() {
        return exportImagesFilePath;
    }

    public void setExportImagesFilePath(final String exportImagesFilePath) {
        this.exportImagesFilePath = exportImagesFilePath;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(final String theme) {
        this.theme = theme;
    }

    public String getBlog() {
        return blog;
    }

    public void setBlog(final String blog) {
        this.blog = blog;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(final Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean getShowHashtagsForAllBlogs() {
        return showHashtagsForAllBlogs;
    }

    public void setShowHashtagsForAllBlogs(final Boolean showHashtagsForAllBlogs) {
        this.showHashtagsForAllBlogs = showHashtagsForAllBlogs;
    }

}
