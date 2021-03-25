package com.esb.service.soap;

import java.io.StringWriter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.esb.soap.soap.employeews.EmployeeReqType;
import com.esb.soap.soap.employeews.EmployeeRespType;
import com.esb.soap.soap.employeews.GetEmployeeRequest;
import com.esb.soap.soap.employeews.GetEmployeeResponse;
import com.esb.soap.soap.employeews.SetEmployeeRequest;
import com.esb.soap.soap.employeews.SetEmployeeResponse;
import com.esb.utility.Constant;
import com.esb.utility.ErrorHandling;
import com.esb.utility.FlowLogger;
import com.esb.utility.RabbitMQProdConnectionPool;
import com.esb.utility.Utility;

@Service
public class EmployeeSOAPService {

	private static final Logger logger = FlowLogger.getLogger(EmployeeSOAPService.class.getName());
	private static final Logger Errorlogger = FlowLogger.getLogger(ErrorHandling.class.getName());

	private RabbitMQProdConnectionPool mqProducerPool = null;
	// private static ActiveMQProdConnectionPool mqProducerPool = null;
	// private static IBMMQProdConnectionPool mqProducerPool = null;

	public synchronized void initialize(String mq_ip, Integer mq_port) {
		try {
			mqProducerPool.shutdown();
		} catch (Exception ex) {
		}
		mqProducerPool = new RabbitMQProdConnectionPool(mq_ip, mq_port, EmployeeSOAPService.class.getName());
		// mqProducerPool = new ActiveMQProdConnectionPool("localhost", 61616);
		// mqProducerPool = new IBMMQProdConnectionPool("localhost", 1415, "IIB10QMGR",
		// "SYSTEM.ADMIN.SVRCONN");
	}

	public SetEmployeeResponse setEmployee(SetEmployeeRequest setEmpReq) {
		String QUEUE_NAME = "EMPLOYEE";
		SetEmployeeResponse setEmpResp = new SetEmployeeResponse();
		EmployeeRespType empResp = new EmployeeRespType();
		try {
			if (mqProducerPool == null) {
				initialize(Utility.getProperty(Constant.RABBITMQ_HOST_NAME), Integer.parseInt(Utility.getProperty(Constant.RABBITMQ_PORT)));
			}
			StringWriter xmlOutputStream = new StringWriter();
			JAXBContext jaxbContext = JAXBContext.newInstance(setEmpReq.getClass());
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.marshal(setEmpReq, xmlOutputStream);
			String xmlString = xmlOutputStream.toString();
			mqProducerPool.enqueue(xmlString, QUEUE_NAME, null);
			// EmployeeResource.mqProducerPool.enqueue(content, QUEUE_NAME, 4);
			empResp.setResultCode("1");
			empResp.setResultDesc("SUCCESS");
		} catch (Exception _ex) {
			logger.error(_ex.getMessage());
			Errorlogger.error(ErrorHandling.getStackTrace(_ex));
			empResp.setResultCode("-10");
			empResp.setResultDesc("FAILURE");
		}
		setEmpResp.setResponse(empResp);
		return setEmpResp;
	}

	public GetEmployeeResponse getEmployee(GetEmployeeRequest getEmpReq) {
		GetEmployeeResponse getEmpResp = new GetEmployeeResponse();
		EmployeeReqType empObj = new EmployeeReqType();
		empObj.setID("123");
		empObj.setName("haris");
		getEmpResp.setResponse(empObj);
		return getEmpResp;
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
