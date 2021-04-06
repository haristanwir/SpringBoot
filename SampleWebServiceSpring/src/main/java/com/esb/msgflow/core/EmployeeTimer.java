package com.esb.msgflow.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.esb.utility.ErrorHandling;
import com.esb.utility.RabbitMQConsConnectionPool;
import com.esb.utility.RabbitMQProdConnectionPool;

public class EmployeeTimer {

	@Autowired
	private RabbitMQConsConnectionPool mqConsumerPool;

	@Autowired
	private RabbitMQProdConnectionPool mqProducerPool;

	private final Logger logger = LogManager.getLogger(EmployeeMQInput.class.getName());
	private final Logger Errorlogger = LogManager.getLogger(ErrorHandling.class.getName());

	private ScheduledExecutorService scheduler = null;
	private String timerID = null;
	private Integer timeoutSec = null;
	private Boolean isInitialized = false;

	public EmployeeTimer(String timerID, Integer timeoutSec) {
		this.timerID = timerID;
		this.timeoutSec = timeoutSec;
	}

	@PostConstruct
	public void init() {
		isInitialized = true;
		scheduler = Executors.newSingleThreadScheduledExecutor();
		TimerWorker timerThread = new TimerWorker(timerID);
		scheduler.scheduleWithFixedDelay(timerThread, 0, timeoutSec, TimeUnit.SECONDS);
	}

	@PreDestroy
	public void shutdown() {
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
				mqProducerPool.enqueue(message, "EMPLOYEE", null);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
				Errorlogger.error(ErrorHandling.getStackTrace(ex));
			}
		}

	}

}
