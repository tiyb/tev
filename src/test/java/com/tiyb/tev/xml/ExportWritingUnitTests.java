package com.tiyb.tev.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import com.tiyb.tev.TevTestingClass;
import com.tiyb.tev.controller.TEVMetadataRestController;
import com.tiyb.tev.controller.TEVPostRestController;

public class ExportWritingUnitTests extends TevTestingClass {

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

    @Before
    public void setupData() throws FileNotFoundException {
        initDataForMainBlog(mdController, postController, Optional.empty());
    }

    @Test
    public void exportRegular() throws IOException {
        checkSinglePostResponse(regularXML, REGULAR_POST_ID);
    }

    @Test
    public void exportAnswer() throws IOException {
        checkSinglePostResponse(answerXML, ANSWER_POST_ID);
    }

    @Test
    public void exportLink() throws IOException {
        checkSinglePostResponse(linkXML, LINK_POST_ID);
    }

    @Test
    public void exportPhoto() throws IOException {
        checkSinglePostResponse(singlePhotoXML, SINGLEPHOTO_POST_ID);
    }

    @Test
    public void exportMultiplePhotos() throws IOException {
        checkSinglePostResponse(this.multiplePhotoXML, MULTIPLEPHOTO_POST_ID);
    }

    private String getExpectedResponse(Resource resource) throws IOException {
        File expectedResponseFile = resource.getFile();
        List<String> expectedResponseStrings = Files.readAllLines(expectedResponseFile.toPath(),
                StandardCharsets.UTF_8);
        String expectedResponseString = String.join(StringUtils.EMPTY, expectedResponseStrings);

        return expectedResponseString;
    }

    private void checkSinglePostResponse(Resource resource, String postID) throws IOException {
        String expectedAnswer = getExpectedResponse(resource);

        List<String> postIDs = new ArrayList<String>();
        postIDs.add(postID);

        String result = BlogXmlWriter.getStagedPostXMLForBlog(postIDs, postController, MAIN_BLOG_NAME);

        assertThat(result).isEqualToIgnoringWhitespace(expectedAnswer);
    }

}
