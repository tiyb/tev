package com.tiyb.tev.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import com.tiyb.tev.controller.TEVConvoRestController;
import com.tiyb.tev.controller.TEVMetadataRestController;
import com.tiyb.tev.datamodel.Conversation;
import com.tiyb.tev.datamodel.ConversationMessage;
import com.tiyb.tev.datamodel.Metadata;

/**
 * Unit tests for loading a second Conversation XML file, after an initial one
 * has already been loaded
 * 
 * @author tiyb
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ConversationXmlMultiBlog {

	@Autowired
	private TEVConvoRestController convoRestController;
	@Autowired
	private TEVMetadataRestController mdRestController;

	private static final String b2FirstParticipant = "secondblogparticipant1";
	private static final String b2DupParticipant = "participant1";

	private static final int TOTAL_NUM_B1_CONVOS = 8;
	private static final int TOTAL_NUM_B2_CONVOS = 2;

	private static final String BLOG_NAME1 = "blogname";
	private static final String BLOG_NAME2 = "secondblog";

	/**
	 * Run before each test to populate the DB fresh, so that the Unit Tests can
	 * test their individual pieces. All Unit Tests in this class rely on the data
	 * to be populated by this method.
	 * 
	 * @throws IOException
	 */
	@Before
	public void Setup() throws IOException {
		Metadata md1 = mdRestController.getMetadataForBlogOrDefault(BLOG_NAME1);
		md1.setMainTumblrUser(BLOG_NAME1);
		md1.setOverwriteConvoData(true);
		md1 = mdRestController.updateMetadata(md1.getId(), md1);
		Metadata md2 = mdRestController.getMetadataForBlogOrDefault(BLOG_NAME2);
		md2.setMainTumblrUser(BLOG_NAME2);
		md2.setOverwriteConvoData(true);
		md2 = mdRestController.updateMetadata(md2.getId(), md2);

		File rawXmlFile = ResourceUtils.getFile("classpath:XML/test-messages-xml.xml");
		InputStream xmlFile = new FileInputStream(rawXmlFile);
		MockMultipartFile mockFile = new MockMultipartFile("testmessages", xmlFile);

		convoRestController.deleteAllConvoMsgsForBlog(BLOG_NAME1);
		convoRestController.deleteAllConversationsForBlog(BLOG_NAME1);
		ConversationXmlReader.parseDocument(mockFile, mdRestController, convoRestController, BLOG_NAME1);

		rawXmlFile = ResourceUtils.getFile("classpath:XML/test-messages-secondblog-xml.xml");
		xmlFile = new FileInputStream(rawXmlFile);
		mockFile = new MockMultipartFile("testmessages2", xmlFile);

		convoRestController.deleteAllConvoMsgsForBlog(BLOG_NAME2);
		convoRestController.deleteAllConversationsForBlog(BLOG_NAME2);
		ConversationXmlReader.parseDocument(mockFile, mdRestController, convoRestController, BLOG_NAME2);
	}

	/**
	 * Quick test that both files have been loaded, separately, into their own blogs
	 */
	@Test
	public void checkConversations() {
		List<Conversation> convos = convoRestController.getAllConversationsForBlog(BLOG_NAME1);
		assertThat(convos).isNotNull();
		assertThat(convos.size()).isEqualTo(TOTAL_NUM_B1_CONVOS);

		convos = convoRestController.getAllConversationsForBlog(BLOG_NAME2);
		assertThat(convos).isNotNull();
		assertThat(convos.size()).isEqualTo(TOTAL_NUM_B2_CONVOS);

		Conversation firstConvo = convoRestController.getConversationForBlogByParticipant(BLOG_NAME2,
				b2FirstParticipant);
		assertThat(firstConvo).isNotNull();
		assertThat(firstConvo.getNumMessages()).isEqualTo(9);
		assertThat(firstConvo.getParticipant()).isEqualTo(b2FirstParticipant);
		assertThat(firstConvo.getParticipantAvatarUrl()).isEqualTo("http://secondblogparticipant1/avatar");

		Conversation dupConvo = convoRestController.getConversationForBlogByParticipant(BLOG_NAME2, b2DupParticipant);
		assertThat(dupConvo).isNotNull();
		assertThat(dupConvo.getNumMessages()).isEqualTo(2);
		assertThat(dupConvo.getParticipant()).isEqualTo(b2DupParticipant);
		assertThat(dupConvo.getParticipantAvatarUrl()).isEqualTo("http://participant1/avatar");
	}

	/**
	 * Test that mutliple blogs can have conversations with the same participant
	 */
	@Test
	public void sameParticipantMultiBlogs() {
		Conversation convo1 = convoRestController.getConversationForBlogByParticipant(BLOG_NAME1, b2DupParticipant);
		assertThat(convo1).isNotNull();
		assertThat(convo1.getParticipantAvatarUrl()).isEqualTo("http://participant1/avatar");
		assertThat(convo1.getNumMessages()).isEqualTo(9);

		Conversation convo2 = convoRestController.getConversationForBlogByParticipant(BLOG_NAME2, b2DupParticipant);
		assertThat(convo2).isNotNull();
		assertThat(convo2.getParticipantAvatarUrl()).isEqualTo("http://participant1/avatar");
		assertThat(convo2.getNumMessages()).isEqualTo(2);
	}

	/**
	 * Checks that the first conversation from the second blog has been read in
	 * correctly
	 */
	@Test
	public void checkConvo1Messages() {
		Conversation convo = convoRestController.getConversationForBlogByParticipant(BLOG_NAME2, b2FirstParticipant);
		List<ConversationMessage> msgs = convoRestController.getConvoMsgForBlogByConvoID(BLOG_NAME2, convo.getId());
		assertThat(msgs).isNotNull();
		assertThat(msgs.size()).isEqualTo(9);

		assertThat(msgs.get(0).getMessage()).isEqualTo("Message 1");
		assertThat(msgs.get(0).getType()).isEqualTo("TEXT");
		assertThat(msgs.get(0).getReceived()).isEqualTo(false);
		assertThat(msgs.get(0).getTimestamp()).isEqualTo(1544197586L);

		assertThat(msgs.get(1).getMessage()).isEqualTo("Message 2");
		assertThat(msgs.get(1).getType()).isEqualTo("TEXT");
		assertThat(msgs.get(1).getReceived()).isEqualTo(false);
		assertThat(msgs.get(1).getTimestamp()).isEqualTo(1544197605L);

		assertThat(msgs.get(2).getMessage()).isEqualTo("Message 3");
		assertThat(msgs.get(2).getType()).isEqualTo("TEXT");
		assertThat(msgs.get(2).getReceived()).isEqualTo(false);
		assertThat(msgs.get(2).getTimestamp()).isEqualTo(1544197624L);

		assertThat(msgs.get(3).getMessage()).isEqualTo("http://www.photourl.com/photo.png");
		assertThat(msgs.get(3).getType()).isEqualTo("IMAGE");
		assertThat(msgs.get(3).getReceived()).isEqualTo(false);
		assertThat(msgs.get(3).getTimestamp()).isEqualTo(1544197647L);

		assertThat(msgs.get(4).getMessage()).isEqualTo("Message 5");
		assertThat(msgs.get(4).getType()).isEqualTo("TEXT");
		assertThat(msgs.get(4).getReceived()).isEqualTo(false);
		assertThat(msgs.get(4).getTimestamp()).isEqualTo(1544198315L);

		assertThat(msgs.get(5).getMessage()).isEqualTo("Message 6");
		assertThat(msgs.get(5).getType()).isEqualTo("TEXT");
		assertThat(msgs.get(5).getReceived()).isEqualTo(true);
		assertThat(msgs.get(5).getTimestamp()).isEqualTo(1544221130L);

		assertThat(msgs.get(6).getMessage()).isEqualTo("Message 7");
		assertThat(msgs.get(6).getType()).isEqualTo("TEXT");
		assertThat(msgs.get(6).getReceived()).isEqualTo(false);
		assertThat(msgs.get(6).getTimestamp()).isEqualTo(1544221197L);

		assertThat(msgs.get(7).getMessage()).isEqualTo("Message 8");
		assertThat(msgs.get(7).getType()).isEqualTo("TEXT");
		assertThat(msgs.get(7).getReceived()).isEqualTo(true);
		assertThat(msgs.get(7).getTimestamp()).isEqualTo(1544221203L);

		assertThat(msgs.get(8).getMessage()).isEqualTo("http://www.tumblr.com/somepost");
		assertThat(msgs.get(8).getType()).isEqualTo("POSTREF");
		assertThat(msgs.get(8).getReceived()).isEqualTo(false);
		assertThat(msgs.get(8).getTimestamp()).isEqualTo(1544221221L);
	}

	/**
	 * Checks that the 2nd conversation from the second blog has been read in
	 * correctly
	 */
	@Test
	public void checkConvo2Messages() {
		Conversation convo = convoRestController.getConversationForBlogByParticipant(BLOG_NAME2, b2DupParticipant);
		List<ConversationMessage> msgs = convoRestController.getConvoMsgForBlogByConvoID(BLOG_NAME2, convo.getId());
		assertThat(msgs).isNotNull();
		assertThat(msgs.size()).isEqualTo(2);

		assertThat(msgs.get(0).getMessage()).isEqualTo("Message 1");
		assertThat(msgs.get(0).getType()).isEqualTo("TEXT");
		assertThat(msgs.get(0).getReceived()).isEqualTo(true);
		assertThat(msgs.get(0).getTimestamp()).isEqualTo(1544012468L);

		assertThat(msgs.get(1).getMessage()).isEqualTo("Message 2");
		assertThat(msgs.get(1).getType()).isEqualTo("TEXT");
		assertThat(msgs.get(1).getReceived()).isEqualTo(false);
		assertThat(msgs.get(1).getTimestamp()).isEqualTo(1544012470L);
	}

	/**
	 * Tests that another conversation can be added successfully, without impacting
	 * the other blog
	 */
	@Test
	public void testAddingConvos() {
		Conversation newConvo = new Conversation();
		newConvo.setBlog(BLOG_NAME2);
		newConvo.setParticipant("brandnewparticipant");
		newConvo = convoRestController.createConversationForBlog(BLOG_NAME2, newConvo);

		List<Conversation> b1Convos = convoRestController.getAllConversationsForBlog(BLOG_NAME1);
		assertThat(b1Convos).isNotNull();
		assertThat(b1Convos.size()).isEqualTo(TOTAL_NUM_B1_CONVOS);

		List<Conversation> b2Convos = convoRestController.getAllConversationsForBlog(BLOG_NAME2);
		assertThat(b2Convos).isNotNull();
		assertThat(b2Convos.size()).isEqualTo(TOTAL_NUM_B2_CONVOS + 1);
	}

}
