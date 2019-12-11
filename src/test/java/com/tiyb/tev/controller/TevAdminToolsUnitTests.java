package com.tiyb.tev.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.xml.BlogXmlReader;

/**
 * Unit test cases for testing admin functions of the application.
 * 
 * @author tiyb
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TevAdminToolsUnitTests {

	@Autowired
	private TEVMetadataRestController mdRestController;
	@Autowired
	private TEVAdminToolsController adminRestController;
	@Autowired
	private TEVPostRestController postController;

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
	 * Sets up the posts to a valid state, using the <code>test-post-xml.xml</code>
	 * input file
	 * 
	 * @throws FileNotFoundException
	 */
	@Before
	public void setupData() throws FileNotFoundException {
		File rawXmlFile = ResourceUtils.getFile("classpath:XML/test-post-xml.xml");
		InputStream xmlFile = new FileInputStream(rawXmlFile);

		Metadata md = mdRestController.getMetadata();
		md.setOverwritePostData(true);
		md.setBaseMediaPath(tempMDImageFolder.getRoot().getAbsolutePath());
		md = mdRestController.updateMetadata(md);

		BlogXmlReader.parseDocument(xmlFile, postController, mdRestController);
	}

	/**
	 * Tests that any files not associated with a post are deleted from the folder
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCleaningExtraImages() throws IOException {
		tempMDImageFolder.newFile("blah.txt");

		adminRestController.cleanImagesOnHD();

		assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(0);
	}

	/**
	 * Tests that nothing is deleted when the exact number of files is in the folder
	 * as expected
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCleaningExactNumImages() throws IOException {
		tempMDImageFolder.newFile("180784644740_0.gif");
		tempMDImageFolder.newFile("180254465582_0.gif");
		tempMDImageFolder.newFile("180254465582_1.gif");

		adminRestController.cleanImagesOnHD();

		assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(3);
	}

	/**
	 * Tests that duplicate images are removed from the folder (the core result of
	 * the feature)
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCleaningDoubleImages() throws IOException {
		tempMDImageFolder.newFile("180784644740_0.gif");
		tempMDImageFolder.newFile("180784644740_1.gif");
		tempMDImageFolder.newFile("180254465582_0.gif");
		tempMDImageFolder.newFile("180254465582_1.gif");
		tempMDImageFolder.newFile("180254465582_2.gif");
		tempMDImageFolder.newFile("180254465582_3.gif");

		adminRestController.cleanImagesOnHD();

		assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(3);
	}

	/**
	 * Test importing images with non-post-related images
	 * 
	 * @throws IOException
	 */
	@Test
	public void testImportingExtraImages() throws IOException {
		tempInputImageFolder.newFile("blah.txt");

		adminRestController.importImages(tempInputImageFolder.getRoot().getAbsolutePath());

		assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(0);
	}

	/**
	 * Test import where the right number of images exists
	 * 
	 * @throws IOException
	 */
	@Test
	public void testImportingExactNumImages() throws IOException {
		tempInputImageFolder.newFile("180784644740_0.gif");
		tempInputImageFolder.newFile("180254465582_0.gif");
		tempInputImageFolder.newFile("180254465582_1.gif");

		adminRestController.importImages(tempInputImageFolder.getRoot().getAbsolutePath());

		assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(3);
	}

	/**
	 * Test import where the input folder has duplicates
	 * 
	 * @throws IOException
	 */
	@Test
	public void testImportingDoubleImages() throws IOException {
		tempInputImageFolder.newFile("180784644740_0.gif");
		tempInputImageFolder.newFile("180784644740_1.gif");
		tempInputImageFolder.newFile("180254465582_0.gif");
		tempInputImageFolder.newFile("180254465582_1.gif");
		tempInputImageFolder.newFile("180254465582_2.gif");
		tempInputImageFolder.newFile("180254465582_3.gif");

		adminRestController.importImages(tempInputImageFolder.getRoot().getAbsolutePath());

		assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(3);
	}

	/**
	 * Test import when there are already images in the destination folder
	 * 
	 * @throws IOException
	 */
	@Test
	public void testImportingWithExisting() throws IOException {
		tempMDImageFolder.newFile("180784644740_0.gif");
		tempMDImageFolder.newFile("180784644740_1.gif");
		tempInputImageFolder.newFile("180254465582_0.gif");
		tempInputImageFolder.newFile("180254465582_1.gif");
		tempInputImageFolder.newFile("180254465582_2.gif");
		tempInputImageFolder.newFile("180254465582_3.gif");

		adminRestController.importImages(tempInputImageFolder.getRoot().getAbsolutePath());

		assertThat(tempMDImageFolder.getRoot().list().length).isEqualTo(3);
	}
	
	/**
	 * Tests functionality for marking all posts read
	 */
	@Test
	public void testMarkingAllPostsRead() {
		List<Post> allPosts = postController.getAllPosts();
		assertThat(allPosts).isNotNull();
		
		for(Post p : allPosts) {
			p.setIsRead(false);
			postController.updatePost(p.getId(), p);
		}
		
		allPosts = postController.getAllPosts();
		assertThat(allPosts).isNotNull();
		for(Post p : allPosts) {
			assertThat(p.getIsRead()).isEqualTo(false);
		}
		
		adminRestController.markAllPostsRead();
		
		allPosts = postController.getAllPosts();
		assertThat(allPosts).isNotNull();
		for(Post p : allPosts) {
			assertThat(p.getIsRead()).isEqualTo(true);
		}
	}
	
	/**
	 * Tests functionality for marking all posts unread
	 */
	@Test
	public void testMarkingAllPostsUnread() {
		List<Post> allPosts = postController.getAllPosts();
		assertThat(allPosts).isNotNull();
		
		for(Post p : allPosts) {
			p.setIsRead(true);
			postController.updatePost(p.getId(), p);
		}
		
		allPosts = postController.getAllPosts();
		assertThat(allPosts).isNotNull();
		for(Post p : allPosts) {
			assertThat(p.getIsRead()).isEqualTo(true);
		}
		
		adminRestController.markAllPostsUnread();
		
		allPosts = postController.getAllPosts();
		assertThat(allPosts).isNotNull();
		for(Post p : allPosts) {
			assertThat(p.getIsRead()).isEqualTo(false);
		}
	}
	
}
