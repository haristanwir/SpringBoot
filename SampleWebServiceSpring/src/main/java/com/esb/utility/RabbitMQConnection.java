package com.esb.utility;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.annotation.PreDestroy;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQConnection {

	private String ip = null;
	private Integer port = null;
	private ConnectionFactory factory = null;
	private Connection connection = null;

	public RabbitMQConnection(String ip, Integer port) throws IOException, TimeoutException {
		this.ip = ip;
		this.port = port;
		factory = new ConnectionFactory();
		factory.setHost(ip);
		factory.setPort(port);
		connection = factory.newConnection(this.getClass().getName());
	}

	public Channel getChannel() throws IOException {
		return connection.createChannel();
	}

	@PreDestroy
	public void shutdown() {
		try {
			synchronized (connection) {
				if (connection.isOpen()) {
					connection.close();
				}
			}
		} catch (Exception ex) {
		}
	}

}
