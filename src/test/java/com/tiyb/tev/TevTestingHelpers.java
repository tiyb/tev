package com.tiyb.tev;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.ResourceUtils;

import com.tiyb.tev.controller.TEVConvoRestController;
import com.tiyb.tev.controller.TEVMetadataRestController;
import com.tiyb.tev.controller.TEVPostRestController;
import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.xml.BlogXmlReader;
import com.tiyb.tev.xml.ConversationXmlReader;

/**
 * Helper methods/constants used in unit tests. Not a best practice -- maybe
 * even an anti-pattern -- but good enough for unit tests
 * 
 * @author tiyb
 *
 */
public abstract class TevTestingHelpers {

    public final static String MAIN_INPUT_XML_FILE = "classpath:XML/test-post-xml.xml";
    public final static String SECONDARY_INPUT_XML_FILE = "classpath:XML/test-post-secondblog.xml";
    public final static String MAIN_CONVO_XML_FILE = "classpath:XML/test-messages-xml.xml";

    public final static String MAIN_BLOG_NAME = "mainblog";
    public final static String SECOND_BLOG_NAME = "secondblog";

    /**
     * Initializes the blog with data from the sample XML file
     * 
     * @param mdController   REST controller for working with metadata
     * @param postController REST controller for working with post data
     * @param baseMediaPath  Optional path to be used for media
     * @throws FileNotFoundException If the input XML file can' be loaded for some
     *                               reason
     */
    public static void initDataForMainBlog(TEVMetadataRestController mdController, TEVPostRestController postController,
            Optional<String> baseMediaPath) throws FileNotFoundException {
        initMainBlogNoData(mdController, baseMediaPath);

        readPostXml(MAIN_INPUT_XML_FILE, postController, MAIN_BLOG_NAME);
    }
    
    public static void initDataForSecondaryBlog(TEVMetadataRestController mdController,
            TEVPostRestController postController, Optional<String> baseMediaPath) throws FileNotFoundException {
        initAdditionalBlog(mdController, SECOND_BLOG_NAME);
        
        readPostXml(SECONDARY_INPUT_XML_FILE, postController, SECOND_BLOG_NAME);
    }
    
    private static void readPostXml(String xmlFileToLoad, TEVPostRestController postController, String blogName)
            throws FileNotFoundException {
        File rawXmlFile = ResourceUtils.getFile(xmlFileToLoad);
        InputStream xmlFile = new FileInputStream(rawXmlFile);
        BlogXmlReader.parseDocument(xmlFile, postController, blogName);
    }
    
    public static void initConvoForMainBlog(TEVMetadataRestController mdController, TEVConvoRestController convoController) throws IOException {
        initMainBlogNoData(mdController, Optional.empty());
        
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
    public static void initMainBlogNoData(TEVMetadataRestController mdController, Optional<String> baseMediaPath) {
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
    
    public static void initAdditionalBlog(TEVMetadataRestController mdController, String blogName) {
        Metadata md = mdController.getMetadataForBlogOrDefault(blogName);
        md.setBlog(blogName);
        md.setIsDefault(false);
        md.setOverwritePostData(true);
        mdController.updateMetadata(md.getId(), md);
    }

}
