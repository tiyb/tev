package com.tiyb.tev.html;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class IndexHtmlTests {

    private WebClient webClient;
    
    @LocalServerPort
    private int serverPort;
    
    @Before
    public void setupWC() {
        webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnScriptError(false);
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
