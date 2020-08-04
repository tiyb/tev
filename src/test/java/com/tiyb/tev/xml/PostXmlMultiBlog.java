package com.tiyb.tev.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.tiyb.tev.TevTestingClass;
import com.tiyb.tev.controller.TEVMetadataRestController;
import com.tiyb.tev.controller.TEVPostRestController;
import com.tiyb.tev.datamodel.Hashtag;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;

/**
 * Tests import of XML for a second blog, after one has already been imported
 *
 * @author tiyb
 *
 */
public class PostXmlMultiBlog extends TevTestingClass {

    @Autowired
    private TEVPostRestController postController;
    @Autowired
    private TEVMetadataRestController mdController;

    private static final int ORIGINAL_NUM_POSTS = 9;
    private static final int SECONDBLOG_NUM_POSTS = 1;
    private static final int SECONDBLOG_NUM_REG_POSTS = 1;

    private static final String SECONDBLOG_FIRSTPOSID = "180894436690";

    private static final List<Hashtag> BLOG1_INITIAL_HASHTAGS = Arrays.asList(new Hashtag("tag1", 4, MAIN_BLOG_NAME),
            new Hashtag("tag2", 4, MAIN_BLOG_NAME), new Hashtag("tag3", 1, MAIN_BLOG_NAME),
            new Hashtag("tag4", 1, MAIN_BLOG_NAME), new Hashtag("tag5", 1, MAIN_BLOG_NAME),
            new Hashtag("tag6", 1, MAIN_BLOG_NAME), new Hashtag("tag7", 1, MAIN_BLOG_NAME),
            new Hashtag("tag8", 1, MAIN_BLOG_NAME), new Hashtag("tag9", 1, MAIN_BLOG_NAME),
            new Hashtag("tag10", 1, MAIN_BLOG_NAME), new Hashtag("tag11", 1, MAIN_BLOG_NAME),
            new Hashtag("tag12", 1, MAIN_BLOG_NAME), new Hashtag("tag13", 1, MAIN_BLOG_NAME),
            new Hashtag("tag14", 1, MAIN_BLOG_NAME), new Hashtag("tag15", 1, MAIN_BLOG_NAME));

    private static final List<Hashtag> BLOG2_INITIAL_HASHTAGS = Arrays
            .asList(new Hashtag("2ndtag1", 1, SECOND_BLOG_NAME), new Hashtag("tag2", 1, SECOND_BLOG_NAME));

    private static final String DUPLICATED_HT = "tag2";
    private static final int DUPLICATED_HT_COUNT = 5;

    /**
     * Called before each unit test to properly reset the data back to an original
     * state of having loaded the test XML documents.
     *
     * @throws FileNotFoundException
     */
    @Before
    public void setupData() throws FileNotFoundException {
        postController.getHashtagController().deleteAllHTs();

        initDataForMainBlog(mdController, postController, Optional.empty());
        initDataForSecondaryBlog(mdController, postController, Optional.empty());
    }

    /**
     * Simple check that all posts have been loaded; details are checked in other
     * unit tests
     */
    @Test
    public void allPosts() {
        List<Post> posts = postController.getAllPostsForBlog(MAIN_BLOG_NAME);
        assertThat(posts).isNotNull();
        assertThat(posts.size()).isEqualTo(ORIGINAL_NUM_POSTS);

        posts = postController.getAllPostsForBlog(SECOND_BLOG_NAME);
        assertThat(posts).isNotNull();
        assertThat(posts.size()).isEqualTo(SECONDBLOG_NUM_POSTS);

    }

