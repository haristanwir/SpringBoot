/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esb.utility;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 *
 * @author Haris Tanwir
 */
public class ActiveMQConnection {

	private Connection connection = null;
	private Session session = null;
	private Destination destination = null;
	private MessageConsumer consumer = null;
	private MessageProducer producer = null;
	private Long timestamp = null;

	public ActiveMQConnection(Connection connection, Session session, Destination destination, MessageConsumer consumer, MessageProducer producer) throws JMSException {
		this.connection = connection;
		this.session = session;
		this.destination = destination;
		this.consumer = consumer;
		this.producer = producer;
		if (this.producer == null) {
			this.connection.start();
		}
		this.timestamp = System.currentTimeMillis();
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Destination getDestination() {
		return destination;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	public MessageConsumer getConsumer() {
		return consumer;
	}

	public void setConsumer(MessageConsumer consumer) {
		this.consumer = consumer;
	}

	public MessageProducer getProducer() {
		return producer;
	}

	public void setProducer(MessageProducer producer) {
		this.producer = producer;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	
	public void shutdown() {
		try {
			if (producer != null) {
				try {
					producer.close();
				} catch (Exception ex) {
				}
				producer = null;
			}
		} catch (Exception ex) {
		}
		try {
			if (consumer != null) {
				try {
					consumer.close();
				} catch (Exception ex) {
				}
				consumer = null;
			}
		} catch (Exception ex) {
		}
		try {
			if (session != null) {
				try {
					session.close();
				} catch (Exception ex) {
				}
				session = null;
			}
		} catch (Exception ex) {
		}
		try {
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception ex) {
				}
				connection = null;
			}
		} catch (Exception ex) {
		}
		try {
			destination = null;
		} catch (Exception ex) {
		}
	}

}