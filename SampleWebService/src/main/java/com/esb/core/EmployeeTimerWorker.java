package com.esb.core;

import java.util.Date;

import org.apache.logging.log4j.Logger;

import com.esb.utility.ErrorHandling;
import com.esb.utility.FlowLogger;
import com.esb.utility.RabbitMQProdConnectionPool;
import com.rabbitmq.client.AMQP.BasicProperties;

public class EmployeeTimerWorker extends Thread {

	private static final Logger logger = FlowLogger.getLogger(EmployeeTimerWorker.class.getName());
	private static final Logger Errorlogger = FlowLogger.getLogger(ErrorHandling.class.getName());

	private String timerID = null;
	private String queue = null;
	private static RabbitMQProdConnectionPool mqProducerPool = null;
	// private static ActiveMQProdConnectionPool mqProducerPool = null;
	// private static IBMMQProdConnectionPool mqProducerPool = null;

	public static void init(String mq_ip, Integer mq_port) {
		try {
			EmployeeTimerWorker.mqProducerPool.shutdown();
		} catch (Exception ex) {
		}
		mqProducerPool = new RabbitMQProdConnectionPool(mq_ip, mq_port, EmployeeTimerWorker.class.getName());
		// mqProducerPool = new ActiveMQProdConnectionPool("localhost", 61616);
		// mqProducerPool = new IBMMQProdConnectionPool("localhost", 1415, "IIB10QMGR", "SYSTEM.ADMIN.SVRCONN");
	}

	public EmployeeTimerWorker(String timerID, String queue) {
		this.timerID = timerID;
		this.queue = queue;
	}

	public synchronized static void shutdownWorker() {
		try {
			if (EmployeeTimerWorker.mqProducerPool != null) {
				EmployeeTimerWorker.mqProducerPool.shutdown();
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			Errorlogger.error(ErrorHandling.getStackTrace(ex));
		}
	}

	@Override
	public void run() {
		String message = System.currentTimeMillis() + "";
		BasicProperties prop = new BasicProperties.Builder().appId(this.getClass().getName()).clusterId(null).contentEncoding("UTF-8").contentType("application/json").correlationId(null).deliveryMode(null).expiration(null).headers(null).messageId(null).priority((int) (System.currentTimeMillis() % 3)).replyTo(null).timestamp(new Date()).type(null).userId(null).build();
		try {
			long start = System.currentTimeMillis();
			logger.info("Timer recieved timerID: " + timerID + ", queue:" + queue + ", message:" + message);
			EmployeeTimerWorker.mqProducerPool.enqueue(message, queue, prop);
			// EmployeeTimerWorker.mqProducerPool.enqueue(message, queue, (int)(System.currentTimeMillis()%3));
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			Errorlogger.error(ErrorHandling.getStackTrace(ex));
		}
	}

}
