package com.tiyb.tev.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.html.HtmlTestingClass;

/**
 * Unit test cases for testing admin functions of the application.
 *
 * @author tiyb
 *
 */
public class TevAdminToolsUnitTests extends HtmlTestingClass {

    @Autowired
    private TEVAdminToolsController adminRestController;
    @Autowired
    private TEVPostRestController postController;
    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Don't know what the Rule annotation does, but this is a temporary folder
     * where images can be created and deleted
     */
    @Rule
    public TemporaryFolder tempMDImageFolder = new TemporaryFolder();

    /**
     * Temp folder used for importing images
     */
    @Rule
    public TemporaryFolder tempInputImageFolder = new TemporaryFolder();

    /**
     * Sets up the posts to a valid state, using the helper function
     *
     * @throws FileNotFoundException
     */
    @Before
    public void setupData() throws FileNotFoundException {
        Optional<String> baseMediaPath = Optional.of(tempMDImageFolder.getRoot().getAbsolutePath());
        restInitDataForMainBlog(baseMediaPath);
    }

    /**
     * Tests that any files not associated with a post are deleted from the folder
     *
     * @throws IOException
     */
    @Test
    public void cleanExtraImages() throws IOException {
        tempMDImageFolder.newFile("blah.txt");

        adminRestController.cleanImagesOnHDForBlog(MAIN_BLOG_NAME);

        assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(0);
    }

    /**
     * Tests that any files not associated with a post are deleted from the folder,
     * via the REST interface
     *
     * @throws IOException
     */
    @Test
    public void cleanExtraImagesRest() throws IOException {
        tempMDImageFolder.newFile("blah.txt");

        restTemplate.getForEntity(String.format("%s/admintools/posts/%s/cleanImagesOnHD", baseUri(), MAIN_BLOG_NAME),
                String.class);

        assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(0);
    }

    /**
     * Tests that nothing is deleted when the exact number of files is in the folder
     * as expected
     *
     * @throws IOException
     */
    @Test
    public void cleanExactNumImages() throws IOException {
        tempMDImageFolder.newFile("180784644740_0.gif");
        tempMDImageFolder.newFile("180254465582_0.gif");
        tempMDImageFolder.newFile("180254465582_1.gif");

        adminRestController.cleanImagesOnHDForBlog(MAIN_BLOG_NAME);

        assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(3);
    }

    /**
     * Tests that nothing is deleted when the exact number of files is in the folder
     * as expected, via the REST interface
     *
     * @throws IOException
     */
    @Test
    public void cleanExactNumImagesRest() throws IOException {
        tempMDImageFolder.newFile("180784644740_0.gif");
        tempMDImageFolder.newFile("180254465582_0.gif");
        tempMDImageFolder.newFile("180254465582_1.gif");

        restTemplate.getForEntity(String.format("%s/admintools/posts/%s/cleanImagesOnHD", baseUri(), MAIN_BLOG_NAME),
                String.class);

        assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(3);
    }

    /**
     * Tests that duplicate images are removed from the folder (the core result of
     * the feature)
     *
     * @throws IOException
     */
    @Test
    public void cleanDoubleImages() throws IOException {
        tempMDImageFolder.newFile("180784644740_0.gif");
        tempMDImageFolder.newFile("180784644740_1.gif");
        tempMDImageFolder.newFile("180254465582_0.gif");
        tempMDImageFolder.newFile("180254465582_1.gif");
        tempMDImageFolder.newFile("180254465582_2.gif");
        tempMDImageFolder.newFile("180254465582_3.gif");

        adminRestController.cleanImagesOnHDForBlog(MAIN_BLOG_NAME);

        assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(3);
    }

    /**
     * Tests that duplicate images are removed from the folder (the core result of
     * the feature), via the REST interface
     *
     * @throws IOException
     */
    @Test
    public void cleanDoubleImagesRest() throws IOException {
        tempMDImageFolder.newFile("180784644740_0.gif");
        tempMDImageFolder.newFile("180784644740_1.gif");
        tempMDImageFolder.newFile("180254465582_0.gif");
        tempMDImageFolder.newFile("180254465582_1.gif");
        tempMDImageFolder.newFile("180254465582_2.gif");
        tempMDImageFolder.newFile("180254465582_3.gif");

        restTemplate.getForEntity(String.format("%s/admintools/posts/%s/cleanImagesOnHD", baseUri(), MAIN_BLOG_NAME),
                String.class);

        assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(3);
    }

