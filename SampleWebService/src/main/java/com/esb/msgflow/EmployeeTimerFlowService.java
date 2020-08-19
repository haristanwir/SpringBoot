package com.esb.msgflow;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.esb.core.EmployeeTimerWorker;
import com.esb.utility.Constant;
import com.esb.utility.ErrorHandling;
import com.esb.utility.FlowLogger;
import com.esb.utility.Utility;

@Service
public class EmployeeTimerFlowService {

	private static final Logger logger = FlowLogger.getLogger(EmployeeTimerFlowService.class.getName());
	private static final Logger Errorlogger = FlowLogger.getLogger(ErrorHandling.class.getName());

	private static String timerID = "timer_1";
	private static String QUEUE_NAME = "EMPLOYEE";
	private static Integer timeoutSec = 1;
	private static ScheduledExecutorService scheduler = null;
	private static Boolean isInitialized = false;
	private static Boolean startOnDeploy = false;

	public Boolean getIsInitialized() {
		return isInitialized;
	}

	public synchronized void setIsInitialized(Boolean isInitialized) {
		this.isInitialized = isInitialized;
	}

	public synchronized void initialize() {
		if (!isInitialized) {
			try {
				EmployeeTimerWorker.init(Utility.getProperty(Constant.RABBITMQ_HOST_NAME), Integer.parseInt(Utility.getProperty(Constant.RABBITMQ_PORT)));
				timeoutSec = Integer.parseInt(Utility.getProperty(Constant.EmployeeTimerFlow_FLOW_TIMEOUT_SEC));
			} catch (Exception ex) {
				logger.error(ex.getMessage());
				Errorlogger.error(ErrorHandling.getStackTrace(ex));
			}
			scheduler = Executors.newSingleThreadScheduledExecutor();
			EmployeeTimerWorker timerThread = new EmployeeTimerWorker(timerID, QUEUE_NAME);
			scheduler.scheduleAtFixedRate(timerThread, 0, timeoutSec, TimeUnit.SECONDS);
			isInitialized = true;
		}
	}

	public synchronized void delete() {
		if (isInitialized) {
			scheduler.shutdown();
			try {
				scheduler.awaitTermination(30, TimeUnit.MINUTES);
			} catch (Exception ex) {
			}
			isInitialized = false;
		}
	}

	@PostConstruct
	private void postConstruct() {
		try {
			startOnDeploy = Boolean.parseBoolean(Utility.getProperty(Constant.EmployeeTimerFlow_START_ON_DEPLOY));
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			Errorlogger.error(ErrorHandling.getStackTrace(ex));
		}
		if (startOnDeploy) {
			initialize();
		}
	}

	@PreDestroy
	private void preDestroy() {
		delete();
		EmployeeTimerWorker.shutdownWorker();
	}

}
