package com.tiyb.tev.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.Type;
import com.tiyb.tev.datamodel.helpers.StaticListData;
import com.tiyb.tev.repository.MetadataRepository;
import com.tiyb.tev.repository.TypeRepository;

/**
 * REST controller for working with the application's Metadata -- it's settings.
 * 
 * @author tiyb
 * @apiviz.landmark
 * @apiviz.uses com.tiyb.tev.repository.TypeRepository
 * @apiviz.uses com.tiyb.tev.repository.MetadataRepository
 */
@RestController
@RequestMapping("/api")
public class TEVMetadataRestController {

	/**
	 * The repo for working with Type data
	 */
	@Autowired
	private TypeRepository typeRepo;

	/**
	 * The repo for working with Metadata
	 */
	@Autowired
	private MetadataRepository metadataRepo;

	/**
	 * GET to return all Types stored in the system. If there are no types, a
	 * hard-coded list of types is created. This implementation is <i>very</i>
	 * hard-coded, because there are only a specific number of types actually
	 * supported by Tumblr.
	 * 
	 * @return {@link java.util.List List} of {@link com.tiyb.tev.datamodel.Type
	 *         Type} objects
	 */
	@GetMapping("/types")
	public List<Type> getAllTypes() {
		List<Type> types = typeRepo.findAll();

		if (types.size() == 0) {
			Type type1 = new Type();
			type1.setId(1L);
			type1.setType("answer");
			typeRepo.save(type1);
			types.add(type1);

			Type type2 = new Type();
			type2.setId(2L);
			type2.setType("link");
			typeRepo.save(type2);
			types.add(type2);

			Type type3 = new Type();
			type3.setId(3L);
			type3.setType("photo");
			typeRepo.save(type3);
			types.add(type3);

			Type type4 = new Type();
			type4.setId(4L);
			type4.setType("regular");
			typeRepo.save(type4);
			types.add(type4);

			Type type5 = new Type();
			type5.setId(5L);
			type5.setType("video");
			typeRepo.save(type5);
			types.add(type5);
		}

		return types;
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
	 * Metadata#newDefaultMetadata()}
	 * </p>
	 * 
	 * @return The {@link com.tiyb.tev.datamodel.Metadata Metadata} object stored in
	 *         the database, or an empty object if the table has no data.
	 */
	@GetMapping("/metadata")
	public Metadata getMetadata() {
		List<Metadata> list = metadataRepo.findAll();

		if (list.size() > 0) {
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

		return sld;
	}

	/**
	 * PUT to update metadata details in the database. Code always acts as if there
	 * is one (and only one) record in the DB, even if it's empty; if no record
	 * exists to be updated, a new one is created instead. For this reason, the ID
	 * is always 1.
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

		md.updateData(metadataDetails);
		
		Metadata returnValue = metadataRepo.save(md);

		return returnValue;
	}

}
