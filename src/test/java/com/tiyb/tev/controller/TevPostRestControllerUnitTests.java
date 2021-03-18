package com.tiyb.tev.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import com.tiyb.tev.datamodel.Answer;
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.datamodel.Video;
import com.tiyb.tev.html.HtmlTestingClass;

/**
 * <p>
 * Test cases for unit testing the REST controller for working with Posts. Not
 * calling every single method/API in the controller, just the ones that involve
 * more logic. (i.e. we're not testing the underlying Spring Boot capabilities,
 * only the application's logic.)
 * </p>
 *
 * <p>
 * <code>updateXXX()</code> APIs <i>are</i> tested, however, to ensure that all
 * fields are always accounted for, since this is an easy place to make a
 * mistake. The logic is always the same for these tests: 1) Create empty post
 * with hard-coded ID; 2) update the fields on that post; 3) use the REST
 * controller to update the post in the DB; 4) use the REST API to retrieve that
 * same post; 5) verify all of the fields are the same
 * </p>
 *
 * <p>
 * This class has test cases for single-blog scenarios, and gets deeper in
 * individual parts of the functionality; the
 * {@link com.tiyb.tev.controller.TevMultiBlogPostUnitTests
 * TevPostRestControllerUnitTestsMultiBlogs} class has unit tests for multi-blog
 * scenarios.
 * </p>
 *
 */
public class TevPostRestControllerUnitTests extends HtmlTestingClass {

    @Autowired
    private TEVPostRestController postController;
    @Autowired
    private TEVMetadataRestController mdController;
    @Autowired
    private TestRestTemplate restTemplate;

    private static final String BLOG_NAME = "blog";

    /**
     * Initial cleanup/readiness
     */
    @Before
    public void setup() {
        postController.getPhotoController().deleteAllPhotosForBlog(BLOG_NAME);
        postController.getAnswerController().deleteAllAnswersForBlog(BLOG_NAME);
        postController.getLinkController().deleteAllLinksForBlog(BLOG_NAME);
        postController.getRegController().deleteAllRegularsForBlog(BLOG_NAME);
        postController.getVideoController().deleteAllVideosForBlog(BLOG_NAME);
        postController.deleteAllPostsForBlog(BLOG_NAME);
    }

    /**
     * Verifies that updating a Post properly updates all fields
     */
    @Test
    public void updatePost() {
        Post originalEmptyPost = new Post();
        originalEmptyPost.setId("1");
        originalEmptyPost.setTumblelog(BLOG_NAME);

        Post modifiedPost = postController.createPostForBlog(BLOG_NAME, originalEmptyPost);
        assertThat(modifiedPost).isNotNull();

        modifiedPost.setDate("Jan 1, 2019");
        modifiedPost.setIsReblog(true);
        modifiedPost.setSlug("New Slug");
        modifiedPost.setDateGmt("Date GMT");
        modifiedPost.setIsFavourite(true);
        modifiedPost.setIsRead(true);
        modifiedPost.setIsReblog(true);
        modifiedPost.setReblogKey("reblog Key");
        modifiedPost.setSlug("new slug");
        modifiedPost.setTags("hi, there");
        modifiedPost.setTumblelog(BLOG_NAME);
        modifiedPost.setUnixtimestamp(25L);
        modifiedPost.setUrl("URL");
        modifiedPost.setUrlWithSlug("URL with slug");
        modifiedPost.setState("published");

        postController.updatePostForBlog(BLOG_NAME, "1", modifiedPost);

        Post finalPostFromServer = postController.getPostForBlogById(BLOG_NAME, "1");

        assertThat(finalPostFromServer).isNotNull();
        assertThat(finalPostFromServer).isEqualToComparingFieldByField(modifiedPost);
    }

    /**
     * Verifies that updating a Post properly updates all fields, via REST
     */
    @Test
    public void updatePostRest() {
        Post originalEmptyPost = new Post();
        originalEmptyPost.setId("1");
        originalEmptyPost.setTumblelog(BLOG_NAME);

        Post modifiedPost = restTemplate.postForObject(String.format("%s/api/posts/%s", baseUri(), BLOG_NAME),
                originalEmptyPost, Post.class);
        assertThat(modifiedPost).isNotNull();

        modifiedPost.setDate("Jan 1, 2019");
        modifiedPost.setIsReblog(true);
        modifiedPost.setSlug("New Slug");
        modifiedPost.setDateGmt("Date GMT");
        modifiedPost.setIsFavourite(true);
        modifiedPost.setIsRead(true);
        modifiedPost.setIsReblog(true);
        modifiedPost.setReblogKey("reblog Key");
        modifiedPost.setSlug("new slug");
        modifiedPost.setTags("hi, there");
        modifiedPost.setTumblelog(BLOG_NAME);
        modifiedPost.setUnixtimestamp(25L);
        modifiedPost.setUrl("URL");
        modifiedPost.setUrlWithSlug("URL with slug");
        modifiedPost.setState("published");

        restTemplate.put(String.format("%s/api/posts/%s/%s", baseUri(), BLOG_NAME, "1"), modifiedPost);

        Post finalPostFromServer = restTemplate
                .getForObject(String.format("%s/api/posts/%s/%s", baseUri(), BLOG_NAME, "1"), Post.class);

        assertThat(finalPostFromServer).isNotNull();
        assertThat(finalPostFromServer).isEqualToComparingFieldByField(modifiedPost);
    }

    /**
     * Verify that marking a post "read" in the DB really does mark it read
     */
    @Test
    public void markPostRead() {
        Post originalPost = new Post();
        originalPost.setId("1");
        originalPost.setIsRead(false);
        originalPost.setTumblelog(BLOG_NAME);

        originalPost = postController.createPostForBlog(BLOG_NAME, originalPost);
        assertThat(originalPost).isNotNull();

        Post newPost = postController.markPostReadForBlog(BLOG_NAME, "1");
        assertThat(newPost).isNotNull();
        assertThat(newPost.getIsRead()).isEqualTo(true);

        Post finalPost = postController.getPostForBlogById(BLOG_NAME, "1");
        assertThat(finalPost).isNotNull();
        assertThat(finalPost.getIsRead()).isEqualTo(true);
    }

    /**
     * Verify that marking a post "read" in the DB really does mark it read, via
     * REST
     */
    @Test
    public void markPostReadRest() {
        Post originalPost = new Post();
        originalPost.setId("1");
        originalPost.setIsRead(false);
        originalPost.setTumblelog(BLOG_NAME);

        originalPost = restTemplate.postForObject(String.format("%s/api/posts/%s", baseUri(), BLOG_NAME), originalPost,
                Post.class);
        assertThat(originalPost).isNotNull();

        Post newPost = restTemplate
                .getForObject(String.format("%s/api/posts/%s/%s/markRead", baseUri(), BLOG_NAME, "1"), Post.class);
        assertThat(newPost).isNotNull();
        assertThat(newPost.getIsRead()).isEqualTo(true);

        Post finalPost = restTemplate.getForObject(String.format("%s/api/posts/%s/%s", baseUri(), BLOG_NAME, "1"),
                Post.class);
        assertThat(finalPost).isNotNull();
        assertThat(finalPost.getIsRead()).isEqualTo(true);
    }

    /**
     * Verify that marking a post "unread" in the DB really does mark it unread
     */
    @Test
    public void markPostUnread() {
        Post originalPost = new Post();
        originalPost.setId("1");
        originalPost.setIsRead(true);
        originalPost.setTumblelog(BLOG_NAME);

        originalPost = postController.createPostForBlog(BLOG_NAME, originalPost);
        assertThat(originalPost).isNotNull();

        Post newPost = postController.markPostUnreadForBlog(BLOG_NAME, "1");
        assertThat(newPost).isNotNull();
        assertThat(newPost.getIsRead()).isEqualTo(false);

        Post finalPost = postController.getPostForBlogById(BLOG_NAME, "1");
        assertThat(finalPost).isNotNull();
        assertThat(finalPost.getIsRead()).isEqualTo(false);
    }

