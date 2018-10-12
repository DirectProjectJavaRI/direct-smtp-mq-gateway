package org.nhindirect.smtpmq.gateway.boot;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.smtp.SMTPClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nhindirect.common.mail.SMTPMailMessage;
import org.nhindirect.common.mail.streams.SMTPMailMessageConverter;
import org.nhindirect.smtpmq.gateway.streams.SmtpGatewayMessageOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.subethamail.smtp.server.SMTPServer;

@WebAppConfiguration 
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SmtpGatewayApplication.class}) 
@Configuration
@TestPropertySource("classpath:properties/testConfig.properties")
@DirtiesContext
public class SMTPMessageHandler_sendMessageTest
{
    protected static MimeMessage sentMessage;
    
	@Autowired 
	protected SMTPServer smtpServer;
	
	@Autowired
	protected SmtpGatewayMessageOutput source;
	
	@Autowired
	private MessageCollector collector;
	
	@Before
	public void setUp()
	{ 
		sentMessage = null;
		if (!smtpServer.isRunning())
		{	
			smtpServer.start();
		}
	}
		
	
	@Test
	public void testGoodMessage_assertMessageSent() throws Exception
	{
        final String sender = "sender@localhost";

        	
        final String data = "To: rcpt@localhost.com\r\nSubject: test\r\n\r\nTestmail";
        final SMTPClient client = new SMTPClient();
        client.connect("localhost", 1025);
        client.helo("localhost");
        client.setSender(sender);
        for (int i = 0; i < 4; ++i)
        	client.addRecipient("rcpt" + i + "@localhost.com");
        
        assertTrue(client.sendShortMessageData(data));
        client.quit();
        client.disconnect();
        
        BlockingQueue<Message<?>> messages  = collector.forChannel(source.txOutput());
        
        Message<?> msg = messages.poll();
        
        final SMTPMailMessage smtpMailMessage = SMTPMailMessageConverter.fromStreamMessage(msg);
        
        sentMessage = smtpMailMessage.getMimeMessage();
        
        assertEquals("test", sentMessage.getSubject());
        assertEquals("rcpt@localhost.com", sentMessage.getHeader("To")[0]);
        final String content = IOUtils.toString(sentMessage.getInputStream(), Charset.defaultCharset());
        assertEquals("Testmail\r\n", content);
        
        final List<InternetAddress> recips = smtpMailMessage.getRecipientAddresses();
        
        assertEquals(4, recips.size());
	}
	
	@Test
	public void testGoodMessageEmptyFrom_assertMessageSent() throws Exception
	{
        final String sender = "";

        	
        final String data = "To: rcpt@localhost.com\r\nSubject: test\r\n\r\nTestmail";
        final SMTPClient client = new SMTPClient();
        client.connect("localhost", 1025);
        client.helo("localhost");
        client.setSender(sender);
        for (int i = 0; i < 4; ++i)
        	client.addRecipient("rcpt" + i + "@localhost.com");
        
        assertTrue(client.sendShortMessageData(data));
        client.quit();
        client.disconnect();
        
        BlockingQueue<Message<?>> messages  = collector.forChannel(source.txOutput());
        
        Message<?> msg = messages.poll();
        
        final SMTPMailMessage smtpMailMessage = SMTPMailMessageConverter.fromStreamMessage(msg);
        
        assertNull(smtpMailMessage.getMailFrom());
        sentMessage = smtpMailMessage.getMimeMessage();
        
        assertEquals("test", sentMessage.getSubject());
        assertEquals("rcpt@localhost.com", sentMessage.getHeader("To")[0]);
        final String content = IOUtils.toString(sentMessage.getInputStream(), Charset.defaultCharset());
        assertEquals("Testmail\r\n", content);
        
        final List<InternetAddress> recips = smtpMailMessage.getRecipientAddresses();
        
        assertEquals(4, recips.size());
	}	
	
	@Test
	public void testGoodInvalidFromMessage_assertMessageNotSent() throws Exception
	{
        final String sender = "bl#@#$#.Localhost.com";

        	
        final String data = "To: rcpt@localhost.com\r\nSubject: test\r\n\r\nTestmail";
        final SMTPClient client = new SMTPClient();
        client.connect("localhost", 1025);
        client.helo("localhost");
        client.setSender(sender);
        for (int i = 0; i < 4; ++i)
        	client.addRecipient("rcpt" + i + "@localhost.com");
        
        assertFalse(client.sendShortMessageData(data));
        client.quit();
        client.disconnect();
        
 	}	

}
