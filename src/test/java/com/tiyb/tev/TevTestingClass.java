package com.tiyb.tev;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import com.tiyb.tev.controller.TEVConvoRestController;
import com.tiyb.tev.controller.TEVMetadataRestController;
import com.tiyb.tev.controller.TEVPostRestController;
import com.tiyb.tev.datamodel.Answer;
import com.tiyb.tev.datamodel.Conversation;
import com.tiyb.tev.datamodel.ConversationMessage;
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.datamodel.Video;
import com.tiyb.tev.xml.BlogXmlReader;
import com.tiyb.tev.xml.ConversationXmlReader;

/**
 * Helper methods/constants used in unit tests. Not a best practice -- maybe
 * even an anti-pattern -- but good enough for unit tests
 * 
 * @author tiyb
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public abstract class TevTestingClass {

    private final static String MAIN_INPUT_XML_FILE = "classpath:XML/test-post-xml.xml";
    private final static String SECONDARY_INPUT_XML_FILE = "classpath:XML/test-post-secondblog.xml";
    private final static String MAIN_CONVO_XML_FILE = "classpath:XML/test-messages-xml.xml";

    protected final static String MAIN_BLOG_NAME = "mainblog";
    protected final static String SECOND_BLOG_NAME = "secondblog";

    protected final static List<Post> postsForUploading = List.of(new Post("180894436671", // id
            "https://mainblog.tumblr.com/post/180894436671", // url
            "https://mainblog.tumblr.com/post/180894436671/first-post", // url with slug
            "2018-12-07 16:48:43 GMT", // date GMT
            "Fri, 07 Dec 2018 11:48:43", // date
            1544201323L, // unixtimestamp
            "O6pLVlp1", // reblog key
            "first-post", // slug
            true, // is reblog
            "mainblog", // tumblelog
            "regular", // type
            false, // is read
            "tag1, tag2", // tags
            false, // is favourite
            "published", // state
            null, // height
            null // width
    ), new Post("190097591599", // id
            "https://mainblog.tumblr.com/post/190097591599", // url
            "https://mainblog.tumblr.com/post/190097591599/draft-post", // url with slug
            "2018-12-07 16:48:43 GMT", // date GMT
            "Fri, 07 Dec 2018 11:48:43", // date
            1544201323L, // unixtimestamp
            "O6pLVlp1", // reblog key
            "draft-post", // slug
            true, // is reblog
            "mainblog", // tumblelog
            "regular", // type
            false, // is read
            "tag1, tag2", // tags
            false, // is favourite
            "draft", // state
            null, // height
            null // width
    ), new Post("778563537472", // id
            "https://mainblog.tumblr.com/post/778563537472", // url
            "https://mainblog.tumblr.com/post/778563537472/queued-post", // url with slug
            "2018-12-07 16:48:43 GMT", // date GMT
            "Fri, 07 Dec 2018 11:48:43", // date
            1544201323L, // unixtimestamp
            "O6pLVlp1", // reblog key
            "queued-post", // slug
            true, // is reblog
            "mainblog", // tumblelog
            "regular", // type
            false, // is read
            "tag1, tag2", // tags
            false, // is favourite
            "queued", // state
            null, // height
            null // width
    ), new Post("180784644740", // id
            "https://mainblog.tumblr.com/post/180784644740", // url
            "https://mainblog.tumblr.com/post/180784644740/new-slug", // url with slug
            "2018-12-04 07:17:52 GMT", // date GMT
            "Tue, 04 Dec 2018 02:17:52", // date
            1543907872L, // unixtimestamp
            "Pius5FOw", // reblog key
            "new-slug", // slug
            true, // is reblog
            "mainblog", // tumblelog
            "photo", // type
            false, // is read
            "tag3, tag4", // tags
            false, // is favourite
            "published", // state
            750, // height
            500 // width
    ), new Post("180782992914", // id
            "https://mainblog.tumblr.com/post/180782992914", // url
            "https://mainblog.tumblr.com/post/180782992914/another-slug", // url with slug
            "2018-12-04 06:09:21 GMT", // date GMT
            "Tue, 04 Dec 2018 01:09:21", // date
            1543903761L, // unixtimestamp
            "IPc1CZyV", // reblog key
            "another-slug", // slug
            true, // is reblog
            "mainblog", // tumblelog
            "video", // type
            false, // is read
            "tag5, tag6, tag7, tag8, tag9, tag10", // tags
            false, // is favourite
            "published", // state
            null, // height
            null // width
    ), new Post("180371366195", // id
            "https://mainblog.tumblr.com/post/180371366195", // url
            "https://mainblog.tumblr.com/post/180371366195/slug-slug-slug", // url with slug
            "2018-11-22 08:26:25 GMT", // date GMT
            "Thu, 22 Nov 2018 03:26:25", // date
            1542875185L, // unixtimestamp
            "OQqor1Zh", // reblog key
            "slug-slug-slug", // slug
            true, // is reblog
            "mainblog", // tumblelog
            "answer", // type
            false, // is read
            "tag2", // tags
            false, // is favourite
            "published", // state
            null, // height
            null // width
    ), new Post("180265557725", // id
            "https://mainblog.tumblr.com/post/180265557725", // url
            "https://mainblog.tumblr.com/post/180265557725/tumblr", // url with slug
            "2018-11-19 06:09:08 GMT", // date GMT
            "Mon, 19 Nov 2018 01:09:08", // date
            1542607748L, // unixtimestamp
            "6pFgAxH2", // reblog key
            "tumblr", // slug
            false, // is reblog
            "mainblog", // tumblelog
            "link", // type
            false, // is read
            "tag1", // tags
            false, // is favourite
            "published", // state
            null, // height
            null // width
    ), new Post("180254465582", // id
            "https://mainblog.tumblr.com/post/180254465582", // url
            "https://mainblog.tumblr.com/post/180254465582/slugs-are-delicious", // url with slug
            "2018-11-18 23:17:36 GMT", // date GMT
            "Sun, 18 Nov 2018 18:17:36", // date
            1542583056L, // unixtimestamp
            "jTxuwC0o", // reblog key
            "slugs-are-delicious", // slug
            true, // is reblog
            "mainblog", // tumblelog
            "photo", // type
            false, // is read
            "tag11, tag12, tag13, tag14, tag15", // tags
            false, // is favourite
            "published", // state
            583, // height
            692 // width
    ), new Post("190894436671", // id
            "https://mainblog.tumblr.com/post/190894436671", // url
            "https://mainblog.tumblr.com/post/190894436671/no-tags", // url with slug
            "2018-12-07 16:48:43 GMT", // date GMT
            "Fri, 07 Dec 2018 11:48:43", // date
            1544201323L, // unixtimestamp
            "O6pLVlp1", // reblog key
            "no-tags", // slug
            true, // is reblog
            "mainblog", // tumblelog
            "regular", // type
            false, // is read
            "", // tags
            false, // is favourite
            "published", // state
            null, // height
            null // width
    ));

    protected final static List<Post> postsForUploadingForSecondBlog = List.of(new Post("180894436690", // id
            "https://mainblog.tumblr.com/post/180894436690", // url
            "https://mainblog.tumblr.com/post/180894436690/first-post", // url with slug
            "2018-12-07 16:48:43 GMT", // date GMT
            "Fri, 07 Dec 2018 11:48:43", // date
            1544201323L, // unixtimestamp
            "O6pLVlp1", // reblog key
            "first-post", // slug
            true, // is reblog
            SECOND_BLOG_NAME, // tumblelog
            "regular", // type
            false, // is read
            "2ndtag1, tag2", // tags
            false, // is favourite
            "published", // state
            null, // height
            null // width
    ));

    protected final static List<Regular> regularsForUploading = List.of(new Regular("180894436671", // post ID
            "First Post", // title
            "“This is some quoted text,” she said, “so will it be interpreted correctly?” It was a great question &ndash; and this JUnit test would settle it once and for all." // body
    ), new Regular("190097591599", // post ID
            "Draft Post", // title
            "post body text here" // body
    ), new Regular("778563537472", // post ID
            "Queued Post", // title
            "post body text here" // body
    ), new Regular("190894436671", // post ID
            "Post with no tags", // title
            "post body text here" // body
    ));

    protected final static List<Regular> regularsForUploadingSecondBlog = List.of(new Regular("180894436690", // post ID
            "First Post", // title
            "This is a post on the second blog" // body
    ));

    protected final static List<Photo> photosForUploading = List.of(new Photo("180784644740", // post ID
            "This is the caption for a photo post", // caption
            "http://bit.ly/some-photo", // photo link url
            null, // offset
            500, // width
            750, // height
            "photo 1 1280.jpg", // url 1280
            "photo 1 500.jpg", // url 500
            "photo 1 400.jpg", // url 400
            "photo 1 250.jpg", // url 250
            "photo 1 100.jpg", // url 100
            "photo 1 75.jpg" // url 75
    ), new Photo("180254465582", // post ID
            "This is hte photo caption", // caption
            null, // photo link url
            "o1", // offset
            692, // width
            583, // height
            "photo 2 1280.jpg", // url 1280
            "photo 2 500.jpg", // url 500
            "photo 2 400.jpg", // url 400
            "photo 2 250.jpg", // url 250
            "photo 2 100.jpg", // url 100
            "photo 2 75.jpg" // url 75
    ), new Photo("180254465582", // post ID
            "This is hte photo caption", // caption
            null, // photo link url
            "o2", // offset
            990, // width
            532, // height
            "photo 4 1280.jpg", // url 1280
            "photo 4 500.jpg", // url 500
            "photo 4 400.jpg", // url 400
            "photo 4 250.jpg", // url 250
            "photo 4 100.jpg", // url 100
            "photo 4 75.jpg" // url 75
    ));

    protected final static List<Video> videosForUploading = List.of(new Video("180782992914", // post ID
            "video/mp4", // content type
            "mp4", // extension
            854, // width
            480, // height
            45, // duration
            "0", // revision
            "This is the caption for a video", // video caption
            "\n" + "&lt;video  id='embed-5c0df1d16b7d4180127212' class='crt-video crt-skin-default' width='400' height='225' poster='https://somesite.com/smart1.jpg' preload='none' muted data-crt-video data-crt-options='{\"autoheight\":null,\"duration\":45,\"hdUrl\":false,\"filmstrip\":{\"url\":\"https://somesite.com/previews/filmstrip.jpg\",\"width\":\"200\",\"height\":\"112\"}}' &gt;\n"
                    + "    &lt;source src=\"https://mainblog.tumblr.com/video_file/t:somefile\" type=\"video/mp4\"&gt;\n"
                    + "&lt;/video&gt;\n" + "            ", // video player
            "\n" + "&lt;video  id='embed-5c0df1d16b7d4180127212' class='crt-video crt-skin-default' width='400' height='225' poster='https://somesite.com/smart1.jpg' preload='none' muted data-crt-video data-crt-options='{\"autoheight\":null,\"duration\":45,\"hdUrl\":false,\"filmstrip\":{\"url\":\"https://somesite.com/previews/filmstrip.jpg\",\"width\":\"200\",\"height\":\"112\"}}' &gt;\n"
                    + "    &lt;source src=\"https://mainblog.tumblr.com/video_file/t:somefile\" type=\"video/mp4\"&gt;\n"
                    + "&lt;/video&gt;\n" + "            ", // video player 500
            "\n" + "&lt;video  id='embed-5c0df1d16b7d4180127212' class='crt-video crt-skin-default' width='400' height='225' poster='https://somesite.com/smart1.jpg' preload='none' muted data-crt-video data-crt-options='{\"autoheight\":null,\"duration\":45,\"hdUrl\":false,\"filmstrip\":{\"url\":\"https://somesite.com/previews/filmstrip.jpg\",\"width\":\"200\",\"height\":\"112\"}}' &gt;\n"
                    + "    &lt;source src=\"https://mainblog.tumblr.com/video_file/t:somefile\" type=\"video/mp4\"&gt;\n"
                    + "&lt;/video&gt;\n" + "            " // video player 250
    ));

    protected final static List<Answer> answersForUploading = List.of(new Answer("180371366195", // post ID
            "Question text", // question
            "Answer text" // answer
    ));

    protected final static List<Link> linksForUploading = List.of(new Link("180265557725", // post ID
            "Tumblr", // text
            "https://someblog.tumblr.com/", // url
            "This is the link description" // description
    ));

    protected final static List<FullConversation> conversationsToUpload = List
            .of(new FullConversation(new Conversation("participant1", // participant
                    "http://participant1/avatar", // participant avatar
                    "t:eMUq2ec6xRwaki33S-DLig", // participant ID
                    MAIN_BLOG_NAME, // blog
                    9 // num messages
            ), List.of(new ConversationMessage(1544197586L, // TS
                    false, // received
                    "TEXT", // type
                    "Message 1" // message
            ), new ConversationMessage(1544197605L, // TS
                    false, // received
                    "TEXT", // type
                    "Message 2" // message
            ), new ConversationMessage(1544197624L, // TS
                    false, // received
                    "TEXT", // type
                    "Message 3" // message
            ), new ConversationMessage(1544197647L, // TS
                    false, // received
                    "IMAGE", // type
                    "http://www.photourl.com/photo.png" // message
            ), new ConversationMessage(1544198315L, // TS
                    false, // received
                    "TEXT", // type
                    "Message 5" // message
            ), new ConversationMessage(1544221130L, // TS
                    true, // received
                    "TEXT", // type
                    "Message 6" // message
            ), new ConversationMessage(1544221197L, // TS
                    false, // received
                    "TEXT", // type
                    "Message 7" // message
            ), new ConversationMessage(1544221203L, // TS
                    true, // received
                    "TEXT", // type
                    "Message 8" // message
            ), new ConversationMessage(1544221221L, // TS
                    false, // received
                    "POSTREF", // type
                    "http://www.tumblr.com/somepost" // message
            ))), new FullConversation(new Conversation("participant2", // participant
                    "http://participant2/avatar", // participant avatar
                    "t:daPIjnWif90ATTPO_iqMVA", // participant ID
                    MAIN_BLOG_NAME, // blog
                    9 // num messages
            ), List.of(new ConversationMessage(1544012468L, // TS
                    true, // received
                    "TEXT", // type
                    "Message 1" // message
            ), new ConversationMessage(1544016206L, // TS
                    false, // received
                    "TEXT", // type
                    "Message 2" // message
            ), new ConversationMessage(1544016402L, // TS
                    true, // received
                    "TEXT", // type
                    "Message 3" // message
            ), new ConversationMessage(1544016410L, // TS
                    true, // received
                    "TEXT", // type
                    "Message 4" // message
            ), new ConversationMessage(1544016579L, // TS
                    true, // received
                    "TEXT", // type
                    "Message 5" // message
            ), new ConversationMessage(1544016582L, // TS
                    true, // received
                    "TEXT", // type
                    "Message 6" // message
            ), new ConversationMessage(1544022051L, // TS
                    false, // received
                    "TEXT", // type
                    "Message 7" // message
            ), new ConversationMessage(1544115936L, // TS
                    true, // received
                    "TEXT", // type
                    "Message 8" // message
            ), new ConversationMessage(1544126671L, // TS
                    false, // received
                    "TEXT", // type
                    "Message 9" // message
            ))), new FullConversation(new Conversation("participant3", // participant
                    "http://participant3/avatar", // participant avatar
                    "", // participant ID
                    MAIN_BLOG_NAME, // blog
                    1 // num messages
            ), List.of(new ConversationMessage(1544012468L, // TS
                    false, // received
                    "TEXT", // type
                    "Message 1" // message
            ))), new FullConversation(new Conversation("participant4", // participant
                    "http://participant4/avatar", // participant avatar
                    "OY3QnUHjX3lZs6orBDaI", // participant ID
                    MAIN_BLOG_NAME, // blog
                    1 // num messages
            ), List.of(new ConversationMessage(1544012468L, // TS
                    true, // received
                    "TEXT", // type
                    "Message 1" // message
            ))), new FullConversation(new Conversation("participant-oldname", // participant
                    "http://participanton/avatar", // participant avatar
                    "foaiehoihafoei", // participant ID
                    MAIN_BLOG_NAME, // blog
                    1 // num messages
            ), List.of(new ConversationMessage(1544012468L, // TS
                    true, // received
                    "TEXT", // type
                    "Message 1" // message
            ))), new FullConversation(new Conversation("goingtobedeactivated", // participant
                    "http://goingtobedeac/avatar", // participant avatar
                    "afoiehaifeh", // participant ID
                    MAIN_BLOG_NAME, // blog
                    2 // num messages
            ), List.of(new ConversationMessage(1544012470L, // TS
                    true, // received
                    "TEXT", // type
                    "Message 1" // message
            ), new ConversationMessage(1544012485L, // TS
                    false, // received
                    "TEXT", // type
                    "Message 2" // message
            ))), new FullConversation(new Conversation("NO NAME", // participant
                    "", // participant avatar
                    "foae8yofeiu", // participant ID
                    MAIN_BLOG_NAME, // blog
                    1 // num messages
            ), List.of(new ConversationMessage(1544012485L, // TS
                    true, // received
                    "TEXT", // type
                    "Message 1" // message
            ))), new FullConversation(new Conversation("participant1 1", // participant
                    "http://duplicatename/avatar", // participant avatar
                    "lihkg", // participant ID
                    MAIN_BLOG_NAME, // blog
                    1 // num messages
            ), List.of(new ConversationMessage(1544012485L, // TS
                    true, // received
                    "TEXT", // type
                    "Message 1" // message
            ))));

    /**
     * Initializes the blog with data from the sample XML file
     * 
     * @param mdController   REST controller for working with metadata
     * @param postController REST controller for working with post data
     * @param baseMediaPath  Optional path to be used for media
     * @throws FileNotFoundException If the input XML file can't be loaded for some
     *                               reason
     */
    protected static void initDataForMainBlog(TEVMetadataRestController mdController,
            TEVPostRestController postController, Optional<String> baseMediaPath) throws FileNotFoundException {
        initMainBlogMetadataata(mdController, baseMediaPath);

        readPostXml(MAIN_INPUT_XML_FILE, postController, MAIN_BLOG_NAME);
    }

    /**
     * Initializes the secondary blog with data from the sample XL file
     * 
     * @param mdController   REST controller for working with metadata
     * @param postController REST controller for working with post data
     * @param baseMediaPath  Optional path to be used for media
     * @throws FileNotFoundException If the input XML file can't be loaded for some
     *                               reason
     */
    protected static void initDataForSecondaryBlog(TEVMetadataRestController mdController,
            TEVPostRestController postController, Optional<String> baseMediaPath) throws FileNotFoundException {
        initAdditionalBlogMetadata(mdController, SECOND_BLOG_NAME);

        readPostXml(SECONDARY_INPUT_XML_FILE, postController, SECOND_BLOG_NAME);
    }

    /**
     * Helper method used for both main and secondary blogs for reading in an XML
     * file
     * 
     * @param xmlFileToLoad  File to be read
     * @param postController REST controller for working with posts
     * @param blogName       Name of the blog for which the import is happening
     * @throws FileNotFoundException If the input XML file can't be read for some
     *                               reason
     */
    private static void readPostXml(String xmlFileToLoad, TEVPostRestController postController, String blogName)
            throws FileNotFoundException {
        File rawXmlFile = ResourceUtils.getFile(xmlFileToLoad);
        InputStream xmlFile = new FileInputStream(rawXmlFile);
        BlogXmlReader.parseDocument(xmlFile, postController, blogName);
    }

    /**
     * Reads in the Conversation sample XML file for the main blog
     * 
     * @param mdController    REST controller for metadata
     * @param convoController REST controller for conversation data
     * @throws IOException If the XML file can't be read
     */
    protected static void initConvoForMainBlog(TEVMetadataRestController mdController,
            TEVConvoRestController convoController) throws IOException {
        initMainBlogMetadataata(mdController, Optional.empty());

        File rawXmlFile = ResourceUtils.getFile(MAIN_CONVO_XML_FILE);
        InputStream xmlFile = new FileInputStream(rawXmlFile);
        MockMultipartFile mockFile = new MockMultipartFile("messages", xmlFile);
        ConversationXmlReader.parseDocument(mockFile, mdController, convoController, MAIN_BLOG_NAME);
    }

    /**
     * Initializes the metadata for the main blog
     * 
     * @param mdController  REST controller for working with metdata
     * @param baseMediaPath Optional path to be used for media
     */
    protected static void initMainBlogMetadataata(TEVMetadataRestController mdController,
            Optional<String> baseMediaPath) {
        Metadata md = mdController.getMetadataForBlogOrDefault(MAIN_BLOG_NAME);
        md.setBlog(MAIN_BLOG_NAME);
        md.setMainTumblrUser(MAIN_BLOG_NAME);
        md.setIsDefault(true);
        md.setOverwritePostData(true);
        md.setOverwriteConvoData(true);
        if (baseMediaPath.isPresent()) {
            md.setBaseMediaPath(baseMediaPath.get());
        }

        mdController.updateMetadata(md.getId(), md);
    }

    /**
     * Initializes metadata for a secondary blog
     * 
     * @param mdController REST controller for working with metadata
     * @param blogName     Name of the blog to be initialized
     */
    protected static void initAdditionalBlogMetadata(TEVMetadataRestController mdController, String blogName) {
        Metadata md = mdController.getMetadataForBlogOrDefault(blogName);
        md.setBlog(blogName);
        md.setIsDefault(false);
        md.setOverwritePostData(true);
        mdController.updateMetadata(md.getId(), md);
    }

}