    /**
     * Verify that marking a post "unread" in the DB really does mark it unread, via
     * REST
     */
    @Test
    public void markPostUnreadRest() {
        Post originalPost = new Post();
        originalPost.setId("1");
        originalPost.setIsRead(true);
        originalPost.setTumblelog(BLOG_NAME);

        originalPost = restTemplate.postForObject(String.format("%s/api/posts/%s", baseUri(), BLOG_NAME), originalPost,
                Post.class);
        assertThat(originalPost).isNotNull();

        Post newPost = restTemplate
                .getForObject(String.format("%s/api/posts/%s/%s/markUnread", baseUri(), BLOG_NAME, "1"), Post.class);
        assertThat(newPost).isNotNull();
        assertThat(newPost.getIsRead()).isEqualTo(false);

        Post finalPost = restTemplate.getForObject(String.format("%s/api/posts/%s/%s", baseUri(), BLOG_NAME, "1"),
                Post.class);
        assertThat(finalPost).isNotNull();
        assertThat(finalPost.getIsRead()).isEqualTo(false);
    }

    /**
     * Verify that marking a post a "favourite" in the DB really does mark it as a
     * favourite
     */
    @Test
    public void markPostFavourite() {
        Post originalPost = new Post();
        originalPost.setId("1");
        originalPost.setIsFavourite(false);
        originalPost.setTumblelog(BLOG_NAME);

        originalPost = postController.createPostForBlog(BLOG_NAME, originalPost);
        assertThat(originalPost).isNotNull();

        Post newPost = postController.markPostFavouriteForBlog(BLOG_NAME, "1");
        assertThat(newPost).isNotNull();
        assertThat(newPost.getIsFavourite()).isEqualTo(true);

        Post finalPost = postController.getPostForBlogById(BLOG_NAME, "1");
        assertThat(finalPost).isNotNull();
        assertThat(finalPost.getIsFavourite()).isEqualTo(true);
    }

    /**
     * Verify that marking a post a "favourite" in the DB really does mark it as a
     * favourite, via REST
     */
    @Test
    public void markPostFavouriteRest() {
        Post originalPost = new Post();
        originalPost.setId("1");
        originalPost.setIsFavourite(false);
        originalPost.setTumblelog(BLOG_NAME);

        originalPost = restTemplate.postForObject(String.format("%s/api/posts/%s", baseUri(), BLOG_NAME), originalPost,
                Post.class);
        assertThat(originalPost).isNotNull();

        Post newPost = restTemplate
                .getForObject(String.format("%s/api/posts/%s/%s/markFavourite", baseUri(), BLOG_NAME, "1"), Post.class);
        assertThat(newPost).isNotNull();
        assertThat(newPost.getIsFavourite()).isEqualTo(true);

        Post finalPost = restTemplate.getForObject(String.format("%s/api/posts/%s/%s", baseUri(), BLOG_NAME, "1"),
                Post.class);
        assertThat(finalPost).isNotNull();
        assertThat(finalPost.getIsFavourite()).isEqualTo(true);
    }

    /**
     * Verify that marking a post not a "favourite" in the DB really does mark it as
     * not a favourite
     */
    @Test
    public void markPostNotFavourite() {
        Post originalPost = new Post();
        originalPost.setId("1");
        originalPost.setIsFavourite(true);
        originalPost.setTumblelog(BLOG_NAME);

        originalPost = postController.createPostForBlog(BLOG_NAME, originalPost);
        assertThat(originalPost).isNotNull();

        Post newPost = postController.markPostNonFavouriteForBlog(BLOG_NAME, "1");
        assertThat(newPost).isNotNull();
        assertThat(newPost.getIsFavourite()).isEqualTo(false);

        Post finalPost = postController.getPostForBlogById(BLOG_NAME, "1");
        assertThat(finalPost).isNotNull();
        assertThat(finalPost.getIsFavourite()).isEqualTo(false);
    }

