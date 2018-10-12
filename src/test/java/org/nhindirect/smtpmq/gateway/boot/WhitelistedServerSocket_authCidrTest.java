package org.nhindirect.smtpmq.gateway.boot;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.subethamail.smtp.server.SMTPServer;


@WebAppConfiguration 
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SmtpGatewayApplication.class}) 
@Configuration
@TestPropertySource("classpath:properties/testLocalAuthorizedCIDRConfig.properties")
@DirtiesContext
public class WhitelistedServerSocket_authCidrTest
{
	@Autowired 
	protected SMTPServer smtpServer;
	
	@Before
	public void setUp()
	{ 
		if (!smtpServer.isRunning())
		{	
			smtpServer.start();
		}
	}	
	
	@Test
	public void testConnectionAuthorized_assertConnected() throws Exception
	{
		final Socket sock = new Socket("localhost", smtpServer.getPort());
		
		Thread.sleep(1000);
		
		// say hello
		final PrintWriter out =
		        new PrintWriter(sock.getOutputStream(), true);
		out.write("ehlo cerner.com");
		out.flush();
		
		/* attempts to read something from the socket
		 * a successful read indicates that we are authorized and that the test passes
		 */
	    final BufferedReader input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
	    System.out.println(input.read());

		sock.close();
	}
}
