package com.esb.msgflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.esb.model.FlowParametersResponse;
import com.esb.model.FlowStatusResponse;

@RestController
public class EmployeeTimerFlowController {

	@Autowired
	private EmployeeTimerFlowService empTimerFlowService;

	@RequestMapping(path = "/EmployeeTimerFlow/getFlowState", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity getFlowState() {
		FlowStatusResponse flowResp = new FlowStatusResponse();
		flowResp.setStatus((empTimerFlowService.getIsInitialized()) ? 1 : 0);
		return ResponseEntity.ok(flowResp);
	}

	@RequestMapping(path = "/EmployeeTimerFlow/startFlow", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity startFlow() {
		empTimerFlowService.initialize();
		FlowStatusResponse flowResp = new FlowStatusResponse();
		flowResp.setStatus(1);
		return ResponseEntity.ok(flowResp);
	}

	@RequestMapping(path = "/EmployeeTimerFlow/stopFlow", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity stopFlow() {
		empTimerFlowService.delete();
		FlowStatusResponse flowResp = new FlowStatusResponse();
		flowResp.setStatus(1);
		return ResponseEntity.ok(flowResp);
	}

	@RequestMapping(path = "/EmployeeTimerFlow/reloadFlow", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity reloadFlow() {
		empTimerFlowService.delete();
		try {
			Thread.sleep(5000);
		} catch (Exception ex) {
		}
		empTimerFlowService.initialize();
		FlowStatusResponse flowResp = new FlowStatusResponse();
		flowResp.setStatus(1);
		return ResponseEntity.ok(flowResp);
	}

	@RequestMapping(path = "/EmployeeTimerFlow/setParameters/{threads}/{tps}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity setParameters(@PathVariable Integer threads, @PathVariable Integer tps) {
		empTimerFlowService.delete();
		try {
			Thread.sleep(5000);
		} catch (Exception ex) {
		}
		empTimerFlowService.initialize();
		FlowStatusResponse flowResp = new FlowStatusResponse();
		flowResp.setStatus(1);
		return ResponseEntity.ok(flowResp);
	}

	@RequestMapping(path = "/EmployeeTimerFlow/getParameters", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity getParameters() {
		FlowParametersResponse flowResp = new FlowParametersResponse();
		flowResp.setThreads(1);
		flowResp.setTps(1);
		return ResponseEntity.ok(flowResp);
	}
}
