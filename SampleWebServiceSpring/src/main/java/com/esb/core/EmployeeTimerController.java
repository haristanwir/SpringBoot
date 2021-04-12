package com.esb.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.esb.model.FlowParametersResponse;
import com.esb.model.FlowStatusResponse;
import com.esb.msgflow.EmployeeTimer;

@RestController
public class EmployeeTimerController {

	@Autowired
	private EmployeeTimer employeeTimer;

	@RequestMapping(path = "/EmployeeTimer/getFlowState", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FlowStatusResponse> getFlowState() {
		FlowStatusResponse flowResp = new FlowStatusResponse();
		flowResp.setStatus((employeeTimer.getIsInitialized()) ? 1 : 0);
		return ResponseEntity.ok(flowResp);
	}

	@RequestMapping(path = "/EmployeeTimer/startFlow", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FlowStatusResponse> startFlow() {
		employeeTimer.init();
		FlowStatusResponse flowResp = new FlowStatusResponse();
		flowResp.setStatus(1);
		return ResponseEntity.ok(flowResp);
	}

	@RequestMapping(path = "/EmployeeTimer/stopFlow", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FlowStatusResponse> stopFlow() {
		employeeTimer.shutdown();
		FlowStatusResponse flowResp = new FlowStatusResponse();
		flowResp.setStatus(1);
		return ResponseEntity.ok(flowResp);
	}

	@RequestMapping(path = "/EmployeeTimer/reloadFlow", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FlowStatusResponse> reloadFlow() {
		employeeTimer.shutdown();
		try {
			Thread.sleep(5000);
		} catch (Exception ex) {
		}
		employeeTimer.init();
		FlowStatusResponse flowResp = new FlowStatusResponse();
		flowResp.setStatus(1);
		return ResponseEntity.ok(flowResp);
	}

	@RequestMapping(path = "/EmployeeTimer/setParameters/{threads}/{tps}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FlowStatusResponse> setParameters(@PathVariable Integer threads, @PathVariable Integer tps) {
		employeeTimer.shutdown();
		try {
			Thread.sleep(5000);
		} catch (Exception ex) {
		}
		employeeTimer.init();
		FlowStatusResponse flowResp = new FlowStatusResponse();
		flowResp.setStatus(1);
		return ResponseEntity.ok(flowResp);
	}

	@RequestMapping(path = "/EmployeeTimer/getParameters", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FlowParametersResponse> getParameters() {
		FlowParametersResponse flowResp = new FlowParametersResponse();
		flowResp.setThreads(1);
		flowResp.setTps(0);
		return ResponseEntity.ok(flowResp);
	}

}
