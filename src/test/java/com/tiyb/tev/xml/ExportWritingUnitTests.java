package com.tiyb.tev.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import com.tiyb.tev.controller.TEVMetadataRestController;
import com.tiyb.tev.controller.TEVPostRestController;
import com.tiyb.tev.datamodel.Metadata;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ExportWritingUnitTests {

	@Autowired
	private TEVPostRestController postController;
	
	@Autowired
	private TEVMetadataRestController mdController;
	
	@Value("classpath:XML/outputs/response-xml-answer.xml")
	Resource answerXML;
	
	@Value("classpath:XML/outputs/response-xml-regular.xml")
	Resource regularXML;
	
	@Value("classpath:XML/outputs/response-xml-link.xml")
	Resource linkXML;
	
	@Value("classpath:XML/outputs/response-xml-singlephoto.xml")
	Resource singlePhotoXML;
	
	@Value("classpath:XML/outputs/response-xml-multiplephotos.xml")
	Resource multiplePhotoXML;
	
	private static long ANSWER_POST_ID = 180371366195L;
	private static long REGULAR_POST_ID = 180894436671L;
	private static long LINK_POST_ID = 180265557725L;
	private static long SINGLEPHOTO_POST_ID = 180784644740L;
	private static long MULTIPLEPHOTO_POST_ID = 180254465582L;
	
	private static String MAIN_BLOG_NAME = "mainblog";
	
	@Before
	public void setupData() throws FileNotFoundException {
		File rawXmlFile = ResourceUtils.getFile("classpath:XML/test-post-xml.xml");
		InputStream xmlFile = new FileInputStream(rawXmlFile);
		
		Metadata md = Metadata.newDefaultMetadata();
		md.setOverwritePostData(true);
		md.setIsDefault(true);
		md.setBlog(MAIN_BLOG_NAME);
		md = mdController.updateMetadata(md.getId(), md);
		
		BlogXmlReader.parseDocument(xmlFile, postController, mdController, MAIN_BLOG_NAME);
	}
	
	@Test
	public void testExportOfRegular() throws IOException {
		testSinglePostResponse(regularXML, REGULAR_POST_ID);
	}
	
	@Test
	public void testExportOfAnswer() throws IOException {
		testSinglePostResponse(answerXML, ANSWER_POST_ID);
	}
	
	@Test
	public void testExportOfLink() throws IOException {
		testSinglePostResponse(linkXML, LINK_POST_ID);
	}
	
	@Test
	public void testExportOfPhoto() throws IOException {
		testSinglePostResponse(singlePhotoXML, SINGLEPHOTO_POST_ID);
	}
	
	@Test
	public void testExportOfPhotoMultiple() throws IOException {
		testSinglePostResponse(this.multiplePhotoXML, MULTIPLEPHOTO_POST_ID);
	}
	
	private String getExpectedResponse(Resource resource) throws IOException {
		File expectedResponseFile = resource.getFile();
		String expectedResponseString = new String(Files.readAllBytes(expectedResponseFile.toPath()));
		
		return expectedResponseString;
	}
	
	private void testSinglePostResponse(Resource resource, long postID) throws IOException {
		String expectedAnswer = getExpectedResponse(resource);
		
		List<Long> postIDs = new ArrayList<Long>();
		postIDs.add(postID);
		
		String result = BlogXmlWriter.getStagedPostXMLForBlog(postIDs, postController, MAIN_BLOG_NAME);
		
		assertThat(result).isEqualToIgnoringWhitespace(expectedAnswer);
	}
	
}
