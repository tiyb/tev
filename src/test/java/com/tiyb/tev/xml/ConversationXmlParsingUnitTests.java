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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import com.tiyb.tev.controller.TEVRestController;
import com.tiyb.tev.datamodel.Conversation;
import com.tiyb.tev.datamodel.ConversationMessage;
import com.tiyb.tev.datamodel.Metadata;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ConversationXmlParsingUnitTests {

	@Autowired
	private TEVRestController restController;
	
	@Before
	public void Setup() throws FileNotFoundException {
		Metadata md = restController.getMetadata();
		md.setMainTumblrUser("blogname");
		restController.updateMetadata(md);
		
		File rawXmlFile = ResourceUtils.getFile("classpath:XML/test-messages-xml.xml");
		InputStream xmlFile = new FileInputStream(rawXmlFile);
		
		restController.deleteAllConversations();
		restController.deleteAllConvoMsgs();
		ConversationXmlReader.parseDocument(xmlFile, restController);
	}
	
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
