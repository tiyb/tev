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
import com.tiyb.tev.exception.XMLParsingException;

/**
 * <p>
 * Unit Tests for the <code>ConversationXmlReader</code> class. Since that class
 * parses the XML and then inserts the data into the DB (via the REST
 * controller), these tests verify the end result: that the data is inserted
 * into the DB as expected.</p?
 * 
 * <p>
 * A <code>test-messages-xml</code> XML file is used for populating the DB.
 * </p>
 * 
 * @author tiyb
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ConversationXmlParsingUnitTests {

	@Autowired
	private TEVConvoRestController convoRestController;
	@Autowired
	private TEVMetadataRestController mdRestController;

	private static final String convo1Participant = "participant1";
	private static final String convo2Participant = "participant2";
	private static final String convo3Participant = "participant3";
	private static final String convo4Participant = "participant4";
	private static final String convo5Participant = "participant5";
	private static final String oldNameParticipantId = "foaiehoihafoei";
	private static final String tobeDeactivatedId = "afoiehaifeh";
	private static final String blankParticipant = "NO NAME";
	private static final String duplicateParticipant = "participant1 1";
	
	private static final int TOTAL_NUM_CONVOS = 8;
	
	private static final String BLOG_NAME = "blogname";

	/**
	 * Run before each test to populate the DB fresh, so that the Unit Tests can
	 * test their individual pieces. All Unit Tests in this class rely on the data
	 * to be populated by this method.
	 * 
	 * @throws IOException
	 */
	@Before
	public void Setup() throws IOException {
		Metadata md = mdRestController.getMetadataForBlogOrDefault(BLOG_NAME);
		md.setMainTumblrUser(BLOG_NAME);
		md.setOverwriteConvoData(true);
		mdRestController.updateMetadata(md.getId(), md);

		File rawXmlFile = ResourceUtils.getFile("classpath:XML/test-messages-xml.xml");
		InputStream xmlFile = new FileInputStream(rawXmlFile);
		MockMultipartFile mockFile = new MockMultipartFile("testmessages", xmlFile);

		convoRestController.deleteAllConversationsForBlog(BLOG_NAME);
		convoRestController.deleteAllConvoMsgsForBlog(BLOG_NAME);
		ConversationXmlReader.parseDocument(mockFile, mdRestController, convoRestController, BLOG_NAME);
	}
	
	/**
	 * Verifies that all of the conversations in the test XML input file have been
	 * properly imported into the database. Since it is known how many conversation
	 * messages are part of each conversation, as well as the participant avatar
	 * URLs, this information is asserted to be present.
	 */
	@Test
	public void checkConversations() {
		List<Conversation> convos = convoRestController.getAllConversationsForBlog(BLOG_NAME);

		assertThat(convos).isNotNull();
		assertThat(convos.size()).isEqualTo(TOTAL_NUM_CONVOS);
		
		Conversation firstConvo = convoRestController.getConversationForBlogByParticipant(BLOG_NAME, convo1Participant);
		assertThat(firstConvo).isNotNull();
		assertThat(firstConvo.getNumMessages()).isEqualTo(9);
		assertThat(firstConvo.getParticipantAvatarUrl()).isEqualTo("http://participant1/avatar");

		Conversation secondConvo = convoRestController.getConversationForBlogByParticipant(BLOG_NAME, convo2Participant);
		assertThat(secondConvo).isNotNull();
		assertThat(secondConvo.getNumMessages()).isEqualTo(9);
		assertThat(secondConvo.getParticipantAvatarUrl()).isEqualTo("http://participant2/avatar");

		Conversation thirdConvo = convoRestController.getConversationForBlogByParticipant(BLOG_NAME, convo3Participant);
		assertThat(thirdConvo).isNotNull();
		assertThat(thirdConvo.getNumMessages()).isEqualTo(1);

		Conversation fourthConvo = convoRestController.getConversationForBlogByParticipant(BLOG_NAME, convo4Participant);
		assertThat(fourthConvo).isNotNull();
		assertThat(fourthConvo.getNumMessages()).isEqualTo(1);

		Conversation changedConvo = convoRestController.getConversationForBlogByParticipantIdOrName(BLOG_NAME, oldNameParticipantId, "");
		assertThat(changedConvo).isNotNull();
		assertThat(changedConvo.getNumMessages()).isEqualTo(1);
		assertThat(changedConvo.getParticipant()).isEqualTo("participant-oldname");
		assertThat(changedConvo.getParticipantAvatarUrl()).isEqualTo("http://participanton/avatar");

		Conversation toBeDeactConvo = convoRestController.getConversationForBlogByParticipantIdOrName(BLOG_NAME, tobeDeactivatedId, "");
		assertThat(toBeDeactConvo).isNotNull();
		assertThat(toBeDeactConvo.getNumMessages()).isEqualTo(2);
		assertThat(toBeDeactConvo.getParticipant()).isEqualTo("goingtobedeactivated");
		assertThat(toBeDeactConvo.getParticipantAvatarUrl()).isEqualTo("http://goingtobedeac/avatar");
		
		Conversation blankParticipantConvo = convoRestController.getConversationForBlogByParticipant(BLOG_NAME, blankParticipant);
		assertThat(blankParticipantConvo).isNotNull();
		assertThat(blankParticipantConvo.getNumMessages()).isEqualTo(1);
		assertThat(blankParticipantConvo.getParticipant()).isEqualTo("NO NAME");
		assertThat(blankParticipantConvo.getParticipantAvatarUrl()).isEmpty();
		
		Conversation dupNameConvo = convoRestController.getConversationForBlogByParticipant(BLOG_NAME, duplicateParticipant);
		assertThat(dupNameConvo).isNotNull();
		assertThat(dupNameConvo.getNumMessages()).isEqualTo(1);
		assertThat(dupNameConvo.getParticipant()).isEqualTo("participant1 1");
		assertThat(dupNameConvo.getParticipantAvatarUrl()).isEqualTo("http://duplicatename/avatar");
	}

	/**
	 * Verifies that cases where a participant is blank (which happens sometimes)
	 * are handled correctly; the solution <i>should</i> insert a default name for
	 * that participant name.
	 */
	@Test
	public void checkBlankParticipantLoading() {
		List<Conversation> convos = convoRestController.getAllConversationsForBlog(BLOG_NAME);

		assertThat(convos).isNotNull();
		assertThat(convos.size()).isEqualTo(TOTAL_NUM_CONVOS);

		for (Conversation c : convos) {
			assertThat(c.getParticipant()).isNotBlank();
		}
	}

	/**
	 * Tests that Conversations with only sent messages can still be retrieved via
	 * participant name, even though no ID was created. (The
	 * <code>testAddingConvos()</code> method will test that the conversation can be
	 * updated properly.) Would make more logical sense to put this test in the
	 * {@link com.tiyb.tev.controller.TevPostRestControllerUnitTests
	 * TevRestControllerUnitTests} class, but is included here instead since the
	 * proper set has been done for inserting the data into the DB.
	 */
	@Test
	public void testRetrieveConvoWithNoId() {
		Conversation returnedConvo = convoRestController.getConversationForBlogByParticipantIdOrName(BLOG_NAME, "", convo3Participant);
		assertThat(returnedConvo).isNotNull();
		assertThat(returnedConvo.getNumMessages()).isEqualTo(1);
		assertThat(returnedConvo.getParticipant()).isEqualTo(convo3Participant);
		assertThat(returnedConvo.getParticipantAvatarUrl()).isEqualTo("http://participant3/avatar");
		assertThat(returnedConvo.getParticipantId()).isBlank();
	}

	/**
	 * Verifies that all of the conversation messages in the test XML input file
	 * have been properly imported into the database for Conversation 1. Since the
	 * message, type, and timestamp is known for each conversation message, this
	 * information is asserted to be present.
	 */
	@Test
	public void checkConvo1Messages() {
		Conversation convo = convoRestController.getConversationForBlogByParticipant(BLOG_NAME, convo1Participant);
		List<ConversationMessage> msgs = convoRestController.getConvoMsgForBlogByConvoID(BLOG_NAME, convo.getId());
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
	 * Verifies that all of the conversation messages in the test XML input file
	 * have been properly imported into the database for Conversation 2. Since the
	 * message, type, and timestamp is known for each conversation message, this
	 * information is asserted to be present.
	 */
	@Test
	public void checkConvo2Messages() {
		Conversation convo = convoRestController.getConversationForBlogByParticipant(BLOG_NAME, convo2Participant);
		List<ConversationMessage> msgs = convoRestController.getConvoMsgForBlogByConvoID(BLOG_NAME, convo.getId());
		assertThat(msgs).isNotNull();
		assertThat(msgs.size()).isEqualTo(9);

		assertThat(msgs.get(0).getMessage()).isEqualTo("Message 1");
		assertThat(msgs.get(0).getType()).isEqualTo("TEXT");
		assertThat(msgs.get(0).getReceived()).isEqualTo(true);
		assertThat(msgs.get(0).getTimestamp()).isEqualTo(1544012468L);

		assertThat(msgs.get(1).getMessage()).isEqualTo("Message 2");
		assertThat(msgs.get(1).getType()).isEqualTo("TEXT");
		assertThat(msgs.get(1).getReceived()).isEqualTo(false);
		assertThat(msgs.get(1).getTimestamp()).isEqualTo(1544016206L);

		assertThat(msgs.get(2).getMessage()).isEqualTo("Message 3");
		assertThat(msgs.get(2).getType()).isEqualTo("TEXT");
		assertThat(msgs.get(2).getReceived()).isEqualTo(true);
		assertThat(msgs.get(2).getTimestamp()).isEqualTo(1544016402L);

		assertThat(msgs.get(3).getMessage()).isEqualTo("Message 4");
		assertThat(msgs.get(3).getType()).isEqualTo("TEXT");
		assertThat(msgs.get(3).getReceived()).isEqualTo(true);
		assertThat(msgs.get(3).getTimestamp()).isEqualTo(1544016410L);

		assertThat(msgs.get(4).getMessage()).isEqualTo("Message 5");
		assertThat(msgs.get(4).getType()).isEqualTo("TEXT");
		assertThat(msgs.get(4).getReceived()).isEqualTo(true);
		assertThat(msgs.get(4).getTimestamp()).isEqualTo(1544016579L);

		assertThat(msgs.get(5).getMessage()).isEqualTo("Message 6");
		assertThat(msgs.get(5).getType()).isEqualTo("TEXT");
		assertThat(msgs.get(5).getReceived()).isEqualTo(true);
		assertThat(msgs.get(5).getTimestamp()).isEqualTo(1544016582L);

		assertThat(msgs.get(6).getMessage()).isEqualTo("Message 7");
		assertThat(msgs.get(6).getType()).isEqualTo("TEXT");
		assertThat(msgs.get(6).getReceived()).isEqualTo(false);
		assertThat(msgs.get(6).getTimestamp()).isEqualTo(1544022051L);

		assertThat(msgs.get(7).getMessage()).isEqualTo("Message 8");
		assertThat(msgs.get(7).getType()).isEqualTo("TEXT");
		assertThat(msgs.get(7).getReceived()).isEqualTo(true);
		assertThat(msgs.get(7).getTimestamp()).isEqualTo(1544115936L);

		assertThat(msgs.get(8).getMessage()).isEqualTo("Message 9");
		assertThat(msgs.get(8).getType()).isEqualTo("TEXT");
		assertThat(msgs.get(8).getReceived()).isEqualTo(false);
		assertThat(msgs.get(8).getTimestamp()).isEqualTo(1544126671L);
	}

	/**
	 * Verifies that all of the conversation messages in the test XML input file
	 * have been properly imported into the database for Conversation 3. Since the
	 * message, type, and timestamp is known for each conversation message, this
	 * information is asserted to be present.
	 */
	@Test
	public void checkConvo3Messages() {
		Conversation convo = convoRestController.getConversationForBlogByParticipant(BLOG_NAME, convo3Participant);
		List<ConversationMessage> msgs = convoRestController.getConvoMsgForBlogByConvoID(BLOG_NAME, convo.getId());
		assertThat(msgs).isNotNull();
		assertThat(msgs.size()).isEqualTo(1);

		assertThat(msgs.get(0).getMessage()).isEqualTo("Message 1");
		assertThat(msgs.get(0).getType()).isEqualTo("TEXT");
		assertThat(msgs.get(0).getReceived()).isEqualTo(false);
		assertThat(msgs.get(0).getTimestamp()).isEqualTo(1544012468L);
	}

	/**
	 * Verifies that all of the conversation messages in the test XML input file
	 * have been properly imported into the database for Conversation 4. Since the
	 * message, type, and timestamp is known for each conversation message, this
	 * information is asserted to be present.
	 */
	@Test
	public void checkConvo4Messages() {
		Conversation convo = convoRestController.getConversationForBlogByParticipant(BLOG_NAME, convo4Participant);
		List<ConversationMessage> msgs = convoRestController.getConvoMsgForBlogByConvoID(BLOG_NAME, convo.getId());
		assertThat(msgs).isNotNull();
		assertThat(msgs.size()).isEqualTo(1);

		assertThat(msgs.get(0).getMessage()).isEqualTo("Message 1");
		assertThat(msgs.get(0).getType()).isEqualTo("TEXT");
		assertThat(msgs.get(0).getReceived()).isEqualTo(true);
		assertThat(msgs.get(0).getTimestamp()).isEqualTo(1544012468L);
	}

	/**
	 * Tests parsing of the Conversation XML in cases where the "overwrite
	 * conversations" flag is set to false; unchanged conversations should be left
	 * alone, new conversations should be uploaded, and conversations that have had
	 * new messages added should have those messages uploaded, and be marked as
	 * un-hidden.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testAddingConvos() throws IOException {
		List<Conversation> convos = convoRestController.getAllConversationsForBlog(BLOG_NAME);
		assertThat(convos.size()).isEqualTo(TOTAL_NUM_CONVOS);

		for (Conversation convo : convos) {
			convo.setHideConversation(true);
			convoRestController.updateConversationForBlog(BLOG_NAME, convo.getId(), convo);
		}

		Metadata md = mdRestController.getMetadataForBlog(BLOG_NAME);
		md.setOverwriteConvoData(false);
		md = mdRestController.updateMetadata(md.getId(), md);

		File rawXmlFile = ResourceUtils.getFile("classpath:XML/test-messages-extended-xml.xml");
		InputStream xmlFile = new FileInputStream(rawXmlFile);
		MockMultipartFile mockFile = new MockMultipartFile("testmessages", xmlFile);
		ConversationXmlReader.parseDocument(mockFile, mdRestController, convoRestController, BLOG_NAME);

		convos = convoRestController.getAllConversationsForBlog(BLOG_NAME);
		assertThat(convos).isNotNull();
		assertThat(convos.size()).isEqualTo(TOTAL_NUM_CONVOS + 1);

		Conversation convo = convoRestController.getConversationForBlogByParticipant(BLOG_NAME, convo1Participant);
		assertThat(convo).isNotNull();
		assertThat(convo.getHideConversation()).isEqualTo(false);
		List<ConversationMessage> messages = convoRestController.getConvoMsgForBlogByConvoID(BLOG_NAME, convo.getId());
		assertThat(messages).isNotNull();
		assertThat(messages.size()).isEqualTo(10);

		convo = convoRestController.getConversationForBlogByParticipant(BLOG_NAME, convo2Participant);
		assertThat(convo).isNotNull();
		assertThat(convo.getHideConversation()).isEqualTo(true);

		convo = convoRestController.getConversationForBlogByParticipant(BLOG_NAME, convo3Participant);
		assertThat(convo).isNotNull();
		assertThat(convo.getHideConversation()).isEqualTo(false);
		assertThat(convo.getNumMessages()).isEqualTo(2);
		messages = convoRestController.getConvoMsgForBlogByConvoID(BLOG_NAME, convo.getId());
		assertThat(messages).isNotNull();
		assertThat(messages.size()).isEqualTo(2);

		convo = convoRestController.getConversationForBlogByParticipant(BLOG_NAME, convo4Participant);
		assertThat(convo).isNotNull();
		assertThat(convo.getHideConversation()).isEqualTo(true);

		convo = convoRestController.getConversationForBlogByParticipant(BLOG_NAME, convo5Participant);
		assertThat(convo).isNotNull();
		assertThat(convo.getHideConversation()).isEqualTo(false);
		messages = convoRestController.getConvoMsgForBlogByConvoID(BLOG_NAME, convo.getId());
		assertThat(messages).isNotNull();
		assertThat(messages.size()).isEqualTo(1);

		ConversationMessage message = messages.get(0);
		assertThat(message.getMessage()).isEqualTo("Message 1");
		assertThat(message.getType()).isEqualTo("TEXT");
		assertThat(message.getReceived()).isEqualTo(true);
		assertThat(message.getTimestamp()).isEqualTo(1544012468L);

		convo = convoRestController.getConversationForBlogByParticipantIdOrName(BLOG_NAME, oldNameParticipantId, "");
		assertThat(convo).isNotNull();
		assertThat(convo.getHideConversation()).isEqualTo(false);
		assertThat(convo.getNumMessages()).isEqualTo(1);
		assertThat(convo.getParticipant()).isEqualTo("participant-newname");
		assertThat(convo.getParticipantAvatarUrl()).isEqualTo("http://participantnn/avatar");

		convo = convoRestController.getConversationForBlogByParticipantIdOrName(BLOG_NAME, tobeDeactivatedId, "");
		assertThat(convo).isNotNull();
		assertThat(convo.getHideConversation()).isEqualTo(true);
		assertThat(convo.getNumMessages()).isEqualTo(2);
		assertThat(convo.getParticipant()).isEqualTo("goingtobedeactivated");
		assertThat(convo.getParticipantAvatarUrl()).isEqualTo("http://goingtobedeac/avatar");
	}

	/**
	 * Tests that parsing an invalid XML file throws the proper exception
	 * 
	 * @throws IOException
	 */
	@Test(expected = XMLParsingException.class)
	public void testBadXml() throws IOException {
		File rawXmlFile = ResourceUtils.getFile("classpath:XML/test-messages-badxml.xml");
		InputStream xmlFile = new FileInputStream(rawXmlFile);
		MockMultipartFile mockFile = new MockMultipartFile("testmessages", xmlFile);

		ConversationXmlReader.parseDocument(mockFile, mdRestController, convoRestController, BLOG_NAME);
	}
}
