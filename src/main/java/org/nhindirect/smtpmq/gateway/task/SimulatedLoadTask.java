package org.nhindirect.smtpmq.gateway.task;

import java.util.Arrays;
import java.util.Calendar;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.nhindirect.common.mail.SMTPMailMessage;
import org.nhindirect.smtpmq.gateway.streams.SmtpGatewayMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@ConditionalOnProperty(name="direct.smtpmqgateway.loadgen.rate", matchIfMissing=false)
@Component
@Slf4j
public class SimulatedLoadTask
{
	@Value("${direct.smtpmqgateway.loadgen.sender}")
	protected String sender;
	
	@Value("${direct.smtpmqgateway.loadgen.recipient}")
	protected String recipient;	
	
	@Autowired
	protected SmtpGatewayMessageSource msgSource;
	
	@Scheduled(fixedRateString = "${direct.smtpmqgateway.loadgen.rate}", initialDelayString = "${direct.smtpmqgateway.loadgen.initialdelay:5000}")
	public void sendMessages() throws Exception 
	{
		final MimeMessage msg = new MimeMessage((Session)null);
		msg.setFrom(new InternetAddress(sender));
		msg.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
		msg.setSentDate(Calendar.getInstance().getTime());
		msg.setText("Load Test");
		
		SMTPMailMessage smptMsg = new SMTPMailMessage(msg, Arrays.asList(new InternetAddress(recipient)), new InternetAddress(sender));
		
		log.info("Sending generated load from " + sender + " to " + recipient);
		
		msgSource.forwardSMTPMessage(smptMsg);
	}
}
