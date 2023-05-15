package com.esb.core;

import java.io.IOException;

import javax.jms.JMSException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.esb.model.FlowParametersResponse;
import com.esb.model.FlowStatusResponse;
import com.esb.msgflow.EmployeeMQInput;

@RestController
public class EmployeeMQInputController {

	@Autowired
	private EmployeeMQInput employeeMQInput;

	@RequestMapping(path = "/EmployeeMQInput/getFlowState", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FlowStatusResponse> getFlowState() {
		FlowStatusResponse flowResp = new FlowStatusResponse();
		flowResp.setStatus((employeeMQInput.getIsInitialized()) ? 1 : 0);
		return ResponseEntity.ok(flowResp);
	}

	@RequestMapping(path = "/EmployeeMQInput/startFlow", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FlowStatusResponse> startFlow() throws IOException, JMSException {
		employeeMQInput.init();
		FlowStatusResponse flowResp = new FlowStatusResponse();
		flowResp.setStatus(1);
		return ResponseEntity.ok(flowResp);
	}

	@RequestMapping(path = "/EmployeeMQInput/stopFlow", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FlowStatusResponse> stopFlow() {
		employeeMQInput.shutdown();
		FlowStatusResponse flowResp = new FlowStatusResponse();
		flowResp.setStatus(1);
		return ResponseEntity.ok(flowResp);
	}

	@RequestMapping(path = "/EmployeeMQInput/reloadFlow", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FlowStatusResponse> reloadFlow() throws IOException, JMSException {
		employeeMQInput.shutdown();
		try {
			Thread.sleep(5000);
		} catch (Exception ex) {
		}
		employeeMQInput.init();
		FlowStatusResponse flowResp = new FlowStatusResponse();
		flowResp.setStatus(1);
		return ResponseEntity.ok(flowResp);
	}

	@RequestMapping(path = "/EmployeeMQInput/setParameters/{threads}/{tps}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FlowStatusResponse> setParameters(@PathVariable Integer threads, @PathVariable Integer tps) throws IOException, JMSException {
		employeeMQInput.shutdown();
		try {
			Thread.sleep(5000);
		} catch (Exception ex) {
		}
		employeeMQInput.setThreadPoolSize(threads);
		employeeMQInput.setThreadPoolTPS(tps);
		employeeMQInput.init();
		FlowStatusResponse flowResp = new FlowStatusResponse();
		flowResp.setStatus(1);
		return ResponseEntity.ok(flowResp);
	}

	@RequestMapping(path = "/EmployeeMQInput/getParameters", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FlowParametersResponse> getParameters() {
		FlowParametersResponse flowResp = new FlowParametersResponse();
		flowResp.setThreads(employeeMQInput.getThreadPoolSize());
		flowResp.setTps(employeeMQInput.getThreadPoolTPS());
		return ResponseEntity.ok(flowResp);
	}

}
