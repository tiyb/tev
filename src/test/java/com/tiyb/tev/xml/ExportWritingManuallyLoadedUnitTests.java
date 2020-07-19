package com.tiyb.tev.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import com.tiyb.tev.TevTestingClass;
import com.tiyb.tev.controller.TEVPostRestController;
import com.tiyb.tev.datamodel.Answer;
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.datamodel.Video;

public class ExportWritingManuallyLoadedUnitTests extends TevTestingClass {

    @Autowired
    TEVPostRestController postController;

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
    public void setupData() {
        postController.getRegController().deleteAllRegularsForBlog(MAIN_BLOG_NAME);
        postController.getAnswerController().deleteAllAnswersForBlog(MAIN_BLOG_NAME);
        postController.getLinkController().deleteAllLinksForBlog(MAIN_BLOG_NAME);
        postController.getPhotoController().deleteAllPhotosForBlog(MAIN_BLOG_NAME);
        postController.getVideoController().deleteAllVideosForBlog(MAIN_BLOG_NAME);
        postController.getHashtagController().deleteAllHashtagsForBlog(MAIN_BLOG_NAME);
        postController.deleteAllPostsForBlog(MAIN_BLOG_NAME);

        for (Post post : postsForUploading) {
            postController.createPostForBlog(MAIN_BLOG_NAME, post);
        }

        for (Regular reg : regularsForUploading) {
            postController.getRegController().createRegularForBlog(MAIN_BLOG_NAME, reg.getPostId(), reg);
        }

        for (Answer answer : answersForUploading) {
            postController.getAnswerController().createAnswerForBlog(MAIN_BLOG_NAME, answer.getPostId(), answer);
        }

        for (Link link : linksForUploading) {
            postController.getLinkController().createLinkForBlog(MAIN_BLOG_NAME, link.getPostId(), link);
        }

        for (Photo photo : photosForUploading) {
            postController.getPhotoController().createPhotoForBlog(MAIN_BLOG_NAME, photo);
        }

        for (Video vid : videosForUploading) {
            postController.getVideoController().createVideoForBlog(MAIN_BLOG_NAME, vid.getPostId(), vid);
        }
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
        List<String> expectedResponseStrings = Files.readAllLines(expectedResponseFile.toPath(),
                StandardCharsets.UTF_8);
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
