package com.tiyb.tev.html;

import java.util.Optional;

import org.springframework.boot.test.web.client.TestRestTemplate;

import com.tiyb.tev.TevTestingHelpers;
import com.tiyb.tev.datamodel.Answer;
import com.tiyb.tev.datamodel.Link;
import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.Regular;
import com.tiyb.tev.datamodel.Video;

public abstract class HtmlTestingHelpers {

    public static void restInitDataForMainBlog(TestRestTemplate restTemplate, int serverPort,
            Optional<String> baseMediaPath) {
        restInitMainBlogSettings(restTemplate, serverPort, baseMediaPath);

        restTemplate.delete(baseUri(serverPort) + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME + "/regulars");
        restTemplate.delete(baseUri(serverPort) + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME + "/answers");
        restTemplate.delete(baseUri(serverPort) + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME + "/links");
        restTemplate.delete(baseUri(serverPort) + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME + "/photos");
        restTemplate.delete(baseUri(serverPort) + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME + "/videos");
        restTemplate.delete(baseUri(serverPort) + "/api/hashtags/" + TevTestingHelpers.MAIN_BLOG_NAME);
        restTemplate.delete(baseUri(serverPort) + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME);

        for (Post post : TevTestingHelpers.postsForUploading) {
            restTemplate.postForObject(baseUri(serverPort) + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME, post,
                    Post.class);
        }

        for (Regular reg : TevTestingHelpers.regularsForUploading) {
            restTemplate.postForObject(baseUri(serverPort) + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME + "/"
                    + reg.getPostId() + "/regular", reg, Regular.class);
        }

        for (Answer answer : TevTestingHelpers.answersForUploading) {
            restTemplate.postForObject(baseUri(serverPort) + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME + "/"
                    + answer.getPostId() + "/answer", answer, Answer.class);
        }

        for (Link link : TevTestingHelpers.linksForUploading) {
            restTemplate.postForObject(baseUri(serverPort) + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME + "/"
                    + link.getPostId() + "/link", link, Link.class);
        }

        for (Photo photo : TevTestingHelpers.photosForUploading) {
            restTemplate.postForObject(
                    baseUri(serverPort) + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME + "/photo", photo,
                    Photo.class);
        }

        for (Video vid : TevTestingHelpers.videosForUploading) {
            restTemplate.postForObject(baseUri(serverPort) + "/api/posts/" + TevTestingHelpers.MAIN_BLOG_NAME + "/"
                    + vid.getPostId() + "/video", vid, Video.class);
        }
    }

    public static void restInitMainBlogSettings(TestRestTemplate restTemplate, int serverPort,
            Optional<String> baseMediaPath) {
        Metadata md = restTemplate.getForObject(
                baseUri(serverPort) + "/api/metadata/byBlog/" + TevTestingHelpers.MAIN_BLOG_NAME + "/orDefault",
                Metadata.class);
        md.setOverwritePostData(true);
        md.setOverwriteConvoData(true);
        md.setMainTumblrUser(TevTestingHelpers.MAIN_BLOG_NAME);
        md.setIsDefault(true);
        md.setConversationDisplayStyle("table");
        md.setConversationSortColumn("numMessages");
        md.setConversationSortOrder("Descending");
        md.setFavFilter("Show Everything");
        md.setFilter("Do Not Filter");
        md.setPageLength(10);
        md.setShowHashtagsForAllBlogs(true);
        md.setShowReadingPane(false);
        md.setSortColumn("ID");
        md.setSortOrder("Descending");
        md.setTheme("base");
        if (baseMediaPath.isPresent()) {
            md.setBaseMediaPath(baseMediaPath.get());
        }

        restTemplate.put(baseUri(serverPort) + "/api/metadata/" + md.getId(), md);
    }

    public static void restInitAdditionalBlog(TestRestTemplate restTemplate, int serverPort, String blogName) {
        Metadata md = restTemplate.getForObject(baseUri(serverPort) + "/api/metadata/byBlog/" + blogName + "/orDefault",
                Metadata.class);
        md.setBlog(blogName);
        md.setIsDefault(false);
        md.setOverwritePostData(true);

        restTemplate.put(baseUri(serverPort) + "/api/metadata/" + md.getId(), md);
    }

    public static String baseUri(int serverPort) {
        return "http://localhost:" + serverPort;
    }
}