    /**
     * tests that the one and only post for the 2nd blog has loaded successfully
     */
    @Test
    public void loadedData() {
        Post post = postController.getPostForBlogById(SECOND_BLOG_NAME, SECONDBLOG_FIRSTPOSID);
        assertThat(post).isNotNull();
        assertThat(post.getDate()).isEqualTo("Fri, 07 Dec 2018 11:48:43");
        assertThat(post.getDateGmt()).isEqualTo("2018-12-07 16:48:43 GMT");
        assertThat(post.getIsFavourite()).isEqualTo(false);
        assertThat(post.getIsRead()).isEqualTo(false);
        assertThat(post.getIsReblog()).isEqualTo(true);
        assertThat(post.getReblogKey()).isEqualTo("O6pLVlp1");
        assertThat(post.getSlug()).isEqualTo("first-post");
        assertThat(post.getState()).isEqualTo("published");
        assertThat(post.getTags()).isEqualTo("2ndtag1, tag2");
        assertThat(post.getTumblelog()).isEqualTo(SECOND_BLOG_NAME);
        assertThat(post.getType()).isEqualTo("regular");
        assertThat(post.getUnixtimestamp()).isEqualTo(1544201323L);
        assertThat(post.getUrl()).isEqualTo("https://mainblog.tumblr.com/post/180894436690");
        assertThat(post.getUrlWithSlug()).isEqualTo("https://mainblog.tumblr.com/post/180894436690/first-post");

        assertThat(postController.getRegController().getAllRegularsForBlog(SECOND_BLOG_NAME).size())
                .isEqualTo(SECONDBLOG_NUM_REG_POSTS);
        Regular regular = postController.getRegController().getRegularForBlogById(SECOND_BLOG_NAME,
                SECONDBLOG_FIRSTPOSID);
        assertThat(regular).isNotNull();
        assertThat(regular.getPostId()).isEqualTo(SECONDBLOG_FIRSTPOSID);
        assertThat(regular.getBody()).isEqualTo("This is a post on the second blog");
        assertThat(regular.getTitle()).isEqualTo("First Post");

    }

    /**
     * Tests that the initial load of posts generated the right number and count of
     * hashtags for both blogs
     */
    @Test
    public void hashtags() {
        List<Hashtag> hashtags = postController.getHashtagController().getAllHashtagsForBlog(MAIN_BLOG_NAME);
        assertThat(hashtags).isNotNull();
        assertThat(hashtags.size()).isEqualTo(BLOG1_INITIAL_HASHTAGS.size());

        hashtagTestHelper(hashtags, BLOG1_INITIAL_HASHTAGS);

        hashtags = postController.getHashtagController().getAllHashtagsForBlog(SECOND_BLOG_NAME);
        assertThat(hashtags).isNotNull();
        assertThat(hashtags.size()).isEqualTo(BLOG2_INITIAL_HASHTAGS.size());

        hashtagTestHelper(hashtags, BLOG2_INITIAL_HASHTAGS);
    }

    /**
     * Tests that hashtags are properly combined across both blogs. There is one
     * hashtag that exists in both blogs, so that count for that tag should be the
     * combined value of both.
     */
    @Test
    public void combinedHashtags() {
        List<Hashtag> allHT = postController.getHashtagController().getAllHashtags();
        assertThat(allHT).isNotNull();
        assertThat(allHT.size()).isEqualTo(BLOG1_INITIAL_HASHTAGS.size() + BLOG2_INITIAL_HASHTAGS.size() - 1);

        for (Hashtag h : allHT) {
            if (DUPLICATED_HT.equals(h.getTag())) {
                assertThat(h.getCount()).isEqualTo(DUPLICATED_HT_COUNT);
            }
        }
    }

    /**
     * Helper function for testing that the tags coming from the API equal the
     * <i>expected</i> tags
     *
     * @param tagsFromAPI The tags returned from the Post API
     * @param masterList  The set of expected tags
     */
    private void hashtagTestHelper(List<Hashtag> tagsFromAPI, List<Hashtag> masterList) {
        assertThat(tagsFromAPI.size()).isEqualTo(masterList.size());

        for (Hashtag tagFromAPI : tagsFromAPI) {
            boolean tagFound = false;
            for (Hashtag tagFromList : masterList) {
                if (tagFromAPI.getTag().equals(tagFromList.getTag())) {
                    assertThat(tagFromAPI.getCount()).isEqualTo(tagFromList.getCount());
                    tagFound = true;
                }
            }
            assertThat(tagFound).isTrue();
        }
    }

    /**
     * Test adding a new hashtag to the 2nd blog, after the initial load
     */
    @Test
    public void addHashtag() {
        postController.getHashtagController().createHashtagForBlog(SECOND_BLOG_NAME, "tag16");

        List<Hashtag> tags = postController.getHashtagController().getAllHashtagsForBlog(MAIN_BLOG_NAME);

        assertThat(tags).isNotNull();
        assertThat(tags.size()).isEqualTo(BLOG1_INITIAL_HASHTAGS.size());

        tags = postController.getHashtagController().getAllHashtagsForBlog(SECOND_BLOG_NAME);
        assertThat(tags).isNotNull();
        assertThat(tags.size()).isEqualTo(BLOG2_INITIAL_HASHTAGS.size() + 1);
    }

}
