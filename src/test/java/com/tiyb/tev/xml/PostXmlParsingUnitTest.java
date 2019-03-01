package com.tiyb.tev.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import com.tiyb.tev.controller.TEVRestController;
import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Type;
import com.tiyb.tev.datamodel.helpers.TEVSuperClass;
import com.tiyb.tev.repository.AnswerRepository;
import com.tiyb.tev.repository.ConversationMessageRepository;
import com.tiyb.tev.repository.ConversationRepository;
import com.tiyb.tev.repository.LinkRepository;
import com.tiyb.tev.repository.MetadataRepository;
import com.tiyb.tev.repository.PhotoRepository;
import com.tiyb.tev.repository.PostRepository;
import com.tiyb.tev.repository.RegularRepository;
import com.tiyb.tev.repository.TypeRepository;
import com.tiyb.tev.repository.VideoRepository;

@RunWith(SpringRunner.class)
@WebMvcTest(TEVRestController.class)
public class PostXmlParsingUnitTest {

	private TEVSuperClass tsc;
	
	@Autowired
	private TEVRestController restController;
	@MockBean
	PostRepository postRepo;
	@MockBean
	TypeRepository typeRepo;
	@MockBean
	RegularRepository regularRepo;
	@MockBean
	AnswerRepository answerRepo;
	@MockBean
	LinkRepository linkRepo;
	@MockBean
	PhotoRepository photoRepo;
	@MockBean
	VideoRepository videoRepo;
	@MockBean
	MetadataRepository metadataRepo;
	@MockBean
	ConversationRepository conversationRepo;
	@MockBean
	ConversationMessageRepository convoMsgRepo;
	
	@Before
	public void setupData() throws FileNotFoundException {
		List<Type> allowableTypes = restController.getAllTypes();
		
		File rawXmlFile = ResourceUtils.getFile("classpath:XML/test-post-xml.xml");
		InputStream xmlFile = new FileInputStream(rawXmlFile);
		
		tsc = BlogXmlReader.parseDocument(xmlFile, allowableTypes);
	}
	
	@Test
	public void testAnswer() {
		assertThat(tsc.getAnswers().size()).isEqualTo(1);
		assertThat(tsc.getAnswers().get(0).getQuestion()).isEqualTo("Question text");
		assertThat(tsc.getAnswers().get(0).getAnswer()).isEqualTo("Answer text");
		assertThat(tsc.getAnswers().get(0).getPostId()).isEqualTo(180371366195L);
	}
	
	@Test
	public void testLink() {
		assertThat(tsc.getLinks().size()).isEqualTo(1);
		assertThat(tsc.getLinks().get(0).getPostId()).isEqualTo(180265557725L);
		assertThat(tsc.getLinks().get(0).getDescription()).isEqualTo("This is the link description");
		assertThat(tsc.getLinks().get(0).getText()).isEqualTo("Tumblr");
		assertThat(tsc.getLinks().get(0).getUrl()).isEqualTo("https://someblog.tumblr.com/");
	}
	
	@Test
	public void testRegular() {
		assertThat(tsc.getRegulars().size()).isEqualTo(1);
		assertThat(tsc.getRegulars().get(0).getPostId()).isEqualTo(180894436671L);
		assertThat(tsc.getRegulars().get(0).getBody()).isEqualTo("post body text here");
		assertThat(tsc.getRegulars().get(0).getTitle()).isEqualTo("First Post");
	}
	
	@Test
	public void testVideo() {
		assertThat(tsc.getVideos().size()).isEqualTo(1);
		assertThat(tsc.getVideos().get(0).getContentType()).isEqualTo("video/mp4");
		assertThat(tsc.getVideos().get(0).getDuration()).isEqualTo(45);
		assertThat(tsc.getVideos().get(0).getExtension()).isEqualTo("mp4");
		assertThat(tsc.getVideos().get(0).getHeight()).isEqualTo(480);
		assertThat(tsc.getVideos().get(0).getPostId()).isEqualTo(180782992914L);
		assertThat(tsc.getVideos().get(0).getRevision()).isEqualTo("0");
		assertThat(tsc.getVideos().get(0).getVideoCaption()).isEqualTo("This is the caption for a video");
		assertThat(tsc.getVideos().get(0).getWidth()).isEqualTo(854);
	}
	
	@Test
	public void testPhotos() {
		assertThat(tsc.getPhotos().size()).isEqualTo(3);
		
		Photo photo = tsc.getPhotos().get(0);
		assertThat(photo.getPostId()).isEqualTo(180784644740L);
		assertThat(photo.getCaption()).isEqualTo("This is the caption for a photo post");
		//assertThat(photo.getPhotoLinkUrl()).isEqualTo("http://bit.ly/some-photo");
		assertThat(photo.getUrl1280()).isEqualTo("photo 1 1280");
		assertThat(photo.getUrl500()).isEqualTo("photo 1 500");
		assertThat(photo.getUrl400()).isEqualTo("photo 1 400");
		assertThat(photo.getUrl250()).isEqualTo("photo 1 250");
		assertThat(photo.getUrl100()).isEqualTo("photo 1 100");
		assertThat(photo.getUrl75()).isEqualTo("photo 1 75");
		
		photo = tsc.getPhotos().get(1);
		assertThat(photo.getPostId()).isEqualTo(180254465582L);
		assertThat(photo.getCaption()).isEqualTo("This is hte photo caption");
		assertThat(photo.getUrl1280()).isEqualTo("photo 3 1280");
		assertThat(photo.getUrl500()).isEqualTo("photo 3 500");
		assertThat(photo.getUrl400()).isEqualTo("photo 3 400");
		assertThat(photo.getUrl250()).isEqualTo("photo 3 250");
		assertThat(photo.getUrl100()).isEqualTo("photo 3 100");
		assertThat(photo.getUrl75()).isEqualTo("photo 3 75");
		
		photo = tsc.getPhotos().get(2);
		assertThat(photo.getPostId()).isEqualTo(180254465582L);
		assertThat(photo.getCaption()).isEqualTo("This is hte photo caption");
		assertThat(photo.getUrl1280()).isEqualTo("photo 4 1280");
		assertThat(photo.getUrl500()).isEqualTo("photo 4 500");
		assertThat(photo.getUrl400()).isEqualTo("photo 4 400");
		assertThat(photo.getUrl250()).isEqualTo("photo 4 250");
		assertThat(photo.getUrl100()).isEqualTo("photo 4 100");
		assertThat(photo.getUrl75()).isEqualTo("photo 4 75");
	}
}
