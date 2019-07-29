package com.tiyb.tev.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Arrays;

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
import com.tiyb.tev.datamodel.Answer;
import com.tiyb.tev.datamodel.Hashtag;
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.datamodel.Video;
import com.tiyb.tev.exception.InvalidTypeException;
import com.tiyb.tev.exception.ResourceNotFoundException;
import com.tiyb.tev.exception.XMLParsingException;

/**
 * <p>
 * Unit Tests for the <code>BlogXmlReader</code> class. Since that class not
 * only parses the XML document but also inserts the data into the DB (via the
 * REST controller), these Unit Tests verify the end result -- that the data is
 * properly inserted into the DB.
 * </p>
 * 
 * <p>
 * A <code>test-post-xml</code> XML input document is used for input data for
 * most unit tests, while <code>test-post-extended-xml</code> is used for one
 * particular test, to verify that additive inserts are working properly.
 * </p>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PostXmlParsingUnitTest {

	@Autowired
	private TEVPostRestController postController;
	@Autowired
	private TEVMetadataRestController mdController;

	private static final long answerPostID = 180371366195L;
	private static final long linkPostID = 180265557725L;
	private static final long regularPostID = 180894436671L;
	private static final long videoPostID = 180782992914L;
	private static final long firstPhotoPostID = 180784644740L;
	private static final long secondPhotoPostID = 180254465582L;
	private static final long addedRegularPostID = 180894436672L;
	private static final long draftRegularPostID = 190097591599L;
	private static final long queuedRegularPostID = 778563537472L;
	
	private static final int ORIGINAL_NUM_POSTS = 7;
	private static final int ORIGINAL_NUM_REG_POSTS = 2;
	private static final int EXTENDED_NUM_POSTS = 8;
	private static final int EXTENDED_NUM_REG_POSTS = 3;

	private static final List<Hashtag> INITIAL_HASHTAGS = Arrays.asList(new Hashtag("tag1", 2), new Hashtag("tag2", 2),
			new Hashtag("tag3", 1), new Hashtag("tag4", 1), new Hashtag("tag5", 1), new Hashtag("tag6", 1),
			new Hashtag("tag7", 1), new Hashtag("tag8", 1), new Hashtag("tag9", 1), new Hashtag("tag10", 1),
			new Hashtag("tag11", 1), new Hashtag("tag12", 1), new Hashtag("tag13", 1), new Hashtag("tag14", 1),
			new Hashtag("tag15", 1));

	private static final List<Hashtag> REVISED_HASHTAGS = Arrays.asList(new Hashtag("tag1", 3), new Hashtag("tag2", 3),
			new Hashtag("tag3", 1), new Hashtag("tag4", 1), new Hashtag("tag5", 1), new Hashtag("tag6", 1),
			new Hashtag("tag7", 1), new Hashtag("tag8", 1), new Hashtag("tag9", 1), new Hashtag("tag10", 1),
			new Hashtag("tag11", 1), new Hashtag("tag12", 1), new Hashtag("tag13", 1), new Hashtag("tag14", 1),
			new Hashtag("tag15", 1));

	/**
	 * Called before each unit test to properly reset the data back to an original
	 * state of having loaded the test XML document.
	 * 
	 * @throws FileNotFoundException
	 */
	@Before
	public void setupData() throws FileNotFoundException {
		File rawXmlFile = ResourceUtils.getFile("classpath:XML/test-post-xml.xml");
		InputStream xmlFile = new FileInputStream(rawXmlFile);

		Metadata md = mdController.getMetadata();
		md.setOverwritePostData(true);
		md = mdController.updateMetadata(md);

		BlogXmlReader.parseDocument(xmlFile, postController, mdController);
	}

	/**
	 * Simple check that all posts have been loaded; details are checked in other
	 * unit tests
	 */
	@Test
	public void testAllPosts() {
		List<Post> posts = postController.getAllPosts();
		assertThat(posts).isNotNull();
		assertThat(posts.size()).isEqualTo(ORIGINAL_NUM_POSTS);

		List<Post> answerPosts = postController.getPostsByType("answer");
		assertThat(answerPosts).isNotNull();
		assertThat(answerPosts.size()).isEqualTo(1);

		List<Post> photoPosts = postController.getPostsByType("photo");
		assertThat(photoPosts).isNotNull();
		assertThat(photoPosts.size()).isEqualTo(2);

		List<Post> regularPosts = postController.getPostsByType("regular");
		assertThat(regularPosts).isNotNull();
		assertThat(regularPosts.size()).isEqualTo(ORIGINAL_NUM_REG_POSTS);

		List<Post> linkPosts = postController.getPostsByType("link");
		assertThat(linkPosts).isNotNull();
		assertThat(linkPosts.size()).isEqualTo(1);

		List<Post> videoPosts = postController.getPostsByType("video");
		assertThat(videoPosts).isNotNull();
		assertThat(videoPosts.size()).isEqualTo(1);
	}

	/**
	 * Tests that a proper exception is thrown when an invalid type is passed to
	 * {@link com.tiyb.tev.controller.TEVPostRestController#getPostsByType(String)
	 * TEVPostRestController#getPostsByType(String)}
	 */
	@Test(expected = InvalidTypeException.class)
	public void testInvalidPostType() {
		postController.getPostsByType("blah");
	}

	/**
	 * Verify that test Answers in the input XML have been properly inserted into
	 * the DB (there is one)
	 */
	@Test
	public void testAnswer() {
		Post post = postController.getPostById(answerPostID);
		assertThat(post).isNotNull();
		assertThat(post.getDate()).isEqualTo("Thu, 22 Nov 2018 03:26:25");
		assertThat(post.getDateGmt()).isEqualTo("2018-11-22 08:26:25 GMT");
		assertThat(post.getIsFavourite()).isEqualTo(false);
		assertThat(post.getIsRead()).isEqualTo(false);
		assertThat(post.getIsReblog()).isEqualTo(true);
		assertThat(post.getReblogKey()).isEqualTo("OQqor1Zh");
		assertThat(post.getSlug()).isEqualTo("slug-slug-slug");
		assertThat(post.getState()).isEqualTo("published");
		assertThat(post.getTags()).isEqualTo("tag2");
		assertThat(post.getTumblelog()).isEqualTo("mainblog");
		assertThat(post.getType()).isEqualTo("answer");
		assertThat(post.getUnixtimestamp()).isEqualTo(1542875185L);
		assertThat(post.getUrl()).isEqualTo("https://mainblog.tumblr.com/post/180371366195");
		assertThat(post.getUrlWithSlug()).isEqualTo("https://mainblog.tumblr.com/post/180371366195/slug-slug-slug");

		assertThat(postController.getAllAnswers().size()).isEqualTo(1);
		Answer answer = postController.getAnswerById(answerPostID);
		assertThat(answer).isNotNull();
		assertThat(answer.getQuestion()).isEqualTo("Question text");
		assertThat(answer.getAnswer()).isEqualTo("Answer text");
		assertThat(answer.getPostId()).isEqualTo(answerPostID);
	}

	/**
	 * Verify that test Links in the input XML have been properly inserted into the
	 * DB (there is one)
	 */
	@Test
	public void testLink() {
		Post post = postController.getPostById(linkPostID);
		assertThat(post).isNotNull();
		assertThat(post.getDate()).isEqualTo("Mon, 19 Nov 2018 01:09:08");
		assertThat(post.getDateGmt()).isEqualTo("2018-11-19 06:09:08 GMT");
		assertThat(post.getIsFavourite()).isEqualTo(false);
		assertThat(post.getIsRead()).isEqualTo(false);
		assertThat(post.getIsReblog()).isEqualTo(false);
		assertThat(post.getReblogKey()).isEqualTo("6pFgAxH2");
		assertThat(post.getSlug()).isEqualTo("tumblr");
		assertThat(post.getState()).isEqualTo("published");
		assertThat(post.getTags()).isEqualTo("tag1");
		assertThat(post.getTumblelog()).isEqualTo("mainblog");
		assertThat(post.getType()).isEqualTo("link");
		assertThat(post.getUnixtimestamp()).isEqualTo(1542607748L);
		assertThat(post.getUrl()).isEqualTo("https://mainblog.tumblr.com/post/180265557725");
		assertThat(post.getUrlWithSlug()).isEqualTo("https://mainblog.tumblr.com/post/180265557725/tumblr");

		assertThat(postController.getAllLinks().size()).isEqualTo(1);
		Link link = postController.getLinkById(linkPostID);
		assertThat(link).isNotNull();
		assertThat(link.getPostId()).isEqualTo(linkPostID);
		assertThat(link.getDescription()).isEqualTo("This is the link description");
		assertThat(link.getText()).isEqualTo("Tumblr");
		assertThat(link.getUrl()).isEqualTo("https://someblog.tumblr.com/");
	}

	/**
	 * Verify that test Regulars in the input XML have been properly inserted into
	 * the DB (there is one)
	 */
	@Test
	public void testRegular() {
		Post post = postController.getPostById(regularPostID);
		assertThat(post).isNotNull();
		assertThat(post.getDate()).isEqualTo("Fri, 07 Dec 2018 11:48:43");
		assertThat(post.getDateGmt()).isEqualTo("2018-12-07 16:48:43 GMT");
		assertThat(post.getIsFavourite()).isEqualTo(false);
		assertThat(post.getIsRead()).isEqualTo(false);
		assertThat(post.getIsReblog()).isEqualTo(true);
		assertThat(post.getReblogKey()).isEqualTo("O6pLVlp1");
		assertThat(post.getSlug()).isEqualTo("first-post");
		assertThat(post.getState()).isEqualTo("published");
		assertThat(post.getTags()).isEqualTo("tag1, tag2");
		assertThat(post.getTumblelog()).isEqualTo("mainblog");
		assertThat(post.getType()).isEqualTo("regular");
		assertThat(post.getUnixtimestamp()).isEqualTo(1544201323L);
		assertThat(post.getUrl()).isEqualTo("https://mainblog.tumblr.com/post/180894436671");
		assertThat(post.getUrlWithSlug()).isEqualTo("https://mainblog.tumblr.com/post/180894436671/first-post");

		assertThat(postController.getAllRegulars().size()).isEqualTo(ORIGINAL_NUM_REG_POSTS);
		Regular regular = postController.getRegularById(regularPostID);
		assertThat(regular).isNotNull();
		assertThat(regular.getPostId()).isEqualTo(regularPostID);
		assertThat(regular.getBody()).isEqualTo("post body text here");
		assertThat(regular.getTitle()).isEqualTo("First Post");
	}

	/**
	 * Tests that a draft post was skipped during the import process
	 */
	@Test(expected = ResourceNotFoundException.class)
	public void testIgnoredDraft() {
		postController.getPostById(draftRegularPostID);
	}

	/**
	 * Tests that a queued post was skipped during the import process
	 */
	@Test(expected = ResourceNotFoundException.class)
	public void testIgnoredQueued() {
		postController.getPostById(queuedRegularPostID);
	}

	/**
	 * Verify that test Videos in the input XML have been properly inserted into the
	 * DB (there is one)
	 */
	@Test
	public void testVideo() {
		Post post = postController.getPostById(videoPostID);
		assertThat(post).isNotNull();
		assertThat(post.getDate()).isEqualTo("Tue, 04 Dec 2018 01:09:21");
		assertThat(post.getDateGmt()).isEqualTo("2018-12-04 06:09:21 GMT");
		assertThat(post.getIsFavourite()).isEqualTo(false);
		assertThat(post.getIsRead()).isEqualTo(false);
		assertThat(post.getIsReblog()).isEqualTo(true);
		assertThat(post.getReblogKey()).isEqualTo("IPc1CZyV");
		assertThat(post.getSlug()).isEqualTo("another-slug");
		assertThat(post.getState()).isEqualTo("published");
		assertThat(post.getTags()).isEqualTo("tag5, tag6, tag7, tag8, tag9, tag10");
		assertThat(post.getTumblelog()).isEqualTo("mainblog");
		assertThat(post.getType()).isEqualTo("video");
		assertThat(post.getUnixtimestamp()).isEqualTo(1543903761L);
		assertThat(post.getUrl()).isEqualTo("https://mainblog.tumblr.com/post/180782992914");
		assertThat(post.getUrlWithSlug()).isEqualTo("https://mainblog.tumblr.com/post/180782992914/another-slug");

		assertThat(postController.getAllVideos().size()).isEqualTo(1);
		Video video = postController.getVideoById(videoPostID);
		assertThat(video).isNotNull();
		assertThat(video.getContentType()).isEqualTo("video/mp4");
		assertThat(video.getDuration()).isEqualTo(45);
		assertThat(video.getExtension()).isEqualTo("mp4");
		assertThat(video.getHeight()).isEqualTo(480);
		assertThat(video.getPostId()).isEqualTo(videoPostID);
		assertThat(video.getRevision()).isEqualTo("0");
		assertThat(video.getVideoCaption()).isEqualTo("This is the caption for a video");
		assertThat(video.getWidth()).isEqualTo(854);
	}

	/**
	 * Verify that test Photos in the input XML have been properly inserted into the
	 * DB (there are three, spread across two posts)
	 */
	@Test
	public void testPhotos() {
		Post post = postController.getPostById(firstPhotoPostID);
		assertThat(post).isNotNull();
		assertThat(post.getDate()).isEqualTo("Tue, 04 Dec 2018 02:17:52");
		assertThat(post.getDateGmt()).isEqualTo("2018-12-04 07:17:52 GMT");
		assertThat(post.getIsFavourite()).isEqualTo(false);
		assertThat(post.getIsRead()).isEqualTo(false);
		assertThat(post.getIsReblog()).isEqualTo(true);
		assertThat(post.getReblogKey()).isEqualTo("Pius5FOw");
		assertThat(post.getSlug()).isEqualTo("new-slug");
		assertThat(post.getState()).isEqualTo("published");
		assertThat(post.getTags()).isEqualTo("tag3, tag4");
		assertThat(post.getTumblelog()).isEqualTo("mainblog");
		assertThat(post.getType()).isEqualTo("photo");
		assertThat(post.getUnixtimestamp()).isEqualTo(1543907872L);
		assertThat(post.getUrl()).isEqualTo("https://mainblog.tumblr.com/post/180784644740");
		assertThat(post.getUrlWithSlug()).isEqualTo("https://mainblog.tumblr.com/post/180784644740/new-slug");

		post = postController.getPostById(secondPhotoPostID);
		assertThat(post).isNotNull();
		assertThat(post.getDate()).isEqualTo("Sun, 18 Nov 2018 18:17:36");
		assertThat(post.getDateGmt()).isEqualTo("2018-11-18 23:17:36 GMT");
		assertThat(post.getIsFavourite()).isEqualTo(false);
		assertThat(post.getIsRead()).isEqualTo(false);
		assertThat(post.getIsReblog()).isEqualTo(true);
		assertThat(post.getReblogKey()).isEqualTo("jTxuwC0o");
		assertThat(post.getSlug()).isEqualTo("slugs-are-delicious");
		assertThat(post.getState()).isEqualTo("published");
		assertThat(post.getTags()).isEqualTo("tag11, tag12, tag13, tag14, tag15");
		assertThat(post.getTumblelog()).isEqualTo("mainblog");
		assertThat(post.getType()).isEqualTo("photo");
		assertThat(post.getUnixtimestamp()).isEqualTo(1542583056L);
		assertThat(post.getUrl()).isEqualTo("https://mainblog.tumblr.com/post/180254465582");
		assertThat(post.getUrlWithSlug())
				.isEqualTo("https://mainblog.tumblr.com/post/180254465582/slugs-are-delicious");

		List<Photo> photos = postController.getPhotoById(secondPhotoPostID);
		assertThat(photos.size()).isEqualTo(2);

		Photo photo = photos.get(0);
		assertThat(photo.getPostId()).isEqualTo(secondPhotoPostID);
		assertThat(photo.getCaption()).isEqualTo("This is hte photo caption");
		assertThat(photo.getUrl1280()).isEqualTo("photo 3 1280");
		assertThat(photo.getUrl500()).isEqualTo("photo 3 500");
		assertThat(photo.getUrl400()).isEqualTo("photo 3 400");
		assertThat(photo.getUrl250()).isEqualTo("photo 3 250");
		assertThat(photo.getUrl100()).isEqualTo("photo 3 100");
		assertThat(photo.getUrl75()).isEqualTo("photo 3 75");

		photo = photos.get(1);
		assertThat(photo.getPostId()).isEqualTo(secondPhotoPostID);
		assertThat(photo.getCaption()).isEqualTo("This is hte photo caption");
		assertThat(photo.getUrl1280()).isEqualTo("photo 4 1280");
		assertThat(photo.getUrl500()).isEqualTo("photo 4 500");
		assertThat(photo.getUrl400()).isEqualTo("photo 4 400");
		assertThat(photo.getUrl250()).isEqualTo("photo 4 250");
		assertThat(photo.getUrl100()).isEqualTo("photo 4 100");
		assertThat(photo.getUrl75()).isEqualTo("photo 4 75");

		photos = postController.getPhotoById(firstPhotoPostID);
		assertThat(photos.size()).isEqualTo(1);
		photo = photos.get(0);
		assertThat(photo.getPostId()).isEqualTo(firstPhotoPostID);
		assertThat(photo.getCaption()).isEqualTo("This is the caption for a photo post");
		assertThat(photo.getUrl1280()).isEqualTo("photo 1 1280");
		assertThat(photo.getUrl500()).isEqualTo("photo 1 500");
		assertThat(photo.getUrl400()).isEqualTo("photo 1 400");
		assertThat(photo.getUrl250()).isEqualTo("photo 1 250");
		assertThat(photo.getUrl100()).isEqualTo("photo 1 100");
		assertThat(photo.getUrl75()).isEqualTo("photo 1 75");

	}

	/**
	 * <p>
	 * Verifies that parsing XML input files with the "overwrite posts" flag turned
	 * OFF will properly read in new posts, while ignoring existing posts. Also
	 * tests that a post is properly reset if the state/date changes. Performs the
	 * following steps:
	 * </p>
	 * 
	 * <ol>
	 * <li>Get all posts, and verify that they're there (not strictly necessary,
	 * since this is covered by other unit tests)</li>
	 * <li>Marks each existing post read</li>
	 * <li>Updates the "overwrite post data" flag to false</li>
	 * <li>Loads in the additive XML file, which has all of the data from the
	 * original XML file plus an extra post</li>
	 * <li>Asserts that the correct number of posts is in the DB</li>
	 * <li>Asserts that there are two Regulars in the DB (the original XML file had
	 * one and the additive file had a second one)</li>
	 * <li>Asserts that the newly added Regular has all of its information properly
	 * inserted</li>
	 * <li>Goes through each of the posts that were originally inserted in the
	 * <code>setupData()</code> method and verifies that they're still there -- and
	 * that they're still marked as read. (They were supposed to be ignored; if they
	 * were improperly imported into the system, they'd have been marked as
	 * unread.)</li>
	 * </ol>
	 * 
	 * @throws FileNotFoundException
	 */
	@Test
	public void testAddingPosts() throws FileNotFoundException {
		List<Post> posts = postController.getAllPosts();
		assertThat(posts.size()).isEqualTo(ORIGINAL_NUM_POSTS);

		for (Post post : posts) {
			postController.markPostRead(post.getId());
		}

		Metadata md = mdController.getMetadata();
		md.setOverwritePostData(false);
		md = mdController.updateMetadata(md);

		File rawXmlFile = ResourceUtils.getFile("classpath:XML/test-post-extended-xml.xml");
		InputStream xmlFile = new FileInputStream(rawXmlFile);
		BlogXmlReader.parseDocument(xmlFile, postController, mdController);

		posts = postController.getAllPosts();
		assertThat(posts.size()).isEqualTo(EXTENDED_NUM_POSTS);

		assertThat(postController.getAllRegulars().size()).isEqualTo(EXTENDED_NUM_REG_POSTS);
		Regular regular = postController.getRegularById(addedRegularPostID);
		assertThat(regular).isNotNull();
		assertThat(regular.getPostId()).isEqualTo(addedRegularPostID);
		assertThat(regular.getBody()).isEqualTo("post added after initial load");
		assertThat(regular.getTitle()).isEqualTo("Added Post");

		Post post = postController.getPostById(addedRegularPostID);
		assertThat(post).isNotNull();
		assertThat(post.getIsRead()).isEqualTo(false);

		post = postController.getPostById(regularPostID);
		assertThat(post).isNotNull();
		assertThat(post.getIsRead()).isEqualTo(true);

		post = postController.getPostById(answerPostID);
		assertThat(post).isNotNull();
		assertThat(post.getIsRead()).isEqualTo(true);

		post = postController.getPostById(linkPostID);
		assertThat(post).isNotNull();
		assertThat(post.getIsRead()).isEqualTo(true);

		post = postController.getPostById(videoPostID);
		assertThat(post).isNotNull();
		assertThat(post.getIsRead()).isEqualTo(true);

		post = postController.getPostById(firstPhotoPostID);
		assertThat(post).isNotNull();
		assertThat(post.getIsRead()).isEqualTo(true);

		post = postController.getPostById(secondPhotoPostID);
		assertThat(post).isNotNull();
		assertThat(post.getIsRead()).isEqualTo(true);

		List<Hashtag> hashtags = postController.getAllHashtags();
		assertThat(hashtags).isNotNull();
		assertThat(hashtags.size()).isEqualTo(15);

		hashtagTestHelper(hashtags, REVISED_HASHTAGS);

	}

	/**
	 * Tests that the initial load of posts generated the right number and count of
	 * hashtags
	 */
	@Test
	public void testInitialHashtags() {
		List<Hashtag> hashtags = postController.getAllHashtags();
		assertThat(hashtags).isNotNull();
		assertThat(hashtags.size()).isEqualTo(INITIAL_HASHTAGS.size());

		hashtagTestHelper(hashtags, INITIAL_HASHTAGS);
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
	 * Tests that parsing of invalid XML throws the proper exception
	 * 
	 * @throws FileNotFoundException
	 */
	@Test(expected = XMLParsingException.class)
	public void testBadXmlUpload() throws FileNotFoundException {
		File rawXmlFile = ResourceUtils.getFile("classpath:XML/test-post-badxml.xml");
		InputStream xmlFile = new FileInputStream(rawXmlFile);

		BlogXmlReader.parseDocument(xmlFile, postController, mdController);
	}

	/**
	 * Simple check that all hashtags have been loaded
	 */
	@Test
	public void testHashtagInitialLoad() {
		List<Hashtag> tags = postController.getAllHashtags();

		assertThat(tags).isNotNull();
		assertThat(tags.size()).isEqualTo(15);
	}

	/**
	 * Test adding a new hashtag, after the initial load
	 */
	@Test
	public void testAddHashtag() {
		postController.createHashtag("tag16");

		List<Hashtag> tags = postController.getAllHashtags();

		assertThat(tags).isNotNull();
		assertThat(tags.size()).isEqualTo(16);
	}

	/**
	 * Test adding a hashtag that already exists in the system
	 */
	@Test
	public void testAddExistingHashtag() {
		postController.createHashtag("tag1");

		List<Hashtag> tags = postController.getAllHashtags();

		assertThat(tags).isNotNull();
		assertThat(tags.size()).isEqualTo(15);
	}

}
