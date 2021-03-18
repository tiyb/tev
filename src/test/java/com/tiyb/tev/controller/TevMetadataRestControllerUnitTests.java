package com.tiyb.tev.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.exception.NoMetadataFoundException;
import com.tiyb.tev.exception.UnableToDeleteMetadataException;
import com.tiyb.tev.html.HtmlTestingClass;

/**
 * Tests for the Metadata REST controller. Not all functionality is tested.
 * 
 * @author tiyb
 *
 */
public class TevMetadataRestControllerUnitTests extends HtmlTestingClass {

    @Autowired
    private TEVMetadataRestController mdController;

    @Autowired
    private TestRestTemplate restTemplate;

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
     * Tests that calling the
     * {@link com.tiyb.tev.controller.TEVMetadataRestController#getDefaultBlogName()
     * getDefaultBlogName()} method returns the proper exception when there are no
     * MD objects in the DB, via REST
     */
    @Test
    public void noBlogsReturnsCorrectExceptionRest() {
        cleanAllMDObjects();
        ResponseEntity<String> result = restTemplate.exchange(
                String.format("%s/api/metadata/default/blogName", baseUri()), HttpMethod.GET, HttpEntity.EMPTY,
                String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

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
     * 
     * via REST
     */
    @Test
    public void metadataCreationRest() {
        cleanAllMDObjects();
        Metadata md1 = restTemplate
                .getForObject(String.format("%s/api/metadata/byBlog/%s/orDefault", baseUri(), "blog1"), Metadata.class);
        assertThat(md1).isNotNull();
        assertThat(md1.getBlog()).isEqualTo("blog1");
        assertThat(md1.getIsDefault()).isEqualTo(true);

        ResponseEntity<Metadata[]> responseEntity = restTemplate
                .getForEntity(String.format("%s/api/metadata", baseUri()), Metadata[].class);
        Metadata[] mdList = responseEntity.getBody();
        assertThat(mdList).isNotEmpty();
        assertThat(mdList.length).isEqualTo(1);

        Metadata md2 = restTemplate
                .getForObject(String.format("%s/api/metadata/byBlog/%s/orDefault", baseUri(), "blog2"), Metadata.class);
        assertThat(md2).isNotNull();
        assertThat(md2.getBlog()).isEqualTo("blog2");
        assertThat(md2.getIsDefault()).isEqualTo(false);

        responseEntity = restTemplate.getForEntity(String.format("%s/api/metadata", baseUri()), Metadata[].class);
        mdList = responseEntity.getBody();
        assertThat(mdList).isNotEmpty();
        assertThat(mdList.length).isEqualTo(2);
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
     * Tests the controller's logic whereby it will refuse to delete the last MD in
     * the DB, via REST
     */
    @Test
    public void deleteLastMDReturnsRightExceptionRest() throws URISyntaxException {
        cleanAllMDObjects();

        restTemplate.getForObject(String.format("%s/api/metadata/byBlog/%s/orDefault", baseUri(), "blog1"),
                Metadata.class);
        restTemplate.getForObject(String.format("%s/api/metadata/byBlog/%s/orDefault", baseUri(), "blog2"),
                Metadata.class);

        ResponseEntity<Metadata[]> responseEntity = restTemplate
                .getForEntity(String.format("%s/api/metadata", baseUri()), Metadata[].class);
        Metadata[] md = responseEntity.getBody();

        ResponseEntity<String> result = restTemplate.exchange(
                String.format("%s/api/metadata/%d", baseUri(), md[0].getId()), HttpMethod.DELETE, HttpEntity.EMPTY,
                String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        result = restTemplate.exchange(String.format("%s/api/metadata/%d", baseUri(), md[1].getId()), HttpMethod.DELETE,
                HttpEntity.EMPTY, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /**
     * Helper function to delete all MD objects in the DB (if any)
     */
    private void cleanAllMDObjects() {
        mdController.deleteAllMD();
    }

}
