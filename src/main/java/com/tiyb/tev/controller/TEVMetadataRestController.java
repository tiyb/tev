package com.tiyb.tev.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.helpers.StaticListData;
import com.tiyb.tev.repository.MetadataRepository;

/**
 * REST controller for working with the application's Metadata -- it's settings.
 * Settings are kept on a blog-by-blog basis. Whenever a Metadata object is
 * encountered that doesn't have a valid <b>theme</b> set, the default is
 * applied; not having a theme is a fatal bug for the application, so extra care
 * is taken to guard agaisnt that eventuality.
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
	 * Helper function to determine if a "type" is valid or not, per Tumblr/TEV list
	 * of types. Though it is only used within this class, it was made public just
	 * in case other code ever needs it.
	 * 
	 * @param typeName       Name of the type to be validated
	 * @param validTypeNames List of all of the valid types in the system
	 * @return True/False
	 */
	public static boolean isValidType(String typeName, List<String> validTypeNames) {
		if (validTypeNames.contains(typeName)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * <p>
	 * GET to return all metadata objects stored in the system, for all blogs.
	 * </p>
	 * 
	 * <p>
	 * If there is no data in the database, a default object is returned based on
	 * {@link com.tiyb.tev.datamodel.Metadata#newDefaultMetadata()
	 * Metadata#newDefaultMetadata()}.
	 * </p>
	 * 
	 * @return The {@link com.tiyb.tev.datamodel.Metadata Metadata} object stored in
	 *         the database, or an empty object if the table has no data.
	 */
	@GetMapping("/metadata")
	public List<Metadata> getAllMetadata() {
		List<Metadata> list = metadataRepo.findAll();

		if (list.size() < 1) {
			list.add(Metadata.newDefaultMetadata());
			return list;
		}

		for (Metadata md : list) {
			if (!Metadata.THEMES.contains(md.getTheme())) {
				md.setTheme(Metadata.DEFAULT_THEME);
			}
		}

		return list;
	}

	/**
	 * GET to return the metadata for a particular blog based on the Metadata ID.
	 * 
	 * @param blog The name of the blog for which to return metadata
	 * @return {@link com.tiyb.tev.datamodel.Metadata Metadata} object containing
	 *         the settings for the blog in question
	 */
	@GetMapping("/metadata/{id}")
	public Metadata getMetadataByID(@PathVariable("id") Integer id) {
		Optional<Metadata> response = metadataRepo.findById(id);
		if (!response.isPresent()) {
			return null;
		}

		Metadata md = response.get();

		if (!Metadata.THEMES.contains(md.getTheme())) {
			md.setTheme(Metadata.DEFAULT_THEME);
		}

		return md;
	}

	/**
	 * PUT to mark a blog as the default blog by setting <code>isDefault</code> to
	 * true for its MD, and false to all other MDs.
	 * 
	 * @param id ID of the Metadata objec to be marked as the default
	 * @return The updated Metadata object
	 */
	@PutMapping("/metadata/{id}/markAsDefault")
	public Metadata markBlogAsDefault(@PathVariable("id") Integer id) {
		List<Metadata> allMDs = metadataRepo.findAll();
		Metadata response = null;

		for (Metadata md : allMDs) {
			if (id.equals(md.getId())) {
				md.setIsDefault(true);
				metadataRepo.save(md);
				response = md;
			} else {
				md.setIsDefault(false);
				metadataRepo.save(md);
			}
		}

		return response;
	}

	/**
	 * Gets the default Metadata object. If none is set as the default, the first is
	 * set as the default (and saved), then returned. If no MD exists at <i>all,</i>
	 * one is created and saved and returned.
	 * 
	 * @return The default MD object
	 */
	@GetMapping("/metadata/default")
	public Metadata getDefaultMetadata() {
		List<Metadata> allMD = metadataRepo.findAll();

		if (allMD == null || allMD.size() < 1) {
			Metadata newMD = Metadata.newDefaultMetadata();
			newMD.setIsDefault(true);
			return metadataRepo.save(newMD);
		}

		for (Metadata md : allMD) {
			if (md.getIsDefault()) {
				return md;
			}
		}

		allMD.get(0).setIsDefault(true);
		metadataRepo.save(allMD.get(0));

		return allMD.get(0);
	}

	/**
	 * GET to return the Metadata for a given blog, by name, or the default MD if
	 * none found
	 * 
	 * @param blog Name of the blog in question
	 * @return Metadata for the blog, or default if none
	 */
	@GetMapping("/metadata/byBlog/{blog}")
	public Metadata getMetadataForBlogOrDefault(@PathVariable("blog") String blog) {
		List<Metadata> all = metadataRepo.findAll();

		for (Metadata md : all) {
			if (blog.equals(md.getBlog())) {
				return md;
			}
		}

		Metadata defaultMD = Metadata.newDefaultMetadata();
		defaultMD.setBlog(blog);
		return metadataRepo.save(defaultMD);
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
	 * PUT to update metadata details in the database. Assertions have been added
	 * around ensuring proper themes are saved.
	 * 
	 * @param id              The ID of the MD being saved
	 * @param metadataDetails The data to be updated in the DB
	 * @return The updated {@link com.tiyb.tev.datamodel.Metadata Metadata} object
	 */
	@PutMapping("/metadata/{id}")
	public Metadata updateMetadata(@PathVariable("id") Integer id, @RequestBody Metadata metadataDetails) {
		Assert.hasText(metadataDetails.getTheme(), "Theme must not be null");
		Assert.isTrue(Metadata.THEMES.contains(metadataDetails.getTheme()),
				"Theme must be one of the pre-defined values");

		return metadataRepo.save(metadataDetails);
	}

}
