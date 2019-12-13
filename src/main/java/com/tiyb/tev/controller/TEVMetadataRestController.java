package com.tiyb.tev.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.helpers.StaticListData;
import com.tiyb.tev.repository.MetadataRepository;

/**
 * REST controller for working with the application's Metadata -- it's settings.
 * 
 * @author tiyb
 */
@RestController
@RequestMapping("/api")
public class TEVMetadataRestController {

	/**
	 * The repo for working with Metadata
	 */
	@Autowired
	private MetadataRepository metadataRepo;

	/**
	 * List of all post types available from Tumblr/TEV
	 */
	public final static List<String> POST_TYPES = new ArrayList<String>(
			Arrays.asList("answer", "link", "photo", "regular", "video"));

	/**
	 * GET to return all Types stored in the system. This implementation is
	 * <i>very</i> hard-coded, because there are only a specific number of types
	 * actually supported by Tumblr. The {@link #POST_TYPES} member is public, but
	 * this API is still useful for JavaScript uses.
	 * 
	 * @return {@link java.util.List List} of type strings
	 */
	@GetMapping("/types")
	public List<String> getAllTypes() {
		return POST_TYPES;
	}

	/**
	 * <p>
	 * GET to return the metadata stored in the system. Only one record will ever be
	 * stored in the <code>metadata</code> table, so the API assumes one and only
	 * one {@link com.tiyb.tev.datamodel.Metadata Metadata} object.
	 * </p>
	 * 
	 * <p>
	 * If there is no data in the database, a default object is returned based on
	 * {@link com.tiyb.tev.datamodel.Metadata#newDefaultMetadata()
	 * Metadata#newDefaultMetadata()}. If an object is found but doesn't have a
	 * valid <b>theme</b> one is supplied, because not having a valid theme causes
	 * major problems for the application's UI.
	 * </p>
	 * 
	 * @return The {@link com.tiyb.tev.datamodel.Metadata Metadata} object stored in
	 *         the database, or an empty object if the table has no data.
	 */
	@GetMapping("/metadata")
	public Metadata getMetadata() {
		List<Metadata> list = metadataRepo.findAll();

		if (list.size() > 0) {
			if (!Metadata.THEMES.contains(list.get(0).getTheme())) {
				list.get(0).setTheme(Metadata.DEFAULT_THEME);
			}
			return list.get(0);
		} else {
			Metadata md = Metadata.newDefaultMetadata();
			return md;
		}
	}

	/**
	 * GET to return static data used to populate drop-down lists, for the user to
	 * set things like preferred sort order, filters, etc.
	 * 
	 * @return object containing the different lists of strings available
	 */
	@GetMapping("/metadata/staticListData")
	public StaticListData getStaticListData() {
		StaticListData sld = new StaticListData();

		for (String s : Metadata.FILTER_TYPES) {
			sld.getFilterTypes().add(s);
		}
		for (String s : Metadata.SORT_COLUMNS) {
			sld.getSortColumns().add(s);
		}
		for (String s : Metadata.SORT_ORDERS) {
			sld.getSortOrders().add(s);
		}
		for (String s : Metadata.FAV_FILTERS) {
			sld.getFavFilters().add(s);
		}
		for (Integer i : Metadata.PAGE_LENGTHS) {
			sld.getPageLengths().add(i);
		}
		for (String s : Metadata.CONVERSATION_DISPLAY_STYLES) {
			sld.getConversationStyles().add(s);
		}
		for (String s : Metadata.CONVERSATION_SORT_COLUMNS) {
			sld.getConversationSortColumns().add(s);
		}
		for (String s : Metadata.THEMES) {
			sld.getThemes().add(s);
		}

		return sld;
	}

	/**
	 * <p>
	 * PUT to update metadata details in the database. Code always acts as if there
	 * is one (and only one) record in the DB, even if it's empty; if no record
	 * exists to be updated, a new one is created instead. For this reason, the ID
	 * is always 1.
	 * </p>
	 * 
	 * <p>
	 * An assertion has been added that the theme must be one of the pre-defined
	 * themes; it was determined that putting an invalid theme in the system causes
	 * the application to fail non-gracefully.
	 * </p>
	 * 
	 * @param metadataDetails The data to be updated in the DB
	 * @return The updated {@link com.tiyb.tev.datamodel.Metadata Metadata} object
	 */
	@PutMapping("/metadata")
	public Metadata updateMetadata(@RequestBody Metadata metadataDetails) {
		Metadata md;
		Optional<Metadata> omd = metadataRepo.findById(1);
		if (omd.isPresent()) {
			md = omd.get();
		} else {
			md = new Metadata();
			md.setId(1);
		}

		Assert.hasText(metadataDetails.getTheme(), "Theme must not be null");
		Assert.isTrue(Metadata.THEMES.contains(metadataDetails.getTheme()),
				"Theme must be one of the pre-defined values");
		md.updateData(metadataDetails);

		Metadata returnValue = metadataRepo.save(md);

		return returnValue;
	}

}
