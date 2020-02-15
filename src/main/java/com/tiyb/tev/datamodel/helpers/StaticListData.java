package com.tiyb.tev.datamodel.helpers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class used for populating drop-down lists, for users setting their metadata preferences
 *
 * @author tiyb
 *
 */
public class StaticListData implements Serializable {

    private static final long serialVersionUID = 8979062815138982615L;

    /**
     * List of filter types
     */
    private List<String> filterTypes = new ArrayList<String>();

    /**
     * All columns that can be sorted on
     */
    private List<String> sortColumns = new ArrayList<String>();

    /**
     * Orders by which data can be sorted (i.e. asc/desc)
     */
    private List<String> sortOrders = new ArrayList<String>();

    /**
     * Filters for favourite/non-favourite posts
     */
    private List<String> favFilters = new ArrayList<String>();

    /**
     * List of valid page sizes
     */
    private List<Integer> pageLengths = new ArrayList<Integer>();

    /**
     * List of styles by which conversations can be displayed
     */
    private List<String> conversationStyles = new ArrayList<String>();

    /**
     * Columns by which conversation data can be sorted
     */
    private List<String> conversationSortColumns = new ArrayList<String>();

    /**
     * All themes available for TEV
     */
    private List<String> themes = new ArrayList<String>();

    public List<String> getFilterTypes() {
        return filterTypes;
    }

    public void setFilterTypes(final List<String> filterTypes) {
        this.filterTypes = filterTypes;
    }

    public List<String> getSortColumns() {
        return sortColumns;
    }

    public void setSortColumns(final List<String> sortColumns) {
        this.sortColumns = sortColumns;
    }

    public List<String> getSortOrders() {
        return sortOrders;
    }

    public void setSortOrders(final List<String> sortOrders) {
        this.sortOrders = sortOrders;
    }

    public List<String> getFavFilters() {
        return favFilters;
    }

    public void setFavFilters(final List<String> favFilters) {
        this.favFilters = favFilters;
    }

    public List<Integer> getPageLengths() {
        return pageLengths;
    }

    public void setPageLengths(final List<Integer> pageLengths) {
        this.pageLengths = pageLengths;
    }

    public List<String> getConversationStyles() {
        return conversationStyles;
    }

    public void setConversationStyles(final List<String> conversationStyles) {
        this.conversationStyles = conversationStyles;
    }

    public List<String> getConversationSortColumns() {
        return conversationSortColumns;
    }

    public void setConversationSortColumns(final List<String> conversationSortColumns) {
        this.conversationSortColumns = conversationSortColumns;
    }

    public List<String> getThemes() {
        return themes;
    }

    public void setThemes(final List<String> themes) {
        this.themes = themes;
    }
}
