package org.nhindirect.smtpmq.gateway.streams;

import org.nhindirect.common.mail.SMTPMailMessage;
import org.nhindirect.common.mail.streams.SMTPMailMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

@EnableBinding(SmtpGatewayMessageOutput.class)
public class SmtpGatewayMessageSource
{	
	@Autowired
	@Qualifier(SmtpGatewayMessageOutput.SMTP_GATEWAY_MESSAGE_OUTPUT)
	private MessageChannel smtpGatewayChannel;
	
	@Output(SmtpGatewayMessageOutput.SMTP_GATEWAY_MESSAGE_OUTPUT)
	public <T> void forwardSMTPMessage(SMTPMailMessage msg) 
	{
		this.smtpGatewayChannel.send(SMTPMailMessageConverter.toStreamMessage(msg));
	}

}
