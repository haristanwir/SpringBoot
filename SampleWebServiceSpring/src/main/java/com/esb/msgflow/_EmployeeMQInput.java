package com.esb.msgflow;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.esb.utility.ErrorHandling;
import com.esb.utility.RabbitMQConsConnectionPool;
import com.esb.utility.RabbitMQProdConnectionPool;
import com.esb.utility.ThroughputController;
import com.rabbitmq.client.GetResponse;

public class _EmployeeMQInput {

	@Autowired
	private RabbitMQConsConnectionPool mqConsumerPool;

	@Autowired
	private RabbitMQProdConnectionPool mqProducerPool;

	@Autowired
	private MessageProcessor messageProcesor;

	private final Logger logger = LogManager.getLogger(_EmployeeMQInput.class.getName());
	private final Logger Errorlogger = LogManager.getLogger(ErrorHandling.class.getName());

	private ExecutorService executorService = null;
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
	public synchronized void init() {
		if (isInitialized) {
			return;
		}
		isInitialized = true;
		tpsController = new ThroughputController(threadPoolTPS);
		executorService = Executors.newFixedThreadPool(threadPoolSize);
		for (int i = 0; i < threadPoolSize; i++) {
			executorService.execute(new MQWorker());
		}
	}

	@PreDestroy
	public synchronized void shutdown() {
		if (!isInitialized) {
			return;
		}
		isInitialized = false;
		executorService.shutdown();
	}

	private class MQWorker implements Runnable {
		private String message = null;

		@Override
		public void run() {
			while (isInitialized) {
				try {
					tpsController.evaluateTPS();
					message = null;
					if (isInitialized) {
						GetResponse mqMessage = mqConsumerPool.dequeue(queueName);
						if (mqMessage != null) {
							message = new String(mqMessage.getBody(), "UTF-8");
							logger.info("message dequeued:" + message);
							messageProcesor.processMessage(message);
						}
					}
				} catch (Exception ex) {
					logger.error(ex.getMessage());
					Errorlogger.error(ErrorHandling.getStackTrace(ex));
					try {
						mqProducerPool.enqueue(message, boqQueueName, null);
					} catch (Exception _ex) {
						logger.error(_ex.getMessage());
						Errorlogger.error(ErrorHandling.getStackTrace(_ex));
					}
				}
			}
		}
	}

}
