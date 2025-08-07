package org.nhindirect.smtpmq.gateway.autoconfig;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.List;

import org.nhindirect.smtpmq.gateway.server.GetMessageHeaderStream;
import org.nhindirect.smtpmq.gateway.server.SMTPMessageHandler;
import org.nhindirect.smtpmq.gateway.server.SizeLimitedInputStreamFactory;
import org.nhindirect.smtpmq.gateway.server.SizeLimitedStreamCreator;
import org.nhindirect.smtpmq.gateway.server.WhitelistedServerSocket;
import org.nhindirect.smtpmq.gateway.streams.SmtpGatewayMessageSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.smtp.server.ServerSocketCreator;

@AutoConfiguration
public class SMTPServerBeanAutionConfiguration
{	
	
	@Value("${direct.smtpmqgateway.binding.port:1025}")
	public int port;
	
	@Value("${direct.smtpmqgateway.binding.host:0.0.0.0}")
	public String host;	

	@Value("${direct.smtpmqgateway.mq.exchange:}")
	private String exchange;
	
	@Value("${direct.smtpmqgateway.message.maxHeaderSize:262144}")
	private int maxHeaderSize;			
	 
	@Value("${direct.smtpmqgateway.message.maxMessageSize:39845888}")
	private int maxMessageSize;			
	
	
	@Value("${direct.smtpmqgateway.clientwhitelist.cidr:}")
	private List<String> clientWhitelistCidrs;	
	
	@ConditionalOnMissingBean
	@Bean(destroyMethod = "stop")
    SMTPServer smtpServer(SMTPMessageHandler smtpMessageHandler) throws Exception
    {
		
		SMTPServer.Builder builder = new SMTPServer.Builder();
		builder.messageHandlerFactory(new MessageHandlerFactory() 
	    {
			@Override
			public MessageHandler create(MessageContext ctx) 
			{
				return smtpMessageHandler;		
			}
	        
        });
		builder.port(port);
		builder.bindAddress(InetAddress.getByName(host));
		builder.softwareName("DirectProject Java RI SMTP To MQ Gateway");
		builder.maxMessageSize(maxMessageSize);
		
		
		builder.serverSocketFactory(new ServerSocketCreator() {
			
			@Override
			public ServerSocket createServerSocket() throws IOException {
				
				if (clientWhitelistCidrs.isEmpty() || 
						(clientWhitelistCidrs.size() == 1 && !StringUtils.hasText(clientWhitelistCidrs.get(0))))
					return new ServerSocket();

				return new WhitelistedServerSocket(clientWhitelistCidrs);				
			}
			
		});
		
	
		
		
		return builder.build();
		
		
    }
	
	@ConditionalOnMissingBean
	@Bean
	SMTPMessageHandler smtpMessageHandler(SmtpGatewayMessageSource messageSourceQueue)
	{	
		final SizeLimitedStreamCreator sizeCreator = new SizeLimitedStreamCreator(maxMessageSize,
				SizeLimitedInputStreamFactory.getInstance());	
		
		return new SMTPMessageHandler(messageSourceQueue, new GetMessageHeaderStream(maxHeaderSize), sizeCreator);
	}	
	
	@ConditionalOnMissingBean
	@Bean
	SmtpGatewayMessageSource smtpGatewayMessageSource() {
		
		return new SmtpGatewayMessageSource();
	}
}
