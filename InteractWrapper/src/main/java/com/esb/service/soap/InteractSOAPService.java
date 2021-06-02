package com.esb.service.soap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esb.client.soap.InteractSOAPClient;
import com.esb.utility.ErrorHandling;
import com.unicacorp.interact.api.soap.ExecuteBatch;
import com.unicacorp.interact.api.soap.ExecuteBatchResponse;

@Service
public class InteractSOAPService {

	@Autowired
	private InteractSOAPClient interactClient;

	private static final Logger logger = LogManager.getLogger(InteractSOAPService.class.getName());
	private static final Logger Errorlogger = LogManager.getLogger(ErrorHandling.class.getName());

	public ExecuteBatchResponse executeBatch(ExecuteBatch executeBatchRequest) {
		return interactClient.executeBatch(executeBatchRequest);
	}

}
