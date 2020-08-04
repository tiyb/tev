package com.tiyb.tev.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.exception.NoMetadataFoundException;
import com.tiyb.tev.exception.UnableToDeleteMetadataException;

/**
 * Tests for the Metadata REST controller. Not all functionality is tested.
 * 
 * @author tiyb
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TevMetadataRestControllerUnitTests {

	@Autowired
	private TEVMetadataRestController mdController;

	/**
	 * Tests that validation works for valid post types
	 */
	@Test
	public void validPostTypes() {
		assertThat(TEVMetadataRestController.isValidType("answer", TEVMetadataRestController.POST_TYPES))
				.isEqualTo(true);
		assertThat(TEVMetadataRestController.isValidType("link", TEVMetadataRestController.POST_TYPES)).isEqualTo(true);
		assertThat(TEVMetadataRestController.isValidType("photo", TEVMetadataRestController.POST_TYPES))
				.isEqualTo(true);
		assertThat(TEVMetadataRestController.isValidType("regular", TEVMetadataRestController.POST_TYPES))
				.isEqualTo(true);
		assertThat(TEVMetadataRestController.isValidType("video", TEVMetadataRestController.POST_TYPES))
				.isEqualTo(true);
	}

	/**
	 * Tests that validation works for invalid post types
	 */
	@Test
	public void invalidPostTypes() {
		assertThat(TEVMetadataRestController.isValidType("blah", TEVMetadataRestController.POST_TYPES))
				.isEqualTo(false);
	}

	/**
	 * Tests that calling the
	 * {@link com.tiyb.tev.controller.TEVMetadataRestController#getDefaultBlogName()
	 * getDefaultBlogName()} method returns the proper exception when there are no
	 * MD objects in the DB
	 */
	@Test(expected = NoMetadataFoundException.class)
	public void noBlogsReturnsCorrectException() {
		cleanAllMDObjects();
		mdController.getDefaultBlogName();
	}

	/**
	 * Tests that:
	 * 
	 * <ol>
	 * <li>Creating an MD object when there are none in the system works
	 * correctly</li>
	 * <li>Creating an MD object when it's the first marks it as the default</li>
	 * <li>Creating a second MD object when there is already one there works
	 * correct</li>
	 * <li>A second MD object is <i>not</i> set as the default</li>
	 * </ol>
	 */
	@Test
	public void metadataCreation() {
		cleanAllMDObjects();
		Metadata md1 = mdController.getMetadataForBlogOrDefault("blog1");
		assertThat(md1).isNotNull();
		assertThat(md1.getBlog()).isEqualTo("blog1");
		assertThat(md1.getIsDefault()).isEqualTo(true);

		List<Metadata> mdList = mdController.getAllMetadata();
		assertThat(mdList).isNotNull();
		assertThat(mdList.size()).isEqualTo(1);

		Metadata md2 = mdController.getMetadataForBlogOrDefault("blog2");
		assertThat(md2).isNotNull();
		assertThat(md2.getBlog()).isEqualTo("blog2");
		assertThat(md2.getIsDefault()).isEqualTo(false);

		mdList = mdController.getAllMetadata();
		assertThat(mdList).isNotNull();
		assertThat(mdList.size()).isEqualTo(2);
	}

	/**
	 * Tests the controller's logic whereby it will refuse to delete the last MD in
	 * the DB
	 */
	@Test(expected = UnableToDeleteMetadataException.class)
	public void deleteLastMDReturnsRightException() {
		cleanAllMDObjects();

		mdController.getMetadataForBlogOrDefault("blog1");
		mdController.getMetadataForBlogOrDefault("blog2");

		List<Metadata> md = mdController.getAllMetadata();

		for (Metadata m : md) {
			mdController.deleteMetadata(m.getId());
		}
	}

	/**
	 * Helper function to delete all MD objects in the DB (if any)
	 */
	private void cleanAllMDObjects() {
		mdController.deleteAllMD();
	}

}
