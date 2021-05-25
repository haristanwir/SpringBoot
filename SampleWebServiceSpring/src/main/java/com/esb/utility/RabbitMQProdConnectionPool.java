/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esb.utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 *
 * @author Haris Tanwir
 */
public class RabbitMQProdConnectionPool {

	private final Logger logger = LogManager.getLogger(RabbitMQProdConnectionPool.class.getName());
	private final Logger Errorlogger = LogManager.getLogger(ErrorHandling.class.getName());

	private String ip = null;
	private Integer port = null;
	private Integer poolSize = 0;
	private Long connectionWait = 50L;
	private String connectionName = null;
	private ConnectionFactory factory = null;
	private Connection connection = null;
	private List<Channel> channelPool = null;
	private ExecutorService executorService = Executors.newSingleThreadExecutor();
	private ConcurrentNavigableMap<Long, RabbitMQMessage> outstandingConfirms = new ConcurrentSkipListMap<>();

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

//	public RabbitMQProdConnectionPool(String ip, Integer port, String connectionName, Integer poolSize) {
//		this.ip = ip;
//		this.port = port;
//		if (poolSize > this.poolSize) {
//			this.poolSize = poolSize;
//		}
//		this.connectionName = connectionName + "(Publisher)";
//		this.channelPool = new ArrayList<>();
//		try {
//			factory = new ConnectionFactory();
//			factory.setHost(ip);
//			factory.setPort(port);
//			connection = factory.newConnection(this.connectionName);
//			for (int i = 0; i < poolSize; i++) {
//				channelPool.add(connection.createChannel());
//			}
//		} catch (Exception e) {
//			logger.error(e.getMessage());
//			Errorlogger.error(ErrorHandling.getStackTrace(e));
//		}
//	}

	private Channel getChannel() {
		Channel channel = null;
		if (!channelPool.isEmpty()) {
			synchronized (channelPool) {
				if (!channelPool.isEmpty()) {
					channel = channelPool.remove(0);
				} else {
					try {
						if (connection == null) {
							synchronized (this) {
								if (connection == null) {
									connection = factory.newConnection(this.connectionName);
								}
							}
						}
						channel = connection.createChannel();
						channel.confirmSelect();
						channel.addConfirmListener(new RabbitMQComfirmListener());
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
						synchronized (this) {
							if (connection == null) {
								connection = factory.newConnection(this.connectionName);
							}
						}
					}
					channel = connection.createChannel();
					channel.confirmSelect();
					channel.addConfirmListener(new RabbitMQComfirmListener());
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
										synchronized (this) {
											if (connection == null) {
												connection = factory.newConnection(this.connectionName);
											}
										}
									}
									channel = connection.createChannel();
									channel.confirmSelect();
									channel.addConfirmListener(new RabbitMQComfirmListener());
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

	@PreDestroy
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
		executorService.shutdown();
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
					synchronized (this) {
						if (connection == null) {
							connection = factory.newConnection(this.connectionName);
						}
					}
				}
				channel = connection.createChannel();
				channel.confirmSelect();
				channel.addConfirmListener(new RabbitMQComfirmListener());
			}
			if (channel.isOpen()) {
				channel.queueDeclare(queuename, true, false, false, null);
				outstandingConfirms.put(channel.getNextPublishSeqNo(), new RabbitMQMessage("", queuename, prop, message));
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
					synchronized (this) {
						if (connection == null) {
							connection = factory.newConnection(this.connectionName);
						}
					}
				}
				channel = connection.createChannel();
				channel.confirmSelect();
				channel.addConfirmListener(new RabbitMQComfirmListener());
			}
			if (channel.isOpen()) {
				outstandingConfirms.put(channel.getNextPublishSeqNo(), new RabbitMQMessage(exchange, routingKey, prop, message));
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

	private class RabbitMQComfirmListener implements ConfirmListener {

		private void confirm(long deliveryTag, boolean multiple) {
			if (multiple) {
				ConcurrentNavigableMap<Long, RabbitMQMessage> confirmed = outstandingConfirms.headMap(deliveryTag, true);
				confirmed.clear();
			} else {
				outstandingConfirms.remove(deliveryTag);
			}
		}

		@Override
		public void handleAck(long deliveryTag, boolean multiple) throws IOException {
			confirm(deliveryTag, multiple);
		}

		@Override
		public void handleNack(long deliveryTag, boolean multiple) throws IOException {
			if (multiple) {
				ConcurrentNavigableMap<Long, RabbitMQMessage> unConfirmed = outstandingConfirms.headMap(deliveryTag, true);
				for (Map.Entry<Long, RabbitMQMessage> _unConfirmed : unConfirmed.entrySet()) {
					executorService.execute(new Runnable() {
						public void run() {
							try {
								publish(_unConfirmed.getValue().getMessage(), _unConfirmed.getValue().getQueueName(), _unConfirmed.getValue().getExchange(), _unConfirmed.getValue().getProperties());
							} catch (Exception ex) {
								logger.error(ex.getMessage());
								Errorlogger.error(ErrorHandling.getStackTrace(ex));
							}
						}
					});
				}
			} else {
				RabbitMQMessage mqMessage = outstandingConfirms.get(deliveryTag);
				executorService.execute(new Runnable() {
					public void run() {
						try {
							publish(mqMessage.getMessage(), mqMessage.getQueueName(), mqMessage.getExchange(), mqMessage.getProperties());
						} catch (Exception ex) {
							logger.error(ex.getMessage());
							Errorlogger.error(ErrorHandling.getStackTrace(ex));
						}
					}
				});
			}
			confirm(deliveryTag, multiple);
		}
	}

}
