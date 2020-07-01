package com.tiyb.tev.html;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.gargoylesoftware.htmlunit.WebClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ConvoHtmlTests {

    @LocalServerPort
    private int serverPort;

    private WebClient webClient;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setupSite() {
        webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.addRequestHeader("Accept-Language", "en");

        HtmlTestingHelpers.restInitDataForMainBlog(restTemplate, serverPort, Optional.empty());
        // TODO upload convos to server
    }

    @Test
    public void showReadingPane() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void showPopups() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void showWordCloud() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void showTable() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void openConvo() {
        assertThat(true).isEqualTo(false);
    }

    @Test
    public void unhideAllConvos() {
        assertThat(true).isEqualTo(false);
    }
}
