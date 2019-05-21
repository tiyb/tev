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

import com.tiyb.tev.controller.TEVRestController;
import com.tiyb.tev.datamodel.Conversation;
import com.tiyb.tev.datamodel.ConversationMessage;
import com.tiyb.tev.datamodel.Metadata;

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
	private TEVRestController restController;

	/**
	 * Run before each test to populate the DB fresh, so that the Unit Tests can
	 * test their individual pieces. All Unit Tests in this class rely on the data
	 * to be populated by this method.
	 * 
	 * @throws IOException
	 */
	@Before
	public void Setup() throws IOException {
		Metadata md = restController.getMetadata();
		md.setMainTumblrUser("blogname");
		restController.updateMetadata(md);

		File rawXmlFile = ResourceUtils.getFile("classpath:XML/test-messages-xml.xml");
		InputStream xmlFile = new FileInputStream(rawXmlFile);
		MockMultipartFile mockFile = new MockMultipartFile("testmessages", xmlFile);

		restController.deleteAllConversations();
		restController.deleteAllConvoMsgs();
		ConversationXmlReader.parseDocument(mockFile, restController);
	}

	/**
	 * Verifies that all of the conversations in the test XML input file have been
	 * properly imported into the database. Since it is known how many conversation
	 * messages are part of each conversation, as well as the participant avatar
	 * URLs, this information is asserted to be present.
	 */
	@Test
	public void checkConversations() {
		List<Conversation> convos = restController.getAllConversations();

		assertThat(convos).isNotNull();
		assertThat(convos.size()).isEqualTo(4);

		Conversation firstConvo = restController.getConversationByParticipant("participant1");
		assertThat(firstConvo).isNotNull();
		assertThat(firstConvo.getNumMessages()).isEqualTo(9);
		assertThat(firstConvo.getParticipantAvatarUrl()).isEqualTo("http://participant1/avatar");

		Conversation secondConvo = restController.getConversationByParticipant("participant2");
		assertThat(secondConvo).isNotNull();
		assertThat(secondConvo.getNumMessages()).isEqualTo(9);
		assertThat(secondConvo.getParticipantAvatarUrl()).isEqualTo("http://participant2/avatar");

		Conversation thirdConvo = restController.getConversationByParticipant("participant3");
		assertThat(thirdConvo).isNotNull();
		assertThat(thirdConvo.getNumMessages()).isEqualTo(1);

		Conversation fourthConvo = restController.getConversationByParticipant("participant4");
		assertThat(fourthConvo).isNotNull();
		assertThat(fourthConvo.getNumMessages()).isEqualTo(1);
	}

	/**
	 * Verifies that all of the conversation messages in the test XML input file
	 * have been properly imported into the database. Since the message, type, and
	 * timestamp is known for each conversation message, this information is
	 * asserted to be present.
	 */
	@Test
	public void checkMessages() {
		Conversation firstConvo = restController.getConversationByParticipant("participant1");
		List<ConversationMessage> firstConvoMsgs = restController.getConvoMsgByConvoID(firstConvo.getId());
		assertThat(firstConvoMsgs).isNotNull();
		assertThat(firstConvoMsgs.size()).isEqualTo(9);

		assertThat(firstConvoMsgs.get(0).getMessage()).isEqualTo("Message 1");
		assertThat(firstConvoMsgs.get(0).getType()).isEqualTo("TEXT");
		assertThat(firstConvoMsgs.get(0).getReceived()).isEqualTo(false);
		assertThat(firstConvoMsgs.get(0).getTimestamp()).isEqualTo(1544197586L);

		assertThat(firstConvoMsgs.get(3).getMessage()).isEqualTo("http://www.photourl.com/photo.png");
		assertThat(firstConvoMsgs.get(3).getType()).isEqualTo("IMAGE");
		assertThat(firstConvoMsgs.get(3).getReceived()).isEqualTo(false);
		assertThat(firstConvoMsgs.get(3).getTimestamp()).isEqualTo(1544197647L);

		assertThat(firstConvoMsgs.get(8).getMessage()).isEqualTo("http://www.tumblr.com/somepost");
		assertThat(firstConvoMsgs.get(8).getType()).isEqualTo("POSTREF");
		assertThat(firstConvoMsgs.get(8).getReceived()).isEqualTo(false);
		assertThat(firstConvoMsgs.get(8).getTimestamp()).isEqualTo(1544221221L);

	}
}