    /**
     * Verify that marking a post not a "favourite" in the DB really does mark it as
     * not a favourite, via REST
     */
    @Test
    public void markPostNotFavouriteRest() {
        Post originalPost = new Post();
        originalPost.setId("1");
        originalPost.setIsFavourite(true);
        originalPost.setTumblelog(BLOG_NAME);

        originalPost = restTemplate.postForObject(String.format("%s/api/posts/%s", baseUri(), BLOG_NAME), originalPost,
                Post.class);
        assertThat(originalPost).isNotNull();

        Post newPost = restTemplate.getForObject(
                String.format("%s/api/posts/%s/%s/markNonFavourite", baseUri(), BLOG_NAME, "1"), Post.class);
        assertThat(newPost).isNotNull();
        assertThat(newPost.getIsFavourite()).isEqualTo(false);

        Post finalPost = restTemplate.getForObject(String.format("%s/api/posts/%s/%s", baseUri(), BLOG_NAME, "1"),
                Post.class);
        assertThat(finalPost).isNotNull();
        assertThat(finalPost.getIsFavourite()).isEqualTo(false);
    }

    /**
     * Verifies that updating an Answer properly updates all fields
     */
    @Test
    public void updateAnswer() {
        Post parentPost = new Post();
        parentPost.setId("1");
        parentPost.setTumblelog(BLOG_NAME);
        postController.createPostForBlog(BLOG_NAME, parentPost);
        Answer originalAns = new Answer();
        originalAns.setPostId("1");
        originalAns.setQuestion("original question");
        originalAns.setAnswer("original answer");

        Answer modifiedAnswer = postController.getAnswerController().createAnswerForBlog(BLOG_NAME, "1", originalAns);
        assertThat(modifiedAnswer).isNotNull();

        modifiedAnswer.setAnswer("new answer");
        modifiedAnswer.setQuestion("new question");

        postController.getAnswerController().updateAnswerForBlog(BLOG_NAME, "1", modifiedAnswer);

        Answer finalFromServer = postController.getAnswerController().getAnswerForBlogById(BLOG_NAME, "1");

        assertThat(finalFromServer).isNotNull();
        assertThat(finalFromServer).isEqualToComparingFieldByField(modifiedAnswer);
    }

    /**
     * Verifies that updating an Answer properly updates all fields, via REST
     */
    @Test
    public void updateAnswerRest() {
        Post parentPost = new Post();
        parentPost.setId("1");
        parentPost.setTumblelog(BLOG_NAME);
        restTemplate.postForObject(String.format("%s/api/posts/%s", baseUri(), BLOG_NAME), parentPost, Post.class);
        Answer originalAns = new Answer();
        originalAns.setPostId("1");
        originalAns.setQuestion("original question");
        originalAns.setAnswer("original answer");

        Answer modifiedAnswer = restTemplate.postForObject(
                String.format("%s/api/posts/%s/%s/answer", baseUri(), BLOG_NAME, "1"), originalAns, Answer.class);
        assertThat(modifiedAnswer).isNotNull();

        modifiedAnswer.setAnswer("new answer");
        modifiedAnswer.setQuestion("new question");

        restTemplate.put(String.format("%s/api/posts/%s/%s/answer", baseUri(), BLOG_NAME, "1"), modifiedAnswer);

        Answer finalFromServer = restTemplate
                .getForObject(String.format("%s/api/posts/%s/%s/answer", baseUri(), BLOG_NAME, "1"), Answer.class);

        assertThat(finalFromServer).isNotNull();
        assertThat(finalFromServer).isEqualToComparingFieldByField(modifiedAnswer);
    }

    /**
     * Verifies that updating a Link properly updates all fields
     */
    @Test
    public void updateLink() {
        Post parentPost = new Post();
        parentPost.setId("1");
        parentPost.setTumblelog(BLOG_NAME);
        postController.createPostForBlog(parentPost.getTumblelog(), parentPost);
        Link original = new Link();
        original.setPostId("1");
        original.setUrl("original url");

        Link modifiedLink = postController.getLinkController().createLinkForBlog(BLOG_NAME, "1", original);
        assertThat(modifiedLink).isNotNull();

        modifiedLink.setDescription("new description");
        modifiedLink.setText("new link text");
        modifiedLink.setUrl("new url");

        postController.getLinkController().updateLinkForBlog(BLOG_NAME, "1", modifiedLink);

        Link finalFromServer = postController.getLinkController().getLinkForBlogById(BLOG_NAME, "1");

        assertThat(finalFromServer).isNotNull();
        assertThat(finalFromServer).isEqualToComparingFieldByField(modifiedLink);
    }

