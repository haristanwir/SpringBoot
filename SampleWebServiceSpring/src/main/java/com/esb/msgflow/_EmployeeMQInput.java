package com.esb.msgflow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.esb.utility.ErrorHandling;
import com.esb.utility.RabbitMQConnection;
import com.esb.utility.RabbitMQProdConnectionPool;
import com.esb.utility.ThroughputController;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

public class _EmployeeMQInput {

	@Autowired
	private RabbitMQConnection mqConnection;

	@Autowired
	private RabbitMQProdConnectionPool mqProducerPool;

	@Autowired
	private MessageProcessor messageProcesor;

	private final Logger logger = LogManager.getLogger(_EmployeeMQInput.class.getName());
	private final Logger Errorlogger = LogManager.getLogger(ErrorHandling.class.getName());

	private ExecutorService executorService = null;
	private Channel channel = null;
	private ArrayList<String> consumerTagList = new ArrayList<String>();
	private String queueName = null;
	private String boqQueueName = null;
	private Integer threadPoolSize = null;
	private Integer threadPoolTPS = null;
	private ThroughputController tpsController = null;
	private Boolean isInitialized = false;

	public _EmployeeMQInput(String queueName, Integer threadPoolSize, Integer tps) {
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
	public synchronized void init() throws IOException {
		if (isInitialized) {
			return;
		}
		isInitialized = true;
		tpsController = new ThroughputController(threadPoolTPS);
		executorService = Executors.newFixedThreadPool(threadPoolSize);
		channel = mqConnection.getChannel();
		channel.basicQos(threadPoolTPS, true);
		channel.queueDeclare(queueName, true, false, false, null);
		for (int i = 0; i < threadPoolSize; i++) {
			consumerTagList.add(channel.basicConsume(queueName, false, new RabbitMQConsumerCallback()));
		}
	}

	@PreDestroy
	public synchronized void shutdown() {
		if (!isInitialized) {
			return;
		}
		isInitialized = false;
		for (String consumerTag : consumerTagList) {
			try {
				channel.basicCancel(consumerTag);
			} catch (Exception ex) {
			}
		}
		try {
			channel.close();
		} catch (Exception ex) {
		}
		consumerTagList.clear();
		channel = null;
		executorService.shutdown();
	}

	private class RabbitMQConsumerCallback implements Consumer {
		@Override
		public void handleConsumeOk(String consumerTag) {
		}

		@Override
		public void handleCancelOk(String consumerTag) {
		}

		@Override
		public void handleCancel(String consumerTag) throws IOException {
		}

		@Override
		public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] messageBody) throws IOException {
			try {
				if (isInitialized) {
					executorService.execute(new MQWorker(messageBody, envelope.getDeliveryTag()));
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage());
				Errorlogger.error(ErrorHandling.getStackTrace(ex));
			}
		}

		@Override
		public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
		}

		@Override
		public void handleRecoverOk(String consumerTag) {
		}
	}

	private class MQWorker implements Runnable {
		private byte[] messageBody = null;
		private long deliveryTag;

		public MQWorker(byte[] messageBody, long deliveryTag) {
			this.messageBody = messageBody;
			this.deliveryTag = deliveryTag;
		}

		@Override
		public void run() {
			if (isInitialized) {
				try {
					if (isInitialized) {
						if (messageBody != null && messageBody.length > 0) {
							tpsController.evaluateTPS();
							String message = new String(messageBody, "UTF-8");
							logger.info("message dequeued:" + message);
							messageProcesor.processMessage(message);
							channel.basicAck(deliveryTag, false);
						}
					}
				} catch (Exception ex) {
					logger.error(ex.getMessage());
					Errorlogger.error(ErrorHandling.getStackTrace(ex));
					try {
						mqProducerPool.enqueue(new String(messageBody, "UTF-8"), boqQueueName, null);
					} catch (Exception _ex) {
						logger.error(_ex.getMessage());
						Errorlogger.error(ErrorHandling.getStackTrace(_ex));
					}
					try {
						channel.basicAck(deliveryTag, false);
					} catch (IOException e) {
					}
				}
			}
		}
	}

}
