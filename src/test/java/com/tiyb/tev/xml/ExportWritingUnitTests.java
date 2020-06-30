package com.tiyb.tev.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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

import com.tiyb.tev.TevTestingHelpers;
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

    private static String ANSWER_POST_ID = "180371366195";
    private static String REGULAR_POST_ID = "180894436671";
    private static String LINK_POST_ID = "180265557725";
    private static String SINGLEPHOTO_POST_ID = "180784644740";
    private static String MULTIPLEPHOTO_POST_ID = "180254465582";

    private static String MAIN_BLOG_NAME = "mainblog";

    @Before
    public void setupData() throws FileNotFoundException {
        File rawXmlFile = ResourceUtils.getFile(TevTestingHelpers.MAIN_INPUT_XML_FILE);
        InputStream xmlFile = new FileInputStream(rawXmlFile);

        Metadata md = mdController.getMetadataForBlogOrDefault(MAIN_BLOG_NAME);
        md.setOverwritePostData(true);
        md.setIsDefault(true);
        md.setBlog(MAIN_BLOG_NAME);
        md = mdController.updateMetadata(md.getId(), md);

        BlogXmlReader.parseDocument(xmlFile, postController, MAIN_BLOG_NAME);
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
        List<String> expectedResponseStrings =
                Files.readAllLines(expectedResponseFile.toPath(), StandardCharsets.UTF_8);
        String expectedResponseString = String.join(StringUtils.EMPTY, expectedResponseStrings);

        return expectedResponseString;
    }

    private void testSinglePostResponse(Resource resource, String postID) throws IOException {
        String expectedAnswer = getExpectedResponse(resource);

        List<String> postIDs = new ArrayList<String>();
        postIDs.add(postID);

        String result = BlogXmlWriter.getStagedPostXMLForBlog(postIDs, postController, MAIN_BLOG_NAME);

        assertThat(result).isEqualToIgnoringWhitespace(expectedAnswer);
    }

}
