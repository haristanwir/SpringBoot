package com.samplesoapclient.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;

import com.esb.soap.soap.employeews.GetEmployeeRequest;
import com.esb.soap.soap.employeews.GetEmployeeResponse;
import com.esb.soap.soap.employeews.SetEmployeeRequest;
import com.esb.soap.soap.employeews.SetEmployeeResponse;
import com.unicacorp.interact.api.soap.ExecuteBatch;
import com.unicacorp.interact.api.soap.ExecuteBatchResponse;

@Service
public class SoapClient {

	@Autowired
	private WebServiceTemplate wsTemplate;

	private static final Logger logger = LogManager.getLogger(SoapClient.class.getName());

	public GetEmployeeResponse getEmployeeWS(GetEmployeeRequest request) {
		GetEmployeeResponse response = (GetEmployeeResponse) wsTemplate.marshalSendAndReceive("http://localhost:9090/soap/EmployeeWS/Interface/", request);
		return response;
	}

	public SetEmployeeResponse setEmployeeWS(SetEmployeeRequest request) {
		SetEmployeeResponse response = (SetEmployeeResponse) wsTemplate.marshalSendAndReceive("http://localhost:9090/soap/EmployeeWS/Interface/", request);
		return response;
	}

	public ExecuteBatchResponse executeBatch(ExecuteBatch request) {
		ExecuteBatchResponse response = (ExecuteBatchResponse) wsTemplate.marshalSendAndReceive("http://localhost:21006/interact/services/InteractService/", request);
		logger.info("response:" + response.getReturn().getValue().getBatchStatusCode());
		return response;

	}

}
