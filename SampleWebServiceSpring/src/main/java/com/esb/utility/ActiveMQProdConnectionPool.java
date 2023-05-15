/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esb.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Haris Tanwir
 */
public class ActiveMQProdConnectionPool {

	private final Logger logger = LogManager.getLogger(ActiveMQProdConnectionPool.class.getName());
	private final Logger Errorlogger = LogManager.getLogger(ErrorHandling.class.getName());

	private String username = null;
	private String password = null;
	private String ip = null;
	private Integer port = null;
	private Integer poolSize = 0;
	private Long connectionWait = 50L;
	private String connectionUrl = null;
	private ActiveMQConnectionFactory factory = null;
	private Long connectionEvictionTimeout = (long) (1000 * 60 * 5);
	private ScheduledExecutorService schedulerService = Executors.newSingleThreadScheduledExecutor();
	private HashMap<String, ArrayList<ActiveMQConnection>> activeMQConnectionMap = null;

	public ActiveMQProdConnectionPool(String username, String password, String ip, Integer port) {
		this.username = username;
		this.password = password;
		this.ip = ip;
		this.port = port;
		this.connectionUrl = "tcp://" + ip + ":" + port + "?jms.prefetchPolicy.all=0";
		this.activeMQConnectionMap = new HashMap<String, ArrayList<ActiveMQConnection>>();
		factory = new ActiveMQConnectionFactory(username, password, connectionUrl);
		schedulerService.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					Set<String> queueNames = activeMQConnectionMap.keySet();
					for (String queueName : queueNames) {
						ArrayList<ActiveMQConnection> mqConnectionList = activeMQConnectionMap.get(queueName);
						if (!mqConnectionList.isEmpty()) {
							synchronized (mqConnectionList) {
								if (!mqConnectionList.isEmpty()) {
									ArrayList<ActiveMQConnection> invalidConnections = new ArrayList<ActiveMQConnection>();
									for (ActiveMQConnection innerConnection : mqConnectionList) {
										Long timestamp = innerConnection.getTimestamp();
										if (timestamp < System.currentTimeMillis() - connectionEvictionTimeout) {
											invalidConnections.add(innerConnection);
										}
									}
									if (!invalidConnections.isEmpty()) {
										int oldpoolsize = mqConnectionList.size();
										mqConnectionList.removeAll(invalidConnections);
										int newpoolsize = mqConnectionList.size();
										for (ActiveMQConnection innerConnection : invalidConnections) {
											shutdown(innerConnection);
										}
										if (newpoolsize < oldpoolsize) {
											logger.info("MQ Connections closed:" + invalidConnections.size());
										}
									}
								}
							}
						}
					}
				} catch (Exception ex) {
					logger.error(ErrorHandling.getMessage(ex));
					Errorlogger.error(ErrorHandling.getStackTrace(ex));
				}
			}
		}, 0, connectionEvictionTimeout + (1000 * 60), TimeUnit.MILLISECONDS);
	}

	private ActiveMQConnection getConnection(String queueName) {
		ActiveMQConnection mqConnection = null;
		ArrayList<ActiveMQConnection> mqConnectionList = activeMQConnectionMap.get(queueName);
		if (mqConnectionList == null) {
			mqConnectionList = new ArrayList<ActiveMQConnection>();
		}
		if (!mqConnectionList.isEmpty()) {
			synchronized (mqConnectionList) {
				if (!mqConnectionList.isEmpty()) {
					mqConnection = mqConnectionList.remove(0);
				} else {
					try {
						Connection connection = factory.createConnection();
						Session session = connection.createSession(false, ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE);
						Destination destination = session.createQueue(queueName);
						MessageProducer producer = session.createProducer(destination);
						mqConnection = new ActiveMQConnection(connection, session, destination, null, producer);
						activeMQConnectionMap.put(queueName, mqConnectionList);
					} catch (Exception ex) {
						logger.error(ErrorHandling.getMessage(ex));
						Errorlogger.error(ErrorHandling.getStackTrace(ex));
					}
				}
			}
		} else {
			if (poolSize == 0) {
				synchronized (mqConnectionList) {
					try {
						Connection connection = factory.createConnection();
						Session session = connection.createSession(false, ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE);
						Destination destination = session.createQueue(queueName);
						MessageProducer producer = session.createProducer(destination);
						mqConnection = new ActiveMQConnection(connection, session, destination, null, producer);
						activeMQConnectionMap.put(queueName, mqConnectionList);
					} catch (Exception ex) {
						logger.error(ErrorHandling.getMessage(ex));
						Errorlogger.error(ErrorHandling.getStackTrace(ex));
					}
				}
			} else {
				while (mqConnection == null) {
					try {
						Thread.sleep(connectionWait);
					} catch (InterruptedException ex) {
					}
					if (!mqConnectionList.isEmpty()) {
						synchronized (mqConnectionList) {
							if (!mqConnectionList.isEmpty()) {
								mqConnection = mqConnectionList.remove(0);
							} else {
								try {
									Connection connection = factory.createConnection();
									Session session = connection.createSession(false, ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE);
									Destination destination = session.createQueue(queueName);
									MessageProducer producer = session.createProducer(destination);
									mqConnection = new ActiveMQConnection(connection, session, destination, null, producer);
									activeMQConnectionMap.put(queueName, mqConnectionList);
								} catch (Exception ex) {
									logger.error(ErrorHandling.getMessage(ex));
									Errorlogger.error(ErrorHandling.getStackTrace(ex));
								}
							}
						}
					}
				}
			}
		}
		return mqConnection;
	}

	private ActiveMQConnection getTopicConnection(String topicName) {
		ActiveMQConnection mqConnection = null;
		ArrayList<ActiveMQConnection> mqConnectionList = activeMQConnectionMap.get(topicName);
		if (mqConnectionList == null) {
			mqConnectionList = new ArrayList<ActiveMQConnection>();
		}
		if (!mqConnectionList.isEmpty()) {
			synchronized (mqConnectionList) {
				if (!mqConnectionList.isEmpty()) {
					mqConnection = mqConnectionList.remove(0);
				} else {
					try {
						Connection connection = factory.createConnection();
						Session session = connection.createSession(false, ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE);
						Destination destination = session.createTopic(topicName);
						MessageProducer producer = session.createProducer(destination);
						mqConnection = new ActiveMQConnection(connection, session, destination, null, producer);
						activeMQConnectionMap.put(topicName, mqConnectionList);
					} catch (Exception ex) {
						logger.error(ErrorHandling.getMessage(ex));
						Errorlogger.error(ErrorHandling.getStackTrace(ex));
					}
				}
			}
		} else {
			if (poolSize == 0) {
				synchronized (mqConnectionList) {
					try {
						Connection connection = factory.createConnection();
						Session session = connection.createSession(false, ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE);
						Destination destination = session.createTopic(topicName);
						MessageProducer producer = session.createProducer(destination);
						mqConnection = new ActiveMQConnection(connection, session, destination, null, producer);
						activeMQConnectionMap.put(topicName, mqConnectionList);
					} catch (Exception ex) {
						logger.error(ErrorHandling.getMessage(ex));
						Errorlogger.error(ErrorHandling.getStackTrace(ex));
					}
				}
			} else {
				while (mqConnection == null) {
					try {
						Thread.sleep(connectionWait);
					} catch (InterruptedException ex) {
					}
					if (!mqConnectionList.isEmpty()) {
						synchronized (mqConnectionList) {
							if (!mqConnectionList.isEmpty()) {
								mqConnection = mqConnectionList.remove(0);
							} else {
								try {
									Connection connection = factory.createConnection();
									Session session = connection.createSession(false, ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE);
									Destination destination = session.createTopic(topicName);
									MessageProducer producer = session.createProducer(destination);
									mqConnection = new ActiveMQConnection(connection, session, destination, null, producer);
									activeMQConnectionMap.put(topicName, mqConnectionList);
								} catch (Exception ex) {
									logger.error(ErrorHandling.getMessage(ex));
									Errorlogger.error(ErrorHandling.getStackTrace(ex));
								}
							}
						}
					}
				}
			}
		}
		return mqConnection;
	}

	private boolean releaseConnection(String queueName, ActiveMQConnection connection) {
		ArrayList<ActiveMQConnection> mqConnectionList = activeMQConnectionMap.get(queueName);
		if (mqConnectionList != null) {
			synchronized (mqConnectionList) {
				connection.setTimestamp(System.currentTimeMillis());
				mqConnectionList.add(0, connection);
				return true;
			}
		} else {
			return false;
		}
	}

	public void shutdown() {
		Set<String> queueNames = activeMQConnectionMap.keySet();
		for (String queueName : queueNames) {
			ArrayList<ActiveMQConnection> mqConnectionList = activeMQConnectionMap.remove(queueName);
			synchronized (mqConnectionList) {
				for (ActiveMQConnection mqConnection : mqConnectionList) {
					try {
						MessageProducer producer = mqConnection.getProducer();
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
						MessageConsumer consumer = mqConnection.getConsumer();
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
						Session session = mqConnection.getSession();
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
						Connection connection = mqConnection.getConnection();
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
						Destination destination = mqConnection.getDestination();
						destination = null;
					} catch (Exception ex) {
					}
				}
				try {
					mqConnectionList.clear();
				} catch (Exception ex) {
				}
			}
		}
		try {
			activeMQConnectionMap.clear();
		} catch (Exception ex) {
		}
//		schedulerService.shutdownNow();
	}

	private void shutdown(ActiveMQConnection mqConnection) {
		try {
			MessageProducer producer = mqConnection.getProducer();
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
			MessageConsumer consumer = mqConnection.getConsumer();
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
			Session session = mqConnection.getSession();
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
			Connection connection = mqConnection.getConnection();
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
			Destination destination = mqConnection.getDestination();
			destination = null;
		} catch (Exception ex) {
		}
	}

	private boolean isConnected(ActiveMQConnection mqConnection) {
		boolean connected = true;
		if (mqConnection.getProducer() == null || mqConnection.getSession() == null || mqConnection.getConnection() == null) {
			connected = false;
		}
		return connected;
	}

	public Boolean enqueue(String message, String queueName, Integer persistance, Integer priority) throws Exception {
		ActiveMQConnection mqConnection = getConnection(queueName);
		try {
			while (!isConnected(mqConnection)) {
				Connection connection = factory.createConnection();
				Session session = connection.createSession(false, ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE);
				Destination destination = session.createQueue(queueName);
				MessageProducer producer = session.createProducer(destination);
				mqConnection = new ActiveMQConnection(connection, session, destination, null, producer);
			}
			TextMessage textMessage = mqConnection.getSession().createTextMessage();
			textMessage.setText(message);
			MessageProducer producer = mqConnection.getProducer();
			producer.send(textMessage, persistance, priority, 0);
			releaseConnection(queueName, mqConnection);
			return true;
		} catch (Exception ex) {
			shutdown(mqConnection);
			throw ex;
		}
	}

	public Boolean publish(String message, String topicName, Integer persistance, Integer priority) throws Exception {
		ActiveMQConnection mqConnection = getTopicConnection(topicName);
		try {
			while (!isConnected(mqConnection)) {
				Connection connection = factory.createConnection();
				Session session = connection.createSession(false, ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE);
				Destination destination = session.createTopic(topicName);
				MessageProducer producer = session.createProducer(destination);
				mqConnection = new ActiveMQConnection(connection, session, destination, null, producer);
			}
			TextMessage textMessage = mqConnection.getSession().createTextMessage();
			textMessage.setText(message);
			MessageProducer producer = mqConnection.getProducer();
			producer.send(textMessage, persistance, priority, 0);
			releaseConnection(topicName, mqConnection);
			return true;
		} catch (Exception ex) {
			shutdown(mqConnection);
			throw ex;
		}
	}

}