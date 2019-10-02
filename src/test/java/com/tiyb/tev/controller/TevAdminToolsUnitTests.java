package com.tiyb.tev.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
import com.tiyb.tev.xml.BlogXmlReader;

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
	
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Before
	public void setupData() throws FileNotFoundException {
		File rawXmlFile = ResourceUtils.getFile("classpath:XML/test-post-xml.xml");
		InputStream xmlFile = new FileInputStream(rawXmlFile);

		Metadata md = mdRestController.getMetadata();
		md.setOverwritePostData(true);
		md.setBaseMediaPath(tempFolder.getRoot().getAbsolutePath());
		md = mdRestController.updateMetadata(md);

		BlogXmlReader.parseDocument(xmlFile, postController, mdRestController);
	}
	
	@Test
	public void testCleaningExtraImages() throws IOException {
		tempFolder.newFile("blah.txt");
		
		adminRestController.cleanImagesOnHD();
		
		assertThat(tempFolder.getRoot().list().length).isEqualTo(0);
	}
	
	@Test
	public void testCleaningExactNumImages() throws IOException {
		tempFolder.newFile("180784644740_0.gif");
		tempFolder.newFile("180254465582_0.gif");
		tempFolder.newFile("180254465582_1.gif");
		
		adminRestController.cleanImagesOnHD();
		
		assertThat(tempFolder.getRoot().list().length).isEqualTo(3);
	}
	
	@Test
	public void testCleaningDoubleImages() throws IOException {
		tempFolder.newFile("180784644740_0.gif");
		tempFolder.newFile("180784644740_1.gif");
		tempFolder.newFile("180254465582_0.gif");
		tempFolder.newFile("180254465582_1.gif");
		tempFolder.newFile("180254465582_2.gif");
		tempFolder.newFile("180254465582_3.gif");
		
		adminRestController.cleanImagesOnHD();
		
		assertThat(tempFolder.getRoot().list().length).isEqualTo(3);
	}
}
