package com.esb.service.soap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.unicacorp.interact.api.soap.ExecuteBatch;
import com.unicacorp.interact.api.soap.ExecuteBatchResponse;

@Endpoint
public class InteractSOAPEndpoint {

	@Autowired
	private InteractSOAPService interactService;

	private static final String NAMESPACE_URI = "http://soap.api.interact.unicacorp.com";

	@ResponsePayload
	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "executeBatch")
	public ExecuteBatchResponse ExecuteBatch(@RequestPayload ExecuteBatch executeBatchRequest) {
		return interactService.executeBatch(executeBatchRequest);
	}
}
