/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esb.utility;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 *
 * @author Haris Tanwir
 */
public class RabbitMQProdConnectionPool {

	private static final Logger logger = FlowLogger.getLogger(RabbitMQProdConnectionPool.class.getName());
	private static final Logger Errorlogger = FlowLogger.getLogger(ErrorHandling.class.getName());

	private String ip = null;
	private Integer port = null;
	private Integer poolSize = 0;
	private Long connectionWait = 50L;
	private String connectionName = null;
	private ConnectionFactory factory = null;
	private Connection connection = null;
	private List<Channel> channelPool = null;

	public RabbitMQProdConnectionPool(String ip, Integer port, String connectionName) {
		this.ip = ip;
		this.port = port;
		this.connectionName = connectionName + "(Publisher)";
		this.channelPool = new ArrayList<>();
		try {
			factory = new ConnectionFactory();
			factory.setHost(ip);
			factory.setPort(port);
			connection = factory.newConnection(this.connectionName);
		} catch (Exception e) {
			logger.error(e.getMessage());
			Errorlogger.error(ErrorHandling.getStackTrace(e));
		}
	}

	public RabbitMQProdConnectionPool(String ip, Integer port, String connectionName, Integer poolSize) {
		this.ip = ip;
		this.port = port;
		if (poolSize > this.poolSize) {
			this.poolSize = poolSize;
		}
		this.connectionName = connectionName + "(Publisher)";
		this.channelPool = new ArrayList<>();
		try {
			factory = new ConnectionFactory();
			factory.setHost(ip);
			factory.setPort(port);
			connection = factory.newConnection(this.connectionName);
			for (int i = 0; i < poolSize; i++) {
				channelPool.add(connection.createChannel());
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			Errorlogger.error(ErrorHandling.getStackTrace(e));
		}
	}

	private Channel getChannel() {
		Channel channel = null;
		if (!channelPool.isEmpty()) {
			synchronized (channelPool) {
				if (!channelPool.isEmpty()) {
					channel = channelPool.remove(0);
				} else {
					try {
						if (connection == null) {
							connection = factory.newConnection(this.connectionName);
						}
						channel = connection.createChannel();
					} catch (Exception ex) {
						logger.error(ex.getMessage());
						Errorlogger.error(ErrorHandling.getStackTrace(ex));
					}
				}
			}
		} else {
			if (poolSize == 0) {
				try {
					if (connection == null) {
						connection = factory.newConnection(this.connectionName);
					}
					channel = connection.createChannel();
				} catch (Exception ex) {
					logger.error(ex.getMessage());
					Errorlogger.error(ErrorHandling.getStackTrace(ex));
				}
			} else {
				while (channel == null) {
					try {
						Thread.sleep(connectionWait);
					} catch (InterruptedException ex) {
					}
					if (!channelPool.isEmpty()) {
						synchronized (channelPool) {
							if (!channelPool.isEmpty()) {
								channel = channelPool.remove(0);
							} else {
								try {
									if (connection == null) {
										connection = factory.newConnection(this.connectionName);
									}
									channel = connection.createChannel();
								} catch (Exception ex) {
									logger.error(ex.getMessage());
									Errorlogger.error(ErrorHandling.getStackTrace(ex));
								}
							}
						}
					}
				}
			}
		}
		return channel;
	}

	private boolean releaseChannel(Channel channel) {
		synchronized (channelPool) {
			return channelPool.add(channel);
		}
	}

	public void shutdown() {
		synchronized (channelPool) {
			for (Channel channel : channelPool) {
				try {
					channel.close();
				} catch (Exception ex) {
				}
				channel = null;
			}
			try {
				channelPool.clear();
			} catch (Exception ex) {
			}
		}
		try {
			synchronized (connection) {
				if (connection.isOpen()) {
					connection.close();
				}
			}
		} catch (Exception ex) {
		}
	}

	private void shutdown(Channel channel) {
		try {
			channel.close();
		} catch (Exception ex) {
		}
		channel = null;
	}

	private Boolean isConnected(Channel channel) {
		Boolean connected = true;
		if (connection == null) {
			connected = null;
		} else if (channel == null) {
			connected = false;
		} else if (!connection.isOpen()) {
			connected = null;
		} else if (!channel.isOpen()) {
			connected = false;
		}
		return connected;
	}

	public Boolean enqueue(String message, String queuename, BasicProperties prop) throws Exception {
		Channel channel = getChannel();
		try {
			while (!isConnected(channel)) {
				if (connection == null) {
					connection = factory.newConnection(this.connectionName);
				}
				channel = connection.createChannel();
			}
			if (channel.isOpen()) {
				channel.queueDeclare(queuename, true, false, false, null);
				channel.basicPublish("", queuename, prop, message.getBytes("UTF-8"));
				releaseChannel(channel);
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			if (poolSize > 0) {
				releaseChannel(channel);
			} else {
				shutdown(channel);
			}
			throw ex;
		}
	}

	public Boolean publish(String message, String routingKey, String exchange, BasicProperties prop) throws Exception {
		Channel channel = getChannel();
		try {
			while (!isConnected(channel)) {
				if (connection == null) {
					connection = factory.newConnection(this.connectionName);
				}
				channel = connection.createChannel();
			}
			if (channel.isOpen()) {
				channel.basicPublish(exchange, routingKey, prop, message.getBytes("UTF-8"));
				releaseChannel(channel);
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			if (poolSize > 0) {
				releaseChannel(channel);
			} else {
				shutdown(channel);
			}
			throw ex;
		}
	}

}
