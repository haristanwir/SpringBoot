package com.esb.client.soap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;

import com.unicacorp.interact.api.soap.ExecuteBatch;
import com.unicacorp.interact.api.soap.ExecuteBatchResponse;

@Service
public class InteractSOAPClient {

	@Autowired
	private WebServiceTemplate wsTemplate;

	private static final Logger logger = LogManager.getLogger(InteractSOAPClient.class.getName());

	public ExecuteBatchResponse executeBatch(ExecuteBatch request) {
		ExecuteBatchResponse response = (ExecuteBatchResponse) wsTemplate.marshalSendAndReceive("http://localhost:21006/interact/services/InteractService/", request);
		logger.info("response:" + response.getReturn().getValue().getBatchStatusCode());
		return response;

	}

}
