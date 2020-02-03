package com.tiyb.tev.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Tests for the Metadata REST controller. Not all functionality is tested.
 * 
 * @author tiyb
 *
 */
@RunWith(SpringRunner.class)
public class TevMetadataRestControllerUnitTests {
	/**
	 * Tests that validation works for valid post types
	 */
	@Test
	public void testValidPostTypes() {
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
	public void testInvalidPostTypes() {
		assertThat(TEVMetadataRestController.isValidType("blah", TEVMetadataRestController.POST_TYPES))
				.isEqualTo(false);
	}

}
