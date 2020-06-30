package com.tiyb.tev.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.tiyb.tev.TevTestingHelpers;
import com.tiyb.tev.datamodel.Hashtag;
import com.tiyb.tev.exception.ExistingTagException;

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
    private static final String FIRST_TAG_VALUE = "tag1";
    private static final String THIRD_TAG_VALUE = "tag3";

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

        TevTestingHelpers.initMainBlogNoData(mdController, Optional.empty());
        TevTestingHelpers.initAdditionalBlog(mdController, TevTestingHelpers.SECOND_BLOG_NAME);

        htController.deleteAllHTs();
    }

    /**
     * Simple test to create a single tag
     */
    @Test
    public void testCreatingTag() {
        htController.createHashtagForBlog(TevTestingHelpers.MAIN_BLOG_NAME, FIRST_TAG_VALUE);

        List<Hashtag> list = htController.getAllHashtagsForBlog(TevTestingHelpers.MAIN_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        Hashtag ht = list.get(0);
        assertThat(ht.getBlog()).isEqualTo(TevTestingHelpers.MAIN_BLOG_NAME);
        assertThat(ht.getCount()).isEqualTo(1);
        assertThat(ht.getTag()).isEqualTo(FIRST_TAG_VALUE);

        list = htController.getAllHashtagsForBlog(TevTestingHelpers.MAIN_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        ht = list.get(0);
        assertThat(ht.getBlog()).isEqualTo(TevTestingHelpers.MAIN_BLOG_NAME);
        assertThat(ht.getCount()).isEqualTo(1);
        assertThat(ht.getTag()).isEqualTo(FIRST_TAG_VALUE);

        list = htController.getAllHashtagsForBlog(TevTestingHelpers.SECOND_BLOG_NAME);
        assertThat(list).isNullOrEmpty();
    }

    /**
     * Tests adding the same tag twice, which should increase the count for that tag
     */
    @Test
    public void testAddingCount() {
        htController.createHashtagForBlog(TevTestingHelpers.MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        List<Hashtag> list = htController.getAllHashtagsForBlog(TevTestingHelpers.MAIN_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);

        htController.createHashtagForBlog(TevTestingHelpers.MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        list = htController.getAllHashtagsForBlog(TevTestingHelpers.MAIN_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        Hashtag ht = list.get(0);
        assertThat(ht.getBlog()).isEqualTo(TevTestingHelpers.MAIN_BLOG_NAME);
        assertThat(ht.getTag()).isEqualTo(FIRST_TAG_VALUE);
        assertThat(ht.getCount()).isEqualTo(2);
    }

    /**
     * Create multiple tags for the same blog
     */
    @Test
    public void testCreatingMultipleTags() {
        htController.createHashtagForBlog(TevTestingHelpers.MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(TevTestingHelpers.MAIN_BLOG_NAME, SECOND_TAG_VALUE);

        List<Hashtag> list = htController.getAllHashtagsForBlog(TevTestingHelpers.MAIN_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(2);
        Hashtag ht = list.get(0);
        assertThat(ht.getBlog()).isEqualTo(TevTestingHelpers.MAIN_BLOG_NAME);
        assertThat(ht.getCount()).isEqualTo(1);
        assertThat(ht.getTag()).isEqualTo(FIRST_TAG_VALUE);
        ht = list.get(1);
        assertThat(ht.getBlog()).isEqualTo(TevTestingHelpers.MAIN_BLOG_NAME);
        assertThat(ht.getCount()).isEqualTo(1);
        assertThat(ht.getTag()).isEqualTo(SECOND_TAG_VALUE);
    }

    /**
     * Create tags across multiple blogs
     */
    @Test
    public void creatingMultipleTagsInMultipleBlogs() {
        htController.createHashtagForBlog(TevTestingHelpers.MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(TevTestingHelpers.SECOND_BLOG_NAME, SECOND_TAG_VALUE);

        List<Hashtag> list = htController.getAllHashtagsForBlog(TevTestingHelpers.MAIN_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);

        list = htController.getAllHashtagsForBlog(TevTestingHelpers.SECOND_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
    }

    /**
     * Create tags for multiple blogs, add the same tag to one of them, and verify
     * that the count is increased for the impacted blog (but not the other)
     */
    @Test
    public void testAddingToOnlyOneBlog() {
        htController.createHashtagForBlog(TevTestingHelpers.MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(TevTestingHelpers.SECOND_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(TevTestingHelpers.MAIN_BLOG_NAME, FIRST_TAG_VALUE);

        List<Hashtag> list = htController.getAllHashtagsForBlog(TevTestingHelpers.MAIN_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        Hashtag ht = list.get(0);
        assertThat(ht.getCount()).isEqualTo(2);

        list = htController.getAllHashtagsForBlog(TevTestingHelpers.SECOND_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        ht = list.get(0);
        assertThat(ht.getCount()).isEqualTo(1);

        List<Hashtag> allTags = htController.getAllHashtags();
        assertThat(allTags.size()).isEqualTo(1);
        assertThat(allTags.get(0).getBlog()).contains(TevTestingHelpers.MAIN_BLOG_NAME,
                TevTestingHelpers.SECOND_BLOG_NAME);
    }

    /**
     * Create the same tag in multiple blogs, delete it from one, and verify it
     * wasn't deleted from the other
     */
    @Test
    public void deleteNameForOneBlog() {
        htController.createHashtagForBlog(TevTestingHelpers.MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(TevTestingHelpers.SECOND_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(TevTestingHelpers.MAIN_BLOG_NAME, FIRST_TAG_VALUE);

        Hashtag ht = new Hashtag(FIRST_TAG_VALUE, 2, TevTestingHelpers.MAIN_BLOG_NAME);
        htController.deleteHashTag(ht);

        List<Hashtag> list = htController.getAllHashtagsForBlog(TevTestingHelpers.SECOND_BLOG_NAME);
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        Hashtag fromServer = list.get(0);
        assertThat(fromServer.getCount()).isEqualTo(1);

        list = htController.getAllHashtagsForBlog(TevTestingHelpers.MAIN_BLOG_NAME);
        assertThat(list).isNullOrEmpty();
    }

    /**
     * Various tests to create tags across multiple blogs, then use the "get all"
     * function to verify that they are merging correctly
     */
    @Test
    public void testMergedList() {
        htController.createHashtagForBlog(TevTestingHelpers.MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(TevTestingHelpers.SECOND_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(TevTestingHelpers.MAIN_BLOG_NAME, FIRST_TAG_VALUE);

        List<Hashtag> list = htController.getAllHashtags();
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        Hashtag ht = list.get(0);
        assertThat(ht.getBlog()).contains(TevTestingHelpers.MAIN_BLOG_NAME, TevTestingHelpers.SECOND_BLOG_NAME);
        assertThat(ht.getCount()).isEqualTo(3);

        htController.createHashtagForBlog(TevTestingHelpers.SECOND_BLOG_NAME, SECOND_TAG_VALUE);

        list = htController.getAllHashtags();
        assertThat(list.size()).isEqualTo(2);
        for (Hashtag t : list) {
            if (FIRST_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(3);
                assertThat(t.getBlog()).contains(TevTestingHelpers.MAIN_BLOG_NAME, TevTestingHelpers.SECOND_BLOG_NAME);
            } else {
                assertThat(t.getCount()).isEqualTo(1);
                assertThat(t.getBlog()).isEqualTo(TevTestingHelpers.SECOND_BLOG_NAME);
            }
        }

        htController.createHashtagForBlog(TevTestingHelpers.SECOND_BLOG_NAME, SECOND_TAG_VALUE);

        list = htController.getAllHashtags();
        assertThat(list.size()).isEqualTo(2);
        for (Hashtag t : list) {
            if (FIRST_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(3);
                assertThat(t.getBlog()).contains(TevTestingHelpers.MAIN_BLOG_NAME, TevTestingHelpers.SECOND_BLOG_NAME);
            } else {
                assertThat(t.getCount()).isEqualTo(2);
                assertThat(t.getBlog()).isEqualTo(TevTestingHelpers.SECOND_BLOG_NAME);
            }
        }
    }

    /**
     * Tests that a hashtag can be created for no blog
     */
    @Test
    public void createHTForNoBlog() {
        htController.createHashtagForBlog(TevTestingHelpers.MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(TevTestingHelpers.SECOND_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(TevTestingHelpers.MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(TevTestingHelpers.SECOND_BLOG_NAME, SECOND_TAG_VALUE);
        htController.createHashtagForNoBlog(THIRD_TAG_VALUE);

        List<Hashtag> list = htController.getAllHashtags();
        assertThat(list.size()).isEqualTo(3);

        for (Hashtag t : list) {
            if (FIRST_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(3);
                assertThat(t.getBlog()).contains(TevTestingHelpers.MAIN_BLOG_NAME, TevTestingHelpers.SECOND_BLOG_NAME);
            } else if (SECOND_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(1);
                assertThat(t.getBlog()).isEqualTo(TevTestingHelpers.SECOND_BLOG_NAME);
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
        htController.createHashtagForBlog(TevTestingHelpers.MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(TevTestingHelpers.SECOND_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(TevTestingHelpers.MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForBlog(TevTestingHelpers.SECOND_BLOG_NAME, SECOND_TAG_VALUE);
        htController.createHashtagForNoBlog(THIRD_TAG_VALUE);

        Hashtag htToDelete = new Hashtag(THIRD_TAG_VALUE, 0, StringUtils.EMPTY);
        htController.deleteHashTag(htToDelete);

        List<Hashtag> list = htController.getAllHashtags();
        assertThat(list.size()).isEqualTo(2);

        for (Hashtag t : list) {
            if (FIRST_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(3);
                assertThat(t.getBlog()).contains(TevTestingHelpers.MAIN_BLOG_NAME, TevTestingHelpers.SECOND_BLOG_NAME);
            } else if (SECOND_TAG_VALUE.equals(t.getTag())) {
                assertThat(t.getCount()).isEqualTo(1);
                assertThat(t.getBlog()).isEqualTo(TevTestingHelpers.SECOND_BLOG_NAME);
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
        htController.createHashtagForBlog(TevTestingHelpers.MAIN_BLOG_NAME, FIRST_TAG_VALUE);
        htController.createHashtagForNoBlog(FIRST_TAG_VALUE);
    }
}