    /**
     * Verifies that updating a Link properly updates all fields, via REST
     */
    @Test
    public void updateLinkRest() {
        Post parentPost = new Post();
        parentPost.setId("1");
        parentPost.setTumblelog(BLOG_NAME);
        restTemplate.postForObject(String.format("%s/api/posts/%s", baseUri(), BLOG_NAME), parentPost, Post.class);
        Link original = new Link();
        original.setPostId("1");
        original.setUrl("original url");

        Link modifiedLink = restTemplate.postForObject(
                String.format("%s/api/posts/%s/%s/link", baseUri(), BLOG_NAME, "1"), original, Link.class);
        assertThat(modifiedLink).isNotNull();

        modifiedLink.setDescription("new description");
        modifiedLink.setText("new link text");
        modifiedLink.setUrl("new url");

        restTemplate.put(String.format("%s/api/posts/%s/%s/link", baseUri(), BLOG_NAME, "1"), modifiedLink);

        Link finalFromServer = restTemplate
                .getForObject(String.format("%s/api/posts/%s/%s/link", baseUri(), BLOG_NAME, "1"), Link.class);

        assertThat(finalFromServer).isNotNull();
        assertThat(finalFromServer).isEqualToComparingFieldByField(modifiedLink);
    }

    /**
     * Verifies that updating a Photo properly updates all fields
     */
    @Test
    public void updatePhoto() {
        Post parentPost = new Post();
        parentPost.setId("1");
        parentPost.setTumblelog(BLOG_NAME);
        parentPost.setType(Post.POST_TYPE_PHOTO);
        postController.createPostForBlog(parentPost.getTumblelog(), parentPost);
        Photo original = new Photo();
        original.setPostId("1");

        Photo modifiedPhoto = postController.getPhotoController().createPhotoForBlog(BLOG_NAME, original);
        assertThat(modifiedPhoto).isNotNull();

        modifiedPhoto.setCaption("new caption");
        modifiedPhoto.setHeight(2);
        modifiedPhoto.setOffset("new offset");
        modifiedPhoto.setPhotoLinkUrl("new photo link url");
        modifiedPhoto.setUrl100("url 100");
        modifiedPhoto.setUrl1280("url 1280");
        modifiedPhoto.setUrl250("url 250");
        modifiedPhoto.setUrl400("url 400");
        modifiedPhoto.setUrl500("url 500");
        modifiedPhoto.setUrl75("url 75");
        modifiedPhoto.setWidth(3);

        postController.getPhotoController().updatePhotoForBlog(BLOG_NAME, modifiedPhoto.getPostId(), modifiedPhoto);

        List<Photo> finalFromServer = postController.getPhotoController().getPhotoForBlogById(BLOG_NAME, "1");
        assertThat(finalFromServer).isNotNull();
        assertThat(finalFromServer.size()).isEqualTo(1);
        assertThat(finalFromServer.get(0)).isEqualToComparingFieldByField(modifiedPhoto);
    }

    /**
     * Verifies that updating a Photo properly updates all fields, via REST
     */
    @Test
    public void updatePhotoRest() {
        Post parentPost = new Post();
        parentPost.setId("1");
        parentPost.setTumblelog(BLOG_NAME);
        parentPost.setType(Post.POST_TYPE_PHOTO);
        restTemplate.postForObject(String.format("%s/api/posts/%s", baseUri(), BLOG_NAME), parentPost, Post.class);
        Photo original = new Photo();
        original.setPostId("1");

        Photo modifiedPhoto = restTemplate.postForObject(String.format("%s/api/posts/%s/photo", baseUri(), BLOG_NAME),
                original, Photo.class);
        assertThat(modifiedPhoto).isNotNull();

        modifiedPhoto.setCaption("new caption");
        modifiedPhoto.setHeight(2);
        modifiedPhoto.setOffset("new offset");
        modifiedPhoto.setPhotoLinkUrl("new photo link url");
        modifiedPhoto.setUrl100("url 100");
        modifiedPhoto.setUrl1280("url 1280");
        modifiedPhoto.setUrl250("url 250");
        modifiedPhoto.setUrl400("url 400");
        modifiedPhoto.setUrl500("url 500");
        modifiedPhoto.setUrl75("url 75");
        modifiedPhoto.setWidth(3);

        restTemplate.put(String.format("%s/api/posts/%s/%s/photo", baseUri(), BLOG_NAME, "1"), modifiedPhoto);

        ResponseEntity<Photo[]> responseEntity = restTemplate
                .getForEntity(String.format("%s/api/posts/%s/%s/photo", baseUri(), BLOG_NAME, "1"), Photo[].class);
        Photo[] finalFromServer = responseEntity.getBody();
        assertThat(finalFromServer).isNotEmpty();
        assertThat(finalFromServer.length).isEqualTo(1);
        assertThat(finalFromServer[0]).isEqualToComparingFieldByField(modifiedPhoto);
    }

