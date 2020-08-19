package com.esb.service.rest;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.esb.model.Employee;
import com.esb.model.ServiceResponse;
import com.esb.utility.Constant;
import com.esb.utility.ErrorHandling;
import com.esb.utility.FlowLogger;
import com.esb.utility.RabbitMQProdConnectionPool;
import com.esb.utility.Utility;

@Service
public class EmployeeService {

	private static final Logger logger = FlowLogger.getLogger(EmployeeService.class.getName());
	private static final Logger Errorlogger = FlowLogger.getLogger(ErrorHandling.class.getName());

	private RabbitMQProdConnectionPool mqProducerPool = null;
	// private static ActiveMQProdConnectionPool mqProducerPool = null;
	// private static IBMMQProdConnectionPool mqProducerPool = null;

	public synchronized void initialize(String mq_ip, Integer mq_port) {
		try {
			mqProducerPool.shutdown();
		} catch (Exception ex) {
		}
		mqProducerPool = new RabbitMQProdConnectionPool(mq_ip, mq_port, EmployeeService.class.getName());
		// mqProducerPool = new ActiveMQProdConnectionPool("localhost", 61616);
		// mqProducerPool = new IBMMQProdConnectionPool("localhost", 1415, "IIB10QMGR", "SYSTEM.ADMIN.SVRCONN");
	}

	public Employee getEmployeeByName(String name) {
		String uri = "http://localhost:9090/rest/employee/id/12";
		RestTemplate restTemplate = new RestTemplate();
		JSONObject result = restTemplate.getForObject(uri, JSONObject.class);
		System.out.println(result.toJSONString());
		uri = "http://localhost:9090/rest/employee/";
		result = restTemplate.postForObject(uri, result, JSONObject.class);
		System.out.println(result.toJSONString());

		Employee emp = new Employee();
		emp.setEmpID("112");
		emp.setEmpName(name);
		return emp;
	}

	public Employee getEmployeeById(String id) {
		Employee emp = new Employee();
		emp.setEmpID(id);
		emp.setEmpName("Haris");
		return emp;
	}

	public ServiceResponse setEmployee(JSONObject jsonObj) {
		String QUEUE_NAME = "EMPLOYEE";
		ServiceResponse serviceResponse = null;
		try {
			if (mqProducerPool == null) {
				initialize(Utility.getProperty(Constant.RABBITMQ_HOST_NAME), Integer.parseInt(Utility.getProperty(Constant.RABBITMQ_PORT)));
			}
			mqProducerPool.enqueue(jsonObj.toString(), QUEUE_NAME, null);
			// EmployeeResource.mqProducerPool.enqueue(content, QUEUE_NAME, 4);
			serviceResponse = new ServiceResponse(0, "SUCCESS", jsonObj);
		} catch (Exception _ex) {
			logger.error(_ex.getMessage());
			Errorlogger.error(ErrorHandling.getStackTrace(_ex));
			serviceResponse = new ServiceResponse(-10, "FAILURE", _ex);
		}
		return serviceResponse;
	}

	@PostConstruct
	private void postConstruct() throws NumberFormatException, Exception {
		if (mqProducerPool == null) {
			initialize(Utility.getProperty(Constant.RABBITMQ_HOST_NAME), Integer.parseInt(Utility.getProperty(Constant.RABBITMQ_PORT)));
		}
	}

	@PreDestroy
	private void preDestroy() {
		try {
			if (mqProducerPool != null) {
				mqProducerPool.shutdown();
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			Errorlogger.error(ErrorHandling.getStackTrace(ex));
		}
	}
}
