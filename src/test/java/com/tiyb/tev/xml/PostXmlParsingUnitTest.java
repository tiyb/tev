package com.tiyb.tev.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import com.tiyb.tev.controller.TEVRestController;
import com.tiyb.tev.datamodel.Answer;
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.datamodel.Video;

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
	private TEVRestController restController;

	private long answerPostID = 180371366195L;
	private long linkPostID = 180265557725L;
	private long regularPostID = 180894436671L;
	private long videoPostID = 180782992914L;
	private long firstPhotoPostID = 180784644740L;
	private long secondPhotoPostID = 180254465582L;
	private long addedRegularPostID = 180894436672L;

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

		Metadata md = restController.getMetadata();
		md.setOverwritePostData(true);
		md = restController.updateMetadata(md);

		BlogXmlReader.parseDocument(xmlFile, restController);
	}

	/**
	 * Verify that test Answers in the input XML have been properly inserted into
	 * the DB (there is one)
	 */
	@Test
	public void testAnswer() {
		assertThat(restController.getAllAnswers().size()).isEqualTo(1);
		Answer answer = restController.getAnswerById(answerPostID);
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
		assertThat(restController.getAllLinks().size()).isEqualTo(1);
		Link link = restController.getLinkById(linkPostID);
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
		assertThat(restController.getAllRegulars().size()).isEqualTo(1);
		Regular regular = restController.getRegularById(regularPostID);
		assertThat(regular).isNotNull();
		assertThat(regular.getPostId()).isEqualTo(regularPostID);
		assertThat(regular.getBody()).isEqualTo("post body text here");
		assertThat(regular.getTitle()).isEqualTo("First Post");
	}

	/**
	 * Verify that test Videos in the input XML have been properly inserted into the
	 * DB (there is one)
	 */
	@Test
	public void testVideo() {
		assertThat(restController.getAllVideos().size()).isEqualTo(1);
		Video video = restController.getVideoById(videoPostID);
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
		List<Photo> photos = restController.getPhotoById(secondPhotoPostID);
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

		photos = restController.getPhotoById(firstPhotoPostID);
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
	 * OFF will properly read in new posts, while ignoring existing posts. Performs
	 * the following steps:
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
		List<Post> posts = restController.getAllPosts();
		assertThat(posts.size()).isEqualTo(6);

		for (Post post : posts) {
			restController.markPostRead(post.getId());
		}

		Metadata md = restController.getMetadata();
		md.setOverwritePostData(false);
		md = restController.updateMetadata(md);

		File rawXmlFile = ResourceUtils.getFile("classpath:XML/test-post-extended-xml.xml");
		InputStream xmlFile = new FileInputStream(rawXmlFile);
		BlogXmlReader.parseDocument(xmlFile, restController);

		posts = restController.getAllPosts();
		assertThat(posts.size()).isEqualTo(7);

		assertThat(restController.getAllRegulars().size()).isEqualTo(2);
		Regular regular = restController.getRegularById(addedRegularPostID);
		assertThat(regular).isNotNull();
		assertThat(regular.getPostId()).isEqualTo(addedRegularPostID);
		assertThat(regular.getBody()).isEqualTo("post added after initial load");
		assertThat(regular.getTitle()).isEqualTo("Added Post");

		Post post = restController.getPostById(addedRegularPostID);
		assertThat(post).isNotNull();
		assertThat(post.getIsRead()).isEqualTo(false);

		post = restController.getPostById(regularPostID);
		assertThat(post).isNotNull();
		assertThat(post.getIsRead()).isEqualTo(true);

		post = restController.getPostById(answerPostID);
		assertThat(post).isNotNull();
		assertThat(post.getIsRead()).isEqualTo(true);

		post = restController.getPostById(linkPostID);
		assertThat(post).isNotNull();
		assertThat(post.getIsRead()).isEqualTo(true);

		post = restController.getPostById(videoPostID);
		assertThat(post).isNotNull();
		assertThat(post.getIsRead()).isEqualTo(true);

		post = restController.getPostById(firstPhotoPostID);
		assertThat(post).isNotNull();
		assertThat(post.getIsRead()).isEqualTo(true);

		post = restController.getPostById(secondPhotoPostID);
		assertThat(post).isNotNull();
		assertThat(post.getIsRead()).isEqualTo(true);

	}

}