    /**
     * Verifies that updating a Regular properly updates all fields
     */
    @Test
    public void updateRegular() {
        Post parentPost = new Post();
        parentPost.setId("1");
        parentPost.setTumblelog(BLOG_NAME);
        postController.createPostForBlog(parentPost.getTumblelog(), parentPost);
        Regular original = new Regular();
        original.setPostId("1");

        Regular modified = postController.getRegController().createRegularForBlog(BLOG_NAME, "1", original);
        assertThat(modified).isNotNull();

        modified.setBody("new body");
        modified.setTitle("new title");

        postController.getRegController().updateRegularForBlog(BLOG_NAME, "1", modified);

        Regular finalFromServer = postController.getRegController().getRegularForBlogById(BLOG_NAME, "1");

        assertThat(finalFromServer).isNotNull();
        assertThat(finalFromServer).isEqualToComparingFieldByField(modified);
    }

    /**
     * Verifies that updating a Regular properly updates all fields, via REST
     */
    @Test
    public void updateRegularRest() {
        Post parentPost = new Post();
        parentPost.setId("1");
        parentPost.setTumblelog(BLOG_NAME);
        restTemplate.postForObject(String.format("%s/api/posts/%s", baseUri(), BLOG_NAME), parentPost, Post.class);
        Regular original = new Regular();
        original.setPostId("1");

        Regular modified = restTemplate.postForObject(
                String.format("%s/api/posts/%s/%s/regular", baseUri(), BLOG_NAME, "1"), original, Regular.class);
        assertThat(modified).isNotNull();

        modified.setBody("new body");
        modified.setTitle("new title");

        restTemplate.put(String.format("%s/api/posts/%s/%s/regular", baseUri(), BLOG_NAME, "1"), modified);

        Regular finalFromServer = restTemplate
                .getForObject(String.format("%s/api/posts/%s/%s/regular", baseUri(), BLOG_NAME, "1"), Regular.class);

        assertThat(finalFromServer).isNotNull();
        assertThat(finalFromServer).isEqualToComparingFieldByField(modified);
    }

    /**
     * Verifies that updating a Video properly updates all fields
     */
    @Test
    public void updateVideo() {
        Post parentPost = new Post();
        parentPost.setId("1");
        parentPost.setTumblelog(BLOG_NAME);
        postController.createPostForBlog(parentPost.getTumblelog(), parentPost);
        Video original = new Video();
        original.setPostId("1");

        Video modified = postController.getVideoController().createVideoForBlog(parentPost.getTumblelog(), "1",
                original);
        assertThat(modified).isNotNull();

        modified.setContentType("content Type");
        modified.setDuration(20);
        modified.setExtension("ext");
        modified.setHeight(30);
        modified.setRevision("revision");
        modified.setVideoCaption("video caption");
        modified.setWidth(40);

        postController.getVideoController().updateVideoForBlog(parentPost.getTumblelog(), "1", modified);

        Video finalFromServer = postController.getVideoController().getVideoForBlogById(parentPost.getTumblelog(), "1");

        assertThat(finalFromServer).isNotNull();
        assertThat(finalFromServer).isEqualToComparingFieldByField(modified);
    }

