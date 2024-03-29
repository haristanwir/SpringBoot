package com.esb.msgflow;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.esb.utility.ErrorHandling;
import com.esb.utility.RabbitMQProdConnectionPool;

public class _EmployeeTimer {

	@Autowired
	private RabbitMQProdConnectionPool mqProducerPool;

	private final Logger logger = LogManager.getLogger(EmployeeMQInput.class.getName());
	private final Logger Errorlogger = LogManager.getLogger(ErrorHandling.class.getName());

	private ScheduledExecutorService scheduler = null;
	private String queueName = null;
	private String timerID = null;
	private Integer timeoutSec = null;
	private Boolean isInitialized = false;

	public _EmployeeTimer(String queueName, String timerID, Integer timeoutSec) {
		this.queueName = queueName;
		this.timerID = timerID;
		this.timeoutSec = timeoutSec;
	}

	public Boolean getIsInitialized() {
		return isInitialized;
	}

	@PostConstruct
	public synchronized void init() {
		if (isInitialized) {
			return;
		}
		scheduler = Executors.newSingleThreadScheduledExecutor();
		TimerWorker timerThread = new TimerWorker(timerID);
		scheduler.scheduleWithFixedDelay(timerThread, 0, timeoutSec, TimeUnit.SECONDS);
		isInitialized = true;
	}

	@PreDestroy
	public synchronized void shutdown() {
		if (!isInitialized) {
			return;
		}
		scheduler.shutdown();
		isInitialized = false;
	}

	private class TimerWorker implements Runnable {
		private String timerID = null;

		private TimerWorker(String timerID) {
			this.timerID = timerID;
		}

		@Override
		public void run() {
			try {
				String message = "Timer called: " + timerID + "_" + System.currentTimeMillis();
				logger.info(message);
				mqProducerPool.enqueue(message, queueName, null);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
				Errorlogger.error(ErrorHandling.getStackTrace(ex));
			}
		}
	}

}
