package org.nhindirect.smtpmq.gateway.streams;

import java.util.List;

import javax.mail.internet.InternetAddress;

import org.nhindirect.common.mail.SMTPMailMessage;
import org.nhindirect.common.mail.streams.SMTPMailMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;


@EnableBinding(SmtpGatewayMessageOutput.class)
public class SmtpGatewayMessageSource
{	
	private static final Logger LOGGER = LoggerFactory.getLogger(SmtpGatewayMessageSource.class);	
	
	@Autowired
	@Qualifier(SmtpGatewayMessageOutput.SMTP_GATEWAY_MESSAGE_OUTPUT)
	private MessageChannel smtpGatewayChannel;
	
	@Output(SmtpGatewayMessageOutput.SMTP_GATEWAY_MESSAGE_OUTPUT)
	public <T> void forwardSMTPMessage(SMTPMailMessage msg) throws Exception
	{
		final String from = (msg.getMailFrom() == null) ? null : msg.getMailFrom().toString();
		
		LOGGER.info("Handing off incoming message to smtp gateway for from {} to {} with message id {}", from, 
				toRecipsPrettingString(msg.getRecipientAddresses()), msg.getMimeMessage().getMessageID());
		
		this.smtpGatewayChannel.send(SMTPMailMessageConverter.toStreamMessage(msg));
	}

	protected String toRecipsPrettingString(List<InternetAddress> recips)
	{
		final String[] addrs = new String[recips.size()];
		
		int idx = 0;
		for (InternetAddress addr : recips)
			addrs[idx++] = addr.toString();
		
		return String.join(",", addrs);
	}
	
}
