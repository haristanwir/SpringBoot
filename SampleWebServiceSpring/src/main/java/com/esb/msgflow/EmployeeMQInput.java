package com.esb.msgflow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.esb.utility.ActiveMQConnection;
import com.esb.utility.ActiveMQProdConnectionPool;
import com.esb.utility.ActiveMQQueueRetryThread;
import com.esb.utility.ErrorHandling;
import com.esb.utility.ThroughputController;

public class EmployeeMQInput {

	@Value("${activemq.username}")
	private String mq_username;

	@Value("${activemq.password}")
	private String mq_password;
	
	@Value("${activemq.ip}")
	private String mq_ip;

	@Value("${activemq.port}")
	private Integer mq_port;

	@Autowired
	private ActiveMQProdConnectionPool mqProducerPool;

	@Autowired
	private MessageProcessor messageProcesor;

	private final Logger logger = LogManager.getLogger(EmployeeMQInput.class.getName());
	private final Logger Errorlogger = LogManager.getLogger(ErrorHandling.class.getName());

	private ExecutorService retryExecutorService = Executors.newFixedThreadPool(1);
	private ExecutorService executorService = null;
	private String connectionUrl = null;
	private ActiveMQConnectionFactory factory = null;
	private ArrayList<ActiveMQConnection> consumerTagList = new ArrayList<ActiveMQConnection>();
	private String queueName = null;
	private String boqQueueName = null;
	private Integer threadPoolSize = null;
	private Integer threadPoolTPS = null;
	private ThroughputController tpsController = null;
	private Boolean isInitialized = false;

	public EmployeeMQInput(String queueName, Integer threadPoolSize, Integer tps) {
		this.queueName = queueName;
		this.boqQueueName = queueName + ".BOQ";
		this.threadPoolSize = (threadPoolSize < 1) ? 1 : threadPoolSize;
		this.threadPoolTPS = (tps < 0) ? 0 : tps;
	}

	public Boolean getIsInitialized() {
		return isInitialized;
	}

	public Integer getThreadPoolSize() {
		return threadPoolSize;
	}

	public void setThreadPoolSize(Integer threadPoolSize) {
		this.threadPoolSize = (threadPoolSize < 1) ? 1 : threadPoolSize;
	}

	public Integer getThreadPoolTPS() {
		return threadPoolTPS;
	}

	public void setThreadPoolTPS(Integer threadPoolTPS) {
		this.threadPoolTPS = (threadPoolTPS < 0) ? 0 : threadPoolTPS;
	}

	@PostConstruct
	public synchronized void init() throws IOException, JMSException {
		if (isInitialized) {
			return;
		}
		isInitialized = true;
		tpsController = new ThroughputController(threadPoolTPS);
		executorService = Executors.newFixedThreadPool(threadPoolSize);
		connectionUrl = "failover:" + "(" + "tcp://" + mq_ip + ":" + mq_port + ")" + "?jms.prefetchPolicy.all=" + threadPoolTPS;
		factory = new ActiveMQConnectionFactory(mq_username, mq_password, connectionUrl);
		for (int i = 0; i < threadPoolSize; i++) {
			Connection connection = factory.createConnection();
			Session session = connection.createSession(false, ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE);
			Destination destination = session.createQueue(queueName);
			MessageConsumer consumer = session.createConsumer(destination);
			consumer.setMessageListener(new ActiveMQConsumerCallback());
			consumerTagList.add(new ActiveMQConnection(connection, session, destination, consumer, null));
		}
	}

	@PreDestroy
	public synchronized void shutdown() {
		if (!isInitialized) {
			return;
		}
		isInitialized = false;
		for (ActiveMQConnection consumerTag : consumerTagList) {
			try {
				consumerTag.shutdown();
			} catch (Exception ex) {
			}
		}
		consumerTagList.clear();
		factory = null;
		executorService.shutdown();
	}

	private class ActiveMQConsumerCallback implements MessageListener  {
		@Override
		public void onMessage(Message message) {
			try {
				if (isInitialized) {
					executorService.execute(new MQWorker(message));
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage());
				Errorlogger.error(ErrorHandling.getStackTrace(ex));
			}
		}
	}

	private class MQWorker implements Runnable {
		private Message message;

		public MQWorker(Message message) {
			this.message = message;
		}

		@Override
		public void run() {
			if (isInitialized) {
				try {
					if (isInitialized) {
						if (message != null) {
							tpsController.evaluateTPS();
							TextMessage textMessage = (TextMessage) message;
							String message = textMessage.getText();
							logger.info("message dequeued:" + message);
							messageProcesor.processMessage(message);
							this.message.acknowledge();
						}
					}
				} catch (Exception ex) {
					logger.error(ex.getMessage());
					Errorlogger.error(ErrorHandling.getStackTrace(ex));
					try {
						mqProducerPool.enqueue(((TextMessage) message).getText(), boqQueueName, DeliveryMode.PERSISTENT, 4);
					} catch (Exception _ex) {
						logger.error(_ex.getMessage());
						Errorlogger.error(ErrorHandling.getStackTrace(_ex));
						try {
							retryExecutorService.execute(new ActiveMQQueueRetryThread(mqProducerPool, boqQueueName, ((TextMessage) message).getText(), DeliveryMode.PERSISTENT, 4, 10000l));
						} catch (JMSException e) {
						}
					}
					try {
						this.message.acknowledge();
					} catch (JMSException e) {
					}
				}
			}
		}
	}

}
