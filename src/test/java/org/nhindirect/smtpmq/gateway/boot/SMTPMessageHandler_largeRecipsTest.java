package org.nhindirect.smtpmq.gateway.boot;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
public class SMTPMessageHandler_largeRecipsTest
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
	public void testMaxRecipientsExceeded_assertRecipientsLimited() throws Exception
	{
        final String sender = "sender@localhost";

        	
        final String body = "Subject: test\r\n\r\nTestmail";
        final SMTPClient client = new SMTPClient();
        client.connect("localhost", 1025);
        client.helo("localhost");
        client.setSender(sender);
        for (int i = 0; i < 550; ++i)
        	client.addRecipient("rcpt" + i + "@localhost.com");
        
        assertTrue(client.sendShortMessageData(body));
        client.quit();
        client.disconnect();
        
        //final Address[] recips = postProc.getRecipientAddresses().get(RabbitConfig.MAIL_RECIPIENTS_HEADER);
        BlockingQueue<Message<?>> messages  = collector.forChannel(source.txOutput());
        
        Message<?> msg = messages.poll();
        
        final SMTPMailMessage smtpMailMessage = SMTPMailMessageConverter.fromStreamMessage(msg);
        
        final List<InternetAddress> recips = smtpMailMessage.getRecipientAddresses();
        assertEquals(550, recips.size());
	}
}
