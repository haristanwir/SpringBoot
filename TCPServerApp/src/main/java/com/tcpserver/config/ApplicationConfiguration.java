package com.tcpserver.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tcpserver.core.TCPServer;

@Configuration
public class ApplicationConfiguration {

	@Bean
	public TCPServer tcpServer(@Value("${tcpserver.port}") Integer port) throws IOException {
		return new TCPServer(port);
	}
}
