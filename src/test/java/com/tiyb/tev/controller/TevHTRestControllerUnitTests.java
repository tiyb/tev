package com.tiyb.tev.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import com.tiyb.tev.datamodel.Hashtag;
import com.tiyb.tev.exception.ExistingTagException;
import com.tiyb.tev.html.HtmlTestingClass;

/**
 * Unit tests for working with Hashtags / the HT REST controller
 *
 * @author tiyb
 */
public class TevHTRestControllerUnitTests extends HtmlTestingClass {

    private static final String SECOND_TAG_VALUE = "tag2";
    private static final String FIRST_TAG_VALUE = "tag1";
    private static final String THIRD_TAG_VALUE = "tag3";

    @Autowired
    private TEVHashtagController htController;

    @Autowired
    private TEVMetadataRestController mdController;
    
    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Clean environment before each test
     */
    @Before
    public void setupData() {
        htController.deleteAllHTs();
        mdController.deleteAllMD();

        initMainBlogMetadataata(mdController, Optional.empty());
        initAdditionalBlogMetadata(mdController, SECOND_BLOG_NAME);

        htController.deleteAllHTs();
    }

    /**
     * Simple test to create a single tag
     */
    @Test
    public void testCreatingTag() {
        htController.createHashtagForBlog(MAIN_BLOG_NAME, FIRST_TAG_VALUE);

        List<Hashtag> list = htController.getAllHashtagsForBlog(MAIN_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        Hashtag ht = list.get(0);
        assertThat(ht.getBlog()).isEqualTo(MAIN_BLOG_NAME);
        assertThat(ht.getCount()).isEqualTo(1);
        assertThat(ht.getTag()).isEqualTo(FIRST_TAG_VALUE);

        list = htController.getAllHashtagsForBlog(MAIN_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        ht = list.get(0);
        assertThat(ht.getBlog()).isEqualTo(MAIN_BLOG_NAME);
        assertThat(ht.getCount()).isEqualTo(1);
        assertThat(ht.getTag()).isEqualTo(FIRST_TAG_VALUE);

        list = htController.getAllHashtagsForBlog(SECOND_BLOG_NAME);
        assertThat(list).isNullOrEmpty();
    }

    /**
     * Simple test to create a single tag, via REST
     */
    @Test
    public void testCreatingTagRest() {
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);

        ResponseEntity<Hashtag[]> responseEntity = restTemplate.getForEntity(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), Hashtag[].class);
        Hashtag[] list = responseEntity.getBody();
        assertThat(list).isNotEmpty();
        assertThat(list.length).isEqualTo(1);
        Hashtag ht = list[0];
        assertThat(ht.getBlog()).isEqualTo(MAIN_BLOG_NAME);
        assertThat(ht.getCount()).isEqualTo(1);
        assertThat(ht.getTag()).isEqualTo(FIRST_TAG_VALUE);

        responseEntity = restTemplate.getForEntity(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), Hashtag[].class);
        list = responseEntity.getBody();
        assertThat(list).isNotEmpty();
        assertThat(list.length).isEqualTo(1);
        ht = list[0];
        assertThat(ht.getBlog()).isEqualTo(MAIN_BLOG_NAME);
        assertThat(ht.getCount()).isEqualTo(1);
        assertThat(ht.getTag()).isEqualTo(FIRST_TAG_VALUE);

