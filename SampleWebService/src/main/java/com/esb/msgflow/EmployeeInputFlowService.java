package com.esb.msgflow;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.esb.core.EmployeeInputWorker;
import com.esb.utility.Constant;
import com.esb.utility.ErrorHandling;
import com.esb.utility.FlowLogger;
import com.esb.utility.Utility;

@Service
public class EmployeeInputFlowService {

	private static final Logger logger = FlowLogger.getLogger(EmployeeInputWorker.class.getName());
	private static final Logger Errorlogger = FlowLogger.getLogger(ErrorHandling.class.getName());

	private String QUEUE_NAME = "EMPLOYEE";
	private String QUEUE_NAME_BOQ = QUEUE_NAME + ".BOQ";
	private Integer threadPoolSize = 1;
	private Integer threadPoolTPS = 1;
	private ExecutorService execService = null;
	private Boolean isInitialized = false;
	private Boolean startOnDeploy = false;

	public Integer getThreadPoolSize() {
		return threadPoolSize;
	}

	public synchronized void setThreadPoolSize(Integer threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	public Integer getThreadPoolTPS() {
		return threadPoolTPS;
	}

	public synchronized void setThreadPoolTPS(Integer threadPoolTPS) {
		this.threadPoolTPS = threadPoolTPS;
	}

	public Boolean getIsInitialized() {
		return isInitialized;
	}

	public synchronized void setIsInitialized(Boolean isInitialized) {
		this.isInitialized = isInitialized;
	}

	public synchronized void initialize() {
		if (!isInitialized) {
			try {
				EmployeeInputWorker.init(threadPoolTPS, Utility.getProperty(Constant.JDBC_DRIVER), Utility.getProperty(Constant.JDBC_STRING), Utility.getProperty(Constant.JDBC_USER_NAME), Utility.getProperty(Constant.JDBC_PASSWORD), Utility.getProperty(Constant.RABBITMQ_HOST_NAME), Integer.parseInt(Utility.getProperty(Constant.RABBITMQ_PORT)));
				// EmployeeInputWorker.init(Integer.parseInt(Utility.getProperty(Constant.EmployeeInputFlow_FLOW_TPS)),
				// Utility.getProperty(Constant.JDBC_DRIVER),
				// Utility.getProperty(Constant.JDBC_STRING),
				// Utility.getProperty(Constant.JDBC_USER_NAME),
				// Utility.getProperty(Constant.JDBC_PASSWORD),
				// Utility.getProperty(Constant.RABBITMQ_HOST_NAME),
				// Integer.parseInt(Utility.getProperty(Constant.RABBITMQ_PORT)));
				// threadPoolSize =
				// Integer.parseInt(Utility.getProperty(Constant.EmployeeInputFlow_POOL_SIZE));//
			} catch (Exception ex) {
				logger.error(ex.getMessage());
				Errorlogger.error(ErrorHandling.getStackTrace(ex));
			}
			EmployeeInputWorker.setIsRunning(true);
			execService = Executors.newFixedThreadPool(threadPoolSize);
			for (int i = 0; i < threadPoolSize; i++) {
				EmployeeInputWorker mqThread = new EmployeeInputWorker(QUEUE_NAME, QUEUE_NAME_BOQ);
				execService.execute(mqThread);
				try {
					Thread.sleep(500);
				} catch (InterruptedException ex) {
				}
			}
			isInitialized = true;
		}
	}

	public synchronized void delete() {
		if (isInitialized) {
			EmployeeInputWorker.setIsRunning(false);
			execService.shutdown();
			try {
				execService.awaitTermination(30, TimeUnit.MINUTES);
			} catch (Exception ex) {
			}
			isInitialized = false;
		}
	}

	@PostConstruct
	private void postConstruct() {
		try {
			startOnDeploy = Boolean.parseBoolean(Utility.getProperty(Constant.EmployeeInputFlow_START_ON_DEPLOY));
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
		EmployeeInputWorker.shutdownWorker();
	}

}