    /**
     * Test importing images with non-post-related images
     *
     * @throws IOException
     */
    @Test
    public void importExtraImages() throws IOException {
        tempInputImageFolder.newFile("blah.txt");

        adminRestController.importImagesForBlog(MAIN_BLOG_NAME, tempInputImageFolder.getRoot().getAbsolutePath());

        assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(0);
    }

    /**
     * Test importing images with non-post-related images, via the REST intrface
     *
     * @throws IOException
     */
    @Test
    public void importExtraImagesRest() throws IOException {
        tempInputImageFolder.newFile("blah.txt");

        restTemplate.postForObject(String.format("%s/admintools/posts/%s/importImages", baseUri(), MAIN_BLOG_NAME),
                tempInputImageFolder.getRoot().getAbsolutePath(), String.class);

        assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(0);
    }

    /**
     * Test import where the right number of images exists
     *
     * @throws IOException
     */
    @Test
    public void importExactNumImages() throws IOException {
        tempInputImageFolder.newFile("180784644740_0.gif");
        tempInputImageFolder.newFile("180254465582_0.gif");
        tempInputImageFolder.newFile("180254465582_1.gif");

        adminRestController.importImagesForBlog(MAIN_BLOG_NAME, tempInputImageFolder.getRoot().getAbsolutePath());

        assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(3);
    }

    /**
     * Test import where the right number of images exists, via the REST interface
     *
     * @throws IOException
     */
    @Test
    public void importExactNumImagesRest() throws IOException {
        tempInputImageFolder.newFile("180784644740_0.gif");
        tempInputImageFolder.newFile("180254465582_0.gif");
        tempInputImageFolder.newFile("180254465582_1.gif");

        restTemplate.postForObject(String.format("%s/admintools/posts/%s/importImages", baseUri(), MAIN_BLOG_NAME),
                tempInputImageFolder.getRoot().getAbsolutePath(), String.class);

        assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(3);
    }

    /**
     * Test import where the input folder has duplicates
     *
     * @throws IOException
     */
    @Test
    public void importDoubleImages() throws IOException {
        tempInputImageFolder.newFile("180784644740_0.gif");
        tempInputImageFolder.newFile("180784644740_1.gif");
        tempInputImageFolder.newFile("180254465582_0.gif");
        tempInputImageFolder.newFile("180254465582_1.gif");
        tempInputImageFolder.newFile("180254465582_2.gif");
        tempInputImageFolder.newFile("180254465582_3.gif");

        adminRestController.importImagesForBlog(MAIN_BLOG_NAME, tempInputImageFolder.getRoot().getAbsolutePath());

        assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(3);
    }

    /**
     * Test import where the input folder has duplicates, via the REST interface
     *
     * @throws IOException
     */
    @Test
    public void importDoubleImagesRest() throws IOException {
        tempInputImageFolder.newFile("180784644740_0.gif");
        tempInputImageFolder.newFile("180784644740_1.gif");
        tempInputImageFolder.newFile("180254465582_0.gif");
        tempInputImageFolder.newFile("180254465582_1.gif");
        tempInputImageFolder.newFile("180254465582_2.gif");
        tempInputImageFolder.newFile("180254465582_3.gif");

        restTemplate.postForObject(String.format("%s/admintools/posts/%s/importImages", baseUri(), MAIN_BLOG_NAME),
                tempInputImageFolder.getRoot().getAbsolutePath(), String.class);

        assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(3);
    }

    /**
     * Test import when there are already images in the destination folder
     *
     * @throws IOException
     */
    @Test
    public void importWithExisting() throws IOException {
        tempMDImageFolder.newFile("180784644740_0.gif");
        tempMDImageFolder.newFile("180784644740_1.gif");
        tempInputImageFolder.newFile("180254465582_0.gif");
        tempInputImageFolder.newFile("180254465582_1.gif");
        tempInputImageFolder.newFile("180254465582_2.gif");
        tempInputImageFolder.newFile("180254465582_3.gif");

        adminRestController.importImagesForBlog(MAIN_BLOG_NAME, tempInputImageFolder.getRoot().getAbsolutePath());

        assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(3);
    }

    /**
     * Test import when there are already images in the destination folder, via the
     * REST interface
     *
     * @throws IOException
     */
    @Test
    public void importWithExistingRest() throws IOException {
        tempMDImageFolder.newFile("180784644740_0.gif");
        tempMDImageFolder.newFile("180784644740_1.gif");
        tempInputImageFolder.newFile("180254465582_0.gif");
        tempInputImageFolder.newFile("180254465582_1.gif");
        tempInputImageFolder.newFile("180254465582_2.gif");
        tempInputImageFolder.newFile("180254465582_3.gif");

        restTemplate.postForObject(String.format("%s/admintools/posts/%s/importImages", baseUri(), MAIN_BLOG_NAME),
                tempInputImageFolder.getRoot().getAbsolutePath(), String.class);

        assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(3);
    }

