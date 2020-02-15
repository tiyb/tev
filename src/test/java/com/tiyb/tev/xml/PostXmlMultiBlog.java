package com.tiyb.tev.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import com.tiyb.tev.controller.TEVMetadataRestController;
import com.tiyb.tev.controller.TEVPostRestController;
import com.tiyb.tev.datamodel.Hashtag;
import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;

/**
 * Tests import of XML for a second blog, after one has already been imported
 *
 * @author tiyb
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PostXmlMultiBlog {

    @Autowired
    private TEVPostRestController postController;
    @Autowired
    private TEVMetadataRestController mdController;

    private static final int ORIGINAL_NUM_POSTS = 9;
    private static final int SECONDBLOG_NUM_POSTS = 1;
    private static final int SECONDBLOG_NUM_REG_POSTS = 1;

    private static final String MAIN_BLOG_NAME = "mainblog";
    private static final String SECOND_BLOG_NAME = "secondblog";
    private static final long SECONDBLOG_FIRSTPOSID = 180894436690L;

    private static final List<Hashtag> BLOG1_INITIAL_HASHTAGS = Arrays.asList(new Hashtag("tag1", 4, MAIN_BLOG_NAME),
            new Hashtag("tag2", 4, MAIN_BLOG_NAME), new Hashtag("tag3", 1, MAIN_BLOG_NAME),
            new Hashtag("tag4", 1, MAIN_BLOG_NAME), new Hashtag("tag5", 1, MAIN_BLOG_NAME),
            new Hashtag("tag6", 1, MAIN_BLOG_NAME), new Hashtag("tag7", 1, MAIN_BLOG_NAME),
            new Hashtag("tag8", 1, MAIN_BLOG_NAME), new Hashtag("tag9", 1, MAIN_BLOG_NAME),
            new Hashtag("tag10", 1, MAIN_BLOG_NAME), new Hashtag("tag11", 1, MAIN_BLOG_NAME),
            new Hashtag("tag12", 1, MAIN_BLOG_NAME), new Hashtag("tag13", 1, MAIN_BLOG_NAME),
            new Hashtag("tag14", 1, MAIN_BLOG_NAME), new Hashtag("tag15", 1, MAIN_BLOG_NAME));

    private static final List<Hashtag> BLOG2_INITIAL_HASHTAGS =
            Arrays.asList(new Hashtag("2ndtag1", 1, SECOND_BLOG_NAME), new Hashtag("tag2", 1, SECOND_BLOG_NAME));

    /**
     * Called before each unit test to properly reset the data back to an original state of having
     * loaded the test XML documents.
     *
     * @throws FileNotFoundException
     */
    @Before
    public void setupData() throws FileNotFoundException {
        Metadata md1 = mdController.getMetadataForBlogOrDefault(MAIN_BLOG_NAME);
        md1.setOverwritePostData(true);
        md1.setBlog(MAIN_BLOG_NAME);
        md1 = mdController.updateMetadata(md1.getId(), md1);
        mdController.markBlogAsDefault(md1.getId());
        Metadata md2 = mdController.getMetadataForBlogOrDefault(SECOND_BLOG_NAME);
        md2.setOverwritePostData(true);
        md2.setBlog(SECOND_BLOG_NAME);
        md2 = mdController.updateMetadata(md2.getId(), md2);

        File rawXmlFile = ResourceUtils.getFile("classpath:XML/test-post-xml.xml");
        InputStream xmlFile = new FileInputStream(rawXmlFile);
        BlogXmlReader.parseDocument(xmlFile, postController, MAIN_BLOG_NAME);

        rawXmlFile = ResourceUtils.getFile("classpath:XML/test-post-secondblog.xml");
        xmlFile = new FileInputStream(rawXmlFile);
        BlogXmlReader.parseDocument(xmlFile, postController, SECOND_BLOG_NAME);
    }

    /**
     * Simple check that all posts have been loaded; details are checked in other unit tests
     */
    @Test
    public void testAllPosts() {
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
    public void testLoadedData() {
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
        Regular regular =
                postController.getRegController().getRegularForBlogById(SECOND_BLOG_NAME, SECONDBLOG_FIRSTPOSID);
        assertThat(regular).isNotNull();
        assertThat(regular.getPostId()).isEqualTo(SECONDBLOG_FIRSTPOSID);
        assertThat(regular.getBody()).isEqualTo("This is a post on the second blog");
        assertThat(regular.getTitle()).isEqualTo("First Post");

    }

    /**
     * Tests that the initial load of posts generated the right number and count of hashtags for
     * both blogs
     */
    @Test
    public void testHashtags() {
        List<Hashtag> hashtags = postController.getAllHashtagsForBlog(MAIN_BLOG_NAME);
        assertThat(hashtags).isNotNull();
        assertThat(hashtags.size()).isEqualTo(BLOG1_INITIAL_HASHTAGS.size());

        hashtagTestHelper(hashtags, BLOG1_INITIAL_HASHTAGS);

        hashtags = postController.getAllHashtagsForBlog(SECOND_BLOG_NAME);
        assertThat(hashtags).isNotNull();
        assertThat(hashtags.size()).isEqualTo(BLOG2_INITIAL_HASHTAGS.size());

        hashtagTestHelper(hashtags, BLOG2_INITIAL_HASHTAGS);
    }

    /**
     * Helper function for testing that the tags coming from the API equal the <i>expected</i> tags
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
    public void testAddHashtag() {
        postController.createHashtagForBlog(SECOND_BLOG_NAME, "tag16");

        List<Hashtag> tags = postController.getAllHashtagsForBlog(MAIN_BLOG_NAME);

        assertThat(tags).isNotNull();
        assertThat(tags.size()).isEqualTo(BLOG1_INITIAL_HASHTAGS.size());

        tags = postController.getAllHashtagsForBlog(SECOND_BLOG_NAME);
        assertThat(tags).isNotNull();
        assertThat(tags.size()).isEqualTo(BLOG2_INITIAL_HASHTAGS.size() + 1);
    }

}
