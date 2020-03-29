package com.tiyb.tev.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.tiyb.tev.datamodel.Hashtag;
import com.tiyb.tev.datamodel.Metadata;

/**
 * Unit tests for working with Hashtags / the HT REST controller
 *
 * @author tiyb
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TevHTRestControllerUnitTests {

    private static final String SECOND_TAG_VALUE = "tag2";
    private static final String SECOND_BLOG_NAME = "blog2";
    private static final String FIRST_TAG_VALUE = "tag1";
    private static final String FIRST_BLOG_NAME = "blog1";

    @Autowired
    TEVHashtagController htController;

    @Autowired
    TEVMetadataRestController mdController;

    /**
     * Clean environment before each test
     */
    @Before
    public void setupData() {
        htController.deleteAllHTs();
        mdController.deleteAllMD();
        Metadata md1 = mdController.getMetadataForBlogOrDefault(FIRST_BLOG_NAME);
        md1.setBlog(FIRST_BLOG_NAME);
        md1.setIsDefault(true);
        md1 = mdController.updateMetadata(md1.getId(), md1);

        Metadata md2 = mdController.getMetadataForBlogOrDefault(SECOND_BLOG_NAME);
        md2.setBlog(SECOND_BLOG_NAME);
        md2.setIsDefault(false);
        md2 = mdController.updateMetadata(md2.getId(), md2);

        htController.deleteAllHTs();
    }

    /**
     * Simple test to create a single tag
     */
    @Test
    public void testCreatingTag() {
        htController.createHashtagForBlog(FIRST_BLOG_NAME, FIRST_TAG_VALUE);

        List<Hashtag> list = htController.getAllHashtagsForBlog(FIRST_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        Hashtag ht = list.get(0);
        assertThat(ht.getBlog()).isEqualTo(FIRST_BLOG_NAME);
        assertThat(ht.getCount()).isEqualTo(1);
        assertThat(ht.getTag()).isEqualTo(FIRST_TAG_VALUE);

        list = htController.getAllHashtagsForBlog(FIRST_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        ht = list.get(0);
        assertThat(ht.getBlog()).isEqualTo(FIRST_BLOG_NAME);
        assertThat(ht.getCount()).isEqualTo(1);
        assertThat(ht.getTag()).isEqualTo(FIRST_TAG_VALUE);

        list = htController.getAllHashtagsForBlog(SECOND_BLOG_NAME);
        assertThat(list).isNullOrEmpty();
    }

    /**
     * Tests adding the same tag twice, which should increase the count for that tag
     */
    @Test
    public void testAddingCount() {
        htController.createHashtagForBlog(FIRST_BLOG_NAME, FIRST_TAG_VALUE);
        List<Hashtag> list = htController.getAllHashtagsForBlog(FIRST_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);

        htController.createHashtagForBlog(FIRST_BLOG_NAME, FIRST_TAG_VALUE);
        list = htController.getAllHashtagsForBlog(FIRST_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        Hashtag ht = list.get(0);
        assertThat(ht.getBlog()).isEqualTo(FIRST_BLOG_NAME);
        assertThat(ht.getTag()).isEqualTo(FIRST_TAG_VALUE);
        assertThat(ht.getCount()).isEqualTo(2);
    }

    /**
     * Create multiple tags for the same blog
     */
    @Test
    public void testCreatingMultipleTags() {
        htController.createHashtagForBlog(FIRST_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(FIRST_BLOG_NAME, SECOND_TAG_VALUE);

        List<Hashtag> list = htController.getAllHashtagsForBlog(FIRST_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(2);
        Hashtag ht = list.get(0);
        assertThat(ht.getBlog()).isEqualTo(FIRST_BLOG_NAME);
        assertThat(ht.getCount()).isEqualTo(1);
        assertThat(ht.getTag()).isEqualTo(FIRST_TAG_VALUE);
        ht = list.get(1);
        assertThat(ht.getBlog()).isEqualTo(FIRST_BLOG_NAME);
        assertThat(ht.getCount()).isEqualTo(1);
        assertThat(ht.getTag()).isEqualTo(SECOND_TAG_VALUE);
    }

    /**
     * Create tags across multiple blogs
     */
    @Test
    public void creatingMultipleTagsInMultipleBlogs() {
        htController.createHashtagForBlog(FIRST_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(SECOND_BLOG_NAME, SECOND_TAG_VALUE);

        List<Hashtag> list = htController.getAllHashtagsForBlog(FIRST_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);

        list = htController.getAllHashtagsForBlog(SECOND_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
    }

    /**
     * Create tags for multiple blogs, add the same tag to one of them, and verify that the count is
     * increased for the impacted blog (but not the other)
     */
    @Test
    public void testAddingToOnlyOneBlog() {
        htController.createHashtagForBlog(FIRST_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(SECOND_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(FIRST_BLOG_NAME, FIRST_TAG_VALUE);

        List<Hashtag> list = htController.getAllHashtagsForBlog(FIRST_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        Hashtag ht = list.get(0);
        assertThat(ht.getCount()).isEqualTo(2);

        list = htController.getAllHashtagsForBlog(SECOND_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        ht = list.get(0);
        assertThat(ht.getCount()).isEqualTo(1);
    }

    /**
     * Create the same tag in multiple blogs, delete it from one, and verify it wasn't deleted from
     * the other
     */
    @Test
    public void deleteNameForOneBlog() {
        htController.createHashtagForBlog(FIRST_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(SECOND_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(FIRST_BLOG_NAME, FIRST_TAG_VALUE);

        Hashtag ht = new Hashtag(FIRST_TAG_VALUE, 2, FIRST_BLOG_NAME);
        Optional<Boolean> removeAll = Optional.of(false);
        htController.deleteHashTag(ht, removeAll);

        List<Hashtag> list = htController.getAllHashtagsForBlog(SECOND_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        Hashtag fromServer = list.get(0);
        assertThat(fromServer.getCount()).isEqualTo(1);

        list = htController.getAllHashtagsForBlog(FIRST_BLOG_NAME);
        assertThat(list).isNullOrEmpty();
    }

    /**
     * Create a tag across multiple blogs, use the "removeAll" function to delete it everywhere, and
     * verify it was actually deleted
     */
    @Test
    public void deleteAllForTagName() {
        htController.createHashtagForBlog(FIRST_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(SECOND_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(FIRST_BLOG_NAME, FIRST_TAG_VALUE);

        Hashtag ht = new Hashtag(FIRST_TAG_VALUE, 2, FIRST_BLOG_NAME);
        Optional<Boolean> removeAll = Optional.of(true);
        htController.deleteHashTag(ht, removeAll);

        List<Hashtag> list = htController.getAllHashtagsForBlog(SECOND_BLOG_NAME);
        assertThat(list).isNullOrEmpty();
        list = htController.getAllHashtagsForBlog(FIRST_BLOG_NAME);
        assertThat(list).isNullOrEmpty();
    }

    /**
     * Various tests to create tags across multiple blogs, then use the "get all" function to verify
     * that they are merging correctly
     */
    @Test
    public void testMergedList() {
        htController.createHashtagForBlog(FIRST_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(SECOND_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(FIRST_BLOG_NAME, FIRST_TAG_VALUE);

        List<Hashtag> list = htController.getAllHashtags();
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        Hashtag ht = list.get(0);
        assertThat(ht.getBlog()).isEqualTo(FIRST_BLOG_NAME);
        assertThat(ht.getCount()).isEqualTo(3);

        htController.createHashtagForBlog(SECOND_BLOG_NAME, SECOND_TAG_VALUE);

        list = htController.getAllHashtags();
        assertThat(list.size()).isEqualTo(2);
        for (Hashtag t : list) {
            if (FIRST_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(3);
                assertThat(t.getBlog()).isEqualTo(FIRST_BLOG_NAME);
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
                assertThat(t.getBlog()).isEqualTo(FIRST_BLOG_NAME);
            } else {
                assertThat(t.getCount()).isEqualTo(2);
                assertThat(t.getBlog()).isEqualTo(SECOND_BLOG_NAME);
            }
        }
    }
}
