package com.esb.utility;

import com.rabbitmq.client.AMQP.BasicProperties;

import lombok.Data;

@Data
public class RabbitMQMessage {
	private String exchange;
	private String queueName;
	private BasicProperties properties;
	private String message;

	public RabbitMQMessage(String exchange, String queueName, BasicProperties properties, String message) {
		this.exchange = exchange;
		this.queueName = queueName;
		this.properties = properties;
		this.message = message;
	}
}
