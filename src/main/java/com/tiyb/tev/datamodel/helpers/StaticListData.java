package com.tiyb.tev.datamodel.helpers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class used for populating drop-down lists, for users setting their
 * metadata preferences
 * 
 * @author tiyb
 *
 */
public class StaticListData implements Serializable {

	private static final long serialVersionUID = 8979062815138982615L;

	private List<String> filterTypes = new ArrayList<String>();
	private List<String> sortColumns = new ArrayList<String>();
	private List<String> sortOrders = new ArrayList<String>();
	private List<String> favFilters = new ArrayList<String>();

	public List<String> getFilterTypes() {
		return filterTypes;
	}

	public void setFilterTypes(List<String> filterTypes) {
		this.filterTypes = filterTypes;
	}

	public List<String> getSortColumns() {
		return sortColumns;
	}

	public void setSortColumns(List<String> sortColumns) {
		this.sortColumns = sortColumns;
	}

	public List<String> getSortOrders() {
		return sortOrders;
	}

	public void setSortOrders(List<String> sortOrders) {
		this.sortOrders = sortOrders;
	}

	public List<String> getFavFilters() {
		return favFilters;
	}

	public void setFavFilters(List<String> favFilters) {
		this.favFilters = favFilters;
	}
}