    /**
     * Tests functionality for marking all posts read
     */
    @Test
    public void markAllPostsRead() {
        List<Post> allPosts = postController.getAllPostsForBlog(MAIN_BLOG_NAME);
        assertThat(allPosts).isNotEmpty();

        for (Post p : allPosts) {
            p.setIsRead(false);
            postController.updatePostForBlog(p.getTumblelog(), p.getId(), p);
        }

        allPosts = postController.getAllPostsForBlog(MAIN_BLOG_NAME);
        assertThat(allPosts).isNotEmpty();
        for (Post p : allPosts) {
            assertThat(p.getIsRead()).isEqualTo(false);
        }

        adminRestController.markAllPostsReadForBlog(MAIN_BLOG_NAME);

        allPosts = postController.getAllPostsForBlog(MAIN_BLOG_NAME);
        assertThat(allPosts).isNotEmpty();
        for (Post p : allPosts) {
            assertThat(p.getIsRead()).isEqualTo(true);
        }
    }

    /**
     * Tests functionality for marking all posts read, via the REST interface
     */
    @Test
    public void markAllPostsReadRest() {
        Post[] allPosts = getAllPostsFromRest(MAIN_BLOG_NAME);
        assertThat(allPosts).isNotEmpty();

        for (Post p : allPosts) {
            p.setIsRead(false);
            restTemplate.put(String.format("%s/api/posts/%s/%s", baseUri(), MAIN_BLOG_NAME, p.getId()), p);
        }

        allPosts = getAllPostsFromRest(MAIN_BLOG_NAME);
        assertThat(allPosts).isNotEmpty();
        for (Post p : allPosts) {
            assertThat(p.getIsRead()).isEqualTo(false);
        }

        restTemplate.getForEntity(String.format("%s/admintools/posts/%s/markAllRead", baseUri(), MAIN_BLOG_NAME),
                String.class);

        allPosts = getAllPostsFromRest(MAIN_BLOG_NAME);
        assertThat(allPosts).isNotEmpty();
        for (Post p : allPosts) {
            assertThat(p.getIsRead()).isEqualTo(true);
        }
    }

    /**
     * Tests functionality for marking all posts unread
     */
    @Test
    public void markAllPostsUnread() {
        List<Post> allPosts = postController.getAllPostsForBlog(MAIN_BLOG_NAME);
        assertThat(allPosts).isNotEmpty();

        for (Post p : allPosts) {
            p.setIsRead(true);
            postController.updatePostForBlog(p.getTumblelog(), p.getId(), p);
        }

        allPosts = postController.getAllPostsForBlog(MAIN_BLOG_NAME);
        assertThat(allPosts).isNotEmpty();
        for (Post p : allPosts) {
            assertThat(p.getIsRead()).isEqualTo(true);
        }

        adminRestController.markAllPostsUnreadForBlog(MAIN_BLOG_NAME);

        allPosts = postController.getAllPostsForBlog(MAIN_BLOG_NAME);
        assertThat(allPosts).isNotEmpty();
        for (Post p : allPosts) {
            assertThat(p.getIsRead()).isEqualTo(false);
        }
    }

    /**
     * Tests functionality for marking all posts unread, via the REST interface
     */
    @Test
    public void markAllPostsUnreadRest() {
        Post[] allPosts = getAllPostsFromRest(MAIN_BLOG_NAME);
        assertThat(allPosts).isNotEmpty();

        for (Post p : allPosts) {
            p.setIsRead(true);
            restTemplate.put(String.format("%s/api/posts/%s/%s", baseUri(), MAIN_BLOG_NAME, p.getId()), p);
        }

        allPosts = getAllPostsFromRest(MAIN_BLOG_NAME);
        assertThat(allPosts).isNotEmpty();
        for (Post p : allPosts) {
            assertThat(p.getIsRead()).isEqualTo(true);
        }

        restTemplate.getForEntity(String.format("%s/admintools/posts/%s/markAllUnread", baseUri(), MAIN_BLOG_NAME),
                String.class);

        allPosts = getAllPostsFromRest(MAIN_BLOG_NAME);
        assertThat(allPosts).isNotEmpty();
        for (Post p : allPosts) {
            assertThat(p.getIsRead()).isEqualTo(false);
        }
    }

}
