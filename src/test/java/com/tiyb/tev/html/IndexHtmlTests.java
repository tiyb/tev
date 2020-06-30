package com.tiyb.tev.html;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.tiyb.tev.TevTestingHelpers;
import com.tiyb.tev.datamodel.Metadata;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class IndexHtmlTests {

    private WebClient webClient;

    @LocalServerPort
    private int serverPort;

    @Autowired
    private TestRestTemplate restTemplate;
    private Logger logger = LoggerFactory.getLogger(IndexHtmlTests.class);

    @Before
    public void setupWC() {
        webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnScriptError(false);

        Metadata md = restTemplate.getForObject("http://localhost:" + this.serverPort + "/api/metadata/byBlog/"
                + TevTestingHelpers.MAIN_BLOG_NAME + "/orDefault", Metadata.class);
        md.setOverwritePostData(true);
        md.setOverwriteConvoData(true);
        md.setMainTumblrUser(TevTestingHelpers.MAIN_BLOG_NAME);
        md.setIsDefault(true);
        restTemplate.put("http://localhost:" + this.serverPort + "/api/metadata/" + md.getId(), md);
        md = restTemplate.getForObject("http://localhost:" + this.serverPort + "/api/metadata/byBlog/"
                + TevTestingHelpers.MAIN_BLOG_NAME + "/orDefault", Metadata.class);
    }

    @After
    public void close() {
        webClient.close();
    }

    @Test
    public void loadApplication() throws Exception {
        String url = "http://localhost:" + this.serverPort;
        HtmlPage page = webClient.getPage(url);

        List<String> pageTitles = new ArrayList<String>();
        pageTitles.add("Index");
        pageTitles.add("Settings");
        assertThat(page.getTitleText()).isIn(pageTitles);
    }
}