    /**
     * Verifies that updating a Video properly updates all fields, via REST
     */
    @Test
    public void updateVideoRest() {
        Post parentPost = new Post();
        parentPost.setId("1");
        parentPost.setTumblelog(BLOG_NAME);
        restTemplate.postForObject(String.format("%s/api/posts/%s", baseUri(), BLOG_NAME), parentPost, Post.class);
        Video original = new Video();
        original.setPostId("1");

        Video modified = restTemplate.postForObject(
                String.format("%s/api/posts/%s/%s/video", baseUri(), BLOG_NAME, "1"), original, Video.class);
        assertThat(modified).isNotNull();

        modified.setContentType("content Type");
        modified.setDuration(20);
        modified.setExtension("ext");
        modified.setHeight(30);
        modified.setRevision("revision");
        modified.setVideoCaption("video caption");
        modified.setWidth(40);

        restTemplate.put(String.format("%s/api/posts/%s/%s/video", baseUri(), BLOG_NAME, "1"), modified);

        Video finalFromServer = restTemplate
                .getForObject(String.format("%s/api/posts/%s/%s/video", baseUri(), BLOG_NAME, "1"), Video.class);

        assertThat(finalFromServer).isNotNull();
        assertThat(finalFromServer).isEqualToComparingFieldByField(modified);
    }

    /**
     * Verifies that retrieving the list of Types from the system returns the proper
     * number of items
     */
    @Test
    public void getTypes() {
        List<String> originalTypes = mdController.getAllTypes();
        assertThat(originalTypes).isNotNull();
        assertThat(originalTypes.size()).isEqualTo(5);
    }

    /**
     * Verifies that retrieving the list of Types from the system returns the proper
     * number of items, via REST
     */
    @Test
    public void getTypesRest() {
        ResponseEntity<String[]> responseEntity = restTemplate.getForEntity(String.format("%s/api/types", baseUri()),
                String[].class);
        String[] originalTypes = responseEntity.getBody();
        assertThat(originalTypes).isNotNull();
        assertThat(originalTypes.length).isEqualTo(5);
    }

    /**
     * Verifies that updating Metadata properly updates all fields
     */
    @Test
    public void updateMetadata() {
        Metadata md = mdController.getMetadataForBlogOrDefault("blogName");
        assertThat(md).isNotNull();

        md.setBaseMediaPath("new base media path");
        md.setFavFilter("new fav filter");
        md.setFilter("new filter");
        md.setMainTumblrUser("main tumblr user");
        md.setPageLength(25);
        md.setSortColumn("sort column");
        md.setSortOrder("sort order");
        md.setShowReadingPane(true);
        md.setOverwritePostData(true);
        md.setOverwriteConvoData(true);

        mdController.updateMetadata(md.getId(), md);

        Metadata finalFromServer = mdController.getMetadataByID(md.getId());

        assertThat(finalFromServer).isNotNull();
        assertThat(finalFromServer).isEqualToComparingFieldByField(md);
    }

    /**
     * Verifies that updating Metadata properly updates all fields, via REST
     */
    @Test
    public void updateMetadataRest() {
        Metadata md = restTemplate.getForObject(
                String.format("%s/api/metadata/byBlog/%s/orDefault", baseUri(), "blogName"), Metadata.class);
        assertThat(md).isNotNull();

        md.setBaseMediaPath("new base media path");
        md.setFavFilter("new fav filter");
        md.setFilter("new filter");
        md.setMainTumblrUser("main tumblr user");
        md.setPageLength(25);
        md.setSortColumn("sort column");
        md.setSortOrder("sort order");
        md.setShowReadingPane(true);
        md.setOverwritePostData(true);
        md.setOverwriteConvoData(true);

        restTemplate.put(String.format("%s/api/metadata/%d", baseUri(), md.getId()), md);

        Metadata finalFromServer = restTemplate.getForObject(String.format("%s/api/metadata/%d", baseUri(), md.getId()),
                Metadata.class);

        assertThat(finalFromServer).isNotNull();
        assertThat(finalFromServer).isEqualToComparingFieldByField(md);
    }

}