        responseEntity = restTemplate.getForEntity(String.format("%s/api/hashtags/%s", baseUri(), SECOND_BLOG_NAME), Hashtag[].class);
        list = responseEntity.getBody();
        assertThat(list).isNullOrEmpty();
    }

    /**
     * Tests adding the same tag twice, which should increase the count for that tag
     */
    @Test
    public void testAddingCount() {
        htController.createHashtagForBlog(MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        List<Hashtag> list = htController.getAllHashtagsForBlog(MAIN_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);

        htController.createHashtagForBlog(MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        list = htController.getAllHashtagsForBlog(MAIN_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        Hashtag ht = list.get(0);
        assertThat(ht.getBlog()).isEqualTo(MAIN_BLOG_NAME);
        assertThat(ht.getTag()).isEqualTo(FIRST_TAG_VALUE);
        assertThat(ht.getCount()).isEqualTo(2);
    }

    /**
     * Tests adding the same tag twice, which should increase the count for that tag, via REST
     */
    @Test
    public void testAddingCountRest() {
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);
        ResponseEntity<Hashtag[]> responseEntity = restTemplate.getForEntity(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), Hashtag[].class);
        Hashtag[] list = responseEntity.getBody();
        assertThat(list).isNotEmpty();
        assertThat(list.length).isEqualTo(1);

        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);
        responseEntity = restTemplate.getForEntity(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), Hashtag[].class);
        list = responseEntity.getBody();
        assertThat(list).isNotEmpty();
        assertThat(list.length).isEqualTo(1);
        Hashtag ht = list[0];
        assertThat(ht.getBlog()).isEqualTo(MAIN_BLOG_NAME);
        assertThat(ht.getTag()).isEqualTo(FIRST_TAG_VALUE);
        assertThat(ht.getCount()).isEqualTo(2);
    }

    /**
     * Create multiple tags for the same blog
     */
    @Test
    public void testCreatingMultipleTags() {
        htController.createHashtagForBlog(MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(MAIN_BLOG_NAME, SECOND_TAG_VALUE);

        List<Hashtag> list = htController.getAllHashtagsForBlog(MAIN_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(2);
        Hashtag ht = list.get(0);
        assertThat(ht.getBlog()).isEqualTo(MAIN_BLOG_NAME);
        assertThat(ht.getCount()).isEqualTo(1);
        assertThat(ht.getTag()).isEqualTo(FIRST_TAG_VALUE);
        ht = list.get(1);
        assertThat(ht.getBlog()).isEqualTo(MAIN_BLOG_NAME);
        assertThat(ht.getCount()).isEqualTo(1);
        assertThat(ht.getTag()).isEqualTo(SECOND_TAG_VALUE);
    }

    /**
     * Create multiple tags for the same blog, via REST
     */
    @Test
    public void testCreatingMultipleTagsRest() {
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), SECOND_TAG_VALUE, Hashtag.class);

        ResponseEntity<Hashtag[]> responseEntity = restTemplate.getForEntity(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), Hashtag[].class);
        Hashtag[] list = responseEntity.getBody();
        assertThat(list).isNotEmpty();
        assertThat(list.length).isEqualTo(2);
        Hashtag ht = list[0];
        assertThat(ht.getBlog()).isEqualTo(MAIN_BLOG_NAME);
        assertThat(ht.getCount()).isEqualTo(1);
        assertThat(ht.getTag()).isEqualTo(FIRST_TAG_VALUE);
        ht = list[1];
        assertThat(ht.getBlog()).isEqualTo(MAIN_BLOG_NAME);
        assertThat(ht.getCount()).isEqualTo(1);
        assertThat(ht.getTag()).isEqualTo(SECOND_TAG_VALUE);
    }

    /**
     * Create tags across multiple blogs
     */
    @Test
    public void creatingMultipleTagsInMultipleBlogs() {
        htController.createHashtagForBlog(MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(SECOND_BLOG_NAME, SECOND_TAG_VALUE);

        List<Hashtag> list = htController.getAllHashtagsForBlog(MAIN_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);

        list = htController.getAllHashtagsForBlog(SECOND_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
    }

    /**
     * Create tags across multiple blogs, via REST
     */
    @Test
    public void creatingMultipleTagsInMultipleBlogsRest() {
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), SECOND_BLOG_NAME), SECOND_TAG_VALUE, Hashtag.class);

        ResponseEntity<Hashtag[]> responseEntity = restTemplate.getForEntity(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), Hashtag[].class);
        Hashtag[] list = responseEntity.getBody();
        assertThat(list).isNotEmpty();
        assertThat(list.length).isEqualTo(1);

        responseEntity = restTemplate.getForEntity(String.format("%s/api/hashtags/%s", baseUri(), SECOND_BLOG_NAME), Hashtag[].class);
        list = responseEntity.getBody();
        assertThat(list).isNotNull();
        assertThat(list.length).isEqualTo(1);
    }

    /**
     * Create tags for multiple blogs, add the same tag to one of them, and verify
     * that the count is increased for the impacted blog (but not the other)
     */
    @Test
    public void testAddingToOnlyOneBlog() {
        htController.createHashtagForBlog(MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(SECOND_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(MAIN_BLOG_NAME, FIRST_TAG_VALUE);

        List<Hashtag> list = htController.getAllHashtagsForBlog(MAIN_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        Hashtag ht = list.get(0);
        assertThat(ht.getCount()).isEqualTo(2);

        list = htController.getAllHashtagsForBlog(SECOND_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        ht = list.get(0);
        assertThat(ht.getCount()).isEqualTo(1);

        List<Hashtag> allTags = htController.getAllHashtags();
        assertThat(allTags.size()).isEqualTo(1);
        assertThat(allTags.get(0).getBlog()).contains(MAIN_BLOG_NAME, SECOND_BLOG_NAME);
    }

    /**
     * Create tags for multiple blogs, add the same tag to one of them, and verify
     * that the count is increased for the impacted blog (but not the other), via REST
     */
    @Test
    public void testAddingToOnlyOneBlogRest() {
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), SECOND_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);

        ResponseEntity<Hashtag[]> responseEntity = restTemplate.getForEntity(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), Hashtag[].class);
        Hashtag[] list = responseEntity.getBody();
        assertThat(list).isNotEmpty();
        assertThat(list.length).isEqualTo(1);
        Hashtag ht = list[0];
        assertThat(ht.getCount()).isEqualTo(2);

        responseEntity = restTemplate.getForEntity(String.format("%s/api/hashtags/%s", baseUri(), SECOND_BLOG_NAME), Hashtag[].class);
        list = responseEntity.getBody();
        assertThat(list).isNotNull();
        assertThat(list.length).isEqualTo(1);
        ht = list[0];
        assertThat(ht.getCount()).isEqualTo(1);

        responseEntity = restTemplate.getForEntity(String.format("%s/api/hashtags", baseUri()), Hashtag[].class);
        Hashtag[] allTags = responseEntity.getBody();
        assertThat(allTags.length).isEqualTo(1);
        assertThat(allTags[0].getBlog()).contains(MAIN_BLOG_NAME, SECOND_BLOG_NAME);
    }

    /**
     * Create the same tag in multiple blogs, delete it from one, and verify it
     * wasn't deleted from the other
     */
    @Test
    public void deleteNameForOneBlog() {
        Hashtag ht1 = htController.createHashtagForBlog(MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(SECOND_BLOG_NAME, FIRST_TAG_VALUE);
        Hashtag ht3 = htController.createHashtagForBlog(MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        
        assertThat(ht1.getId()).isEqualTo(ht3.getId());

        htController.deleteHashTag(ht3.getId());

        List<Hashtag> list = htController.getAllHashtagsForBlog(SECOND_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        Hashtag fromServer = list.get(0);
        assertThat(fromServer.getCount()).isEqualTo(1);

        list = htController.getAllHashtagsForBlog(MAIN_BLOG_NAME);
        assertThat(list).isNullOrEmpty();
    }

    /**
     * Create the same tag in multiple blogs, delete it from one, and verify it
     * wasn't deleted from the other, via REST
     */
    @Test
    public void deleteNameForOneBlogRest() {
        Hashtag ht1 = restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), SECOND_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);
        Hashtag ht3 = restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);
        
        assertThat(ht1.getId()).isEqualTo(ht3.getId());

        restTemplate.delete(String.format("%s/api/hashtags/%d", baseUri(), ht3.getId()));

        ResponseEntity<Hashtag[]> responseEntity = restTemplate.getForEntity(String.format("%s/api/hashtags/%s", baseUri(), SECOND_BLOG_NAME), Hashtag[].class);
        Hashtag[] list = responseEntity.getBody();
        assertThat(list).isNotEmpty();
        assertThat(list.length).isEqualTo(1);
        Hashtag fromServer = list[0];
        assertThat(fromServer.getCount()).isEqualTo(1);

        responseEntity = restTemplate.getForEntity(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), Hashtag[].class);
        list = responseEntity.getBody();
        assertThat(list).isNullOrEmpty();
    }

    /**
     * Various tests to create tags across multiple blogs, then use the "get all"
     * function to verify that they are merging correctly
     */
    @Test
    public void mergeList() {
        htController.createHashtagForBlog(MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(SECOND_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(MAIN_BLOG_NAME, FIRST_TAG_VALUE);

        List<Hashtag> list = htController.getAllHashtags();
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        Hashtag ht = list.get(0);
        assertThat(ht.getBlog()).contains(MAIN_BLOG_NAME, SECOND_BLOG_NAME);
        assertThat(ht.getCount()).isEqualTo(3);

        htController.createHashtagForBlog(SECOND_BLOG_NAME, SECOND_TAG_VALUE);

        list = htController.getAllHashtags();
        assertThat(list.size()).isEqualTo(2);
        for (Hashtag t : list) {
            if (FIRST_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(3);
                assertThat(t.getBlog()).contains(MAIN_BLOG_NAME, SECOND_BLOG_NAME);
            } else {
                assertThat(t.getCount()).isEqualTo(1);
                assertThat(t.getBlog()).isEqualTo(SECOND_BLOG_NAME);
            }
        }

        htController.createHashtagForBlog(SECOND_BLOG_NAME, SECOND_TAG_VALUE);

        list = htController.getAllHashtags();
        assertThat(list.size()).isEqualTo(2);
        for (Hashtag t : list) {
            if (FIRST_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(3);
                assertThat(t.getBlog()).contains(MAIN_BLOG_NAME, SECOND_BLOG_NAME);
            } else {
                assertThat(t.getCount()).isEqualTo(2);
                assertThat(t.getBlog()).isEqualTo(SECOND_BLOG_NAME);
            }
        }
    }

    /**
     * Various tests to create tags across multiple blogs, then use the "get all"
     * function to verify that they are merging correctly, via REST
     */
    @Test
    public void mergeListRest() {
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), SECOND_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);

        ResponseEntity<Hashtag[]> responseEntity = restTemplate.getForEntity(String.format("%s/api/hashtags/", baseUri()), Hashtag[].class);
        Hashtag[] list = responseEntity.getBody();
        assertThat(list).isNotEmpty();
        assertThat(list.length).isEqualTo(1);
        Hashtag ht = list[0];
        assertThat(ht.getBlog()).contains(MAIN_BLOG_NAME, SECOND_BLOG_NAME);
        assertThat(ht.getCount()).isEqualTo(3);

        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), SECOND_BLOG_NAME), SECOND_TAG_VALUE, Hashtag.class);

        responseEntity = restTemplate.getForEntity(String.format("%s/api/hashtags/", baseUri()), Hashtag[].class);
        list = responseEntity.getBody();
        
        assertThat(list.length).isEqualTo(2);
        for (Hashtag t : list) {
            if (FIRST_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(3);
                assertThat(t.getBlog()).contains(MAIN_BLOG_NAME, SECOND_BLOG_NAME);
            } else {
                assertThat(t.getCount()).isEqualTo(1);
                assertThat(t.getBlog()).isEqualTo(SECOND_BLOG_NAME);
            }
        }

        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), SECOND_BLOG_NAME), SECOND_TAG_VALUE, Hashtag.class);

        responseEntity = restTemplate.getForEntity(String.format("%s/api/hashtags/", baseUri()), Hashtag[].class);
        list = responseEntity.getBody();
        assertThat(list.length).isEqualTo(2);
        for (Hashtag t : list) {
            if (FIRST_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(3);
                assertThat(t.getBlog()).contains(MAIN_BLOG_NAME, SECOND_BLOG_NAME);
            } else {
                assertThat(t.getCount()).isEqualTo(2);
                assertThat(t.getBlog()).isEqualTo(SECOND_BLOG_NAME);
            }
        }
    }

    /**
     * Tests that a hashtag can be created for no blog
     */
    @Test
    public void createHTForNoBlog() {
        htController.createHashtagForBlog(MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(SECOND_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(SECOND_BLOG_NAME, SECOND_TAG_VALUE);
        htController.createHashtagForNoBlog(THIRD_TAG_VALUE);

        List<Hashtag> list = htController.getAllHashtags();
        assertThat(list.size()).isEqualTo(3);

        for (Hashtag t : list) {
            if (FIRST_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(3);
                assertThat(t.getBlog()).contains(MAIN_BLOG_NAME, SECOND_BLOG_NAME);
            } else if (SECOND_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(1);
                assertThat(t.getBlog()).isEqualTo(SECOND_BLOG_NAME);
            } else if (THIRD_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(1);
                assertThat(t.getBlog()).isBlank();
            } else {
                assertThat(true).isFalse();
            }
        }
    }

    /**
     * Tests that a hashtag can be created for no blog, via REST
     */
    @Test
    public void createHTForNoBlogRest() {
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), SECOND_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), SECOND_BLOG_NAME), SECOND_TAG_VALUE, Hashtag.class);
        restTemplate.postForObject(String.format("%s/api/hashtags/", baseUri()), THIRD_TAG_VALUE, Hashtag.class);

        ResponseEntity<Hashtag[]> responseEntity = restTemplate.getForEntity(String.format("%s/api/hashtags/", baseUri()), Hashtag[].class);
        Hashtag[] list = responseEntity.getBody();
        assertThat(list.length).isEqualTo(3);

        for (Hashtag t : list) {
            if (FIRST_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(3);
                assertThat(t.getBlog()).contains(MAIN_BLOG_NAME, SECOND_BLOG_NAME);
            } else if (SECOND_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(1);
                assertThat(t.getBlog()).isEqualTo(SECOND_BLOG_NAME);
            } else if (THIRD_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(1);
                assertThat(t.getBlog()).isBlank();
            } else {
                assertThat(true).isFalse();
            }
        }
    }

    /**
     * Tests that a hashtag created with no blog can be deleted
     */
    @Test
    public void deleteBlankBlog() {
        htController.createHashtagForBlog(MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(SECOND_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(SECOND_BLOG_NAME, SECOND_TAG_VALUE);
        Hashtag ht5 = htController.createHashtagForNoBlog(THIRD_TAG_VALUE);

        htController.deleteHashTag(ht5.getId());

        List<Hashtag> list = htController.getAllHashtags();
        assertThat(list.size()).isEqualTo(2);

        for (Hashtag t : list) {
            if (FIRST_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(3);
                assertThat(t.getBlog()).contains(MAIN_BLOG_NAME, SECOND_BLOG_NAME);
            } else if (SECOND_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(1);
                assertThat(t.getBlog()).isEqualTo(SECOND_BLOG_NAME);
            } else if (THIRD_TAG_VALUE.equals(t.getTag())) {
                assertThat(true).isFalse();
            } else {
                assertThat(true).isFalse();
            }
        }
    }

    /**
     * Tests that a hashtag created with no blog can be deleted, via REST
     */
    @Test
    public void deleteBlankBlogRest() {
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), SECOND_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), SECOND_BLOG_NAME), SECOND_TAG_VALUE, Hashtag.class);
        Hashtag ht5 = restTemplate.postForObject(String.format("%s/api/hashtags/", baseUri()), THIRD_TAG_VALUE, Hashtag.class);

        restTemplate.delete(String.format("%s/api/hashtags/%d", baseUri(), ht5.getId()));

        ResponseEntity<Hashtag[]> responseEntity = restTemplate.getForEntity(String.format("%s/api/hashtags/", baseUri()), Hashtag[].class);
        Hashtag[] list = responseEntity.getBody();
        assertThat(list.length).isEqualTo(2);

        for (Hashtag t : list) {
            if (FIRST_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(3);
                assertThat(t.getBlog()).contains(MAIN_BLOG_NAME, SECOND_BLOG_NAME);
            } else if (SECOND_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(1);
                assertThat(t.getBlog()).isEqualTo(SECOND_BLOG_NAME);
            } else if (THIRD_TAG_VALUE.equals(t.getTag())) {
                assertThat(true).isFalse();
            } else {
                assertThat(true).isFalse();
            }
        }
    }

    /**
     * Tests that a hashtag can't be inserted into the "no blog" namespace if it's
     * already been created for a blog
     */
    @Test(expected = ExistingTagException.class)
    public void htCantCreateBlankWhenExists() {
        htController.createHashtagForBlog(MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForNoBlog(FIRST_TAG_VALUE);
    }
    
    /**
     * Tests that a hashtag can't be inserted into the "no blog" namespace if it's
     * already been created for a blog, via REST
     */
    @Test(expected = RestClientException.class)
    public void htCantCreateBlankWhenExistsRest() {
        restTemplate.postForObject(String.format("%s/api/hashtags/%s", baseUri(), MAIN_BLOG_NAME), FIRST_TAG_VALUE, Hashtag.class);
        restTemplate.postForObject(String.format("%s/api/hashtags/", baseUri()), FIRST_TAG_VALUE, Hashtag.class);
    }
}
