package com.esb.msgflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.esb.model.FlowStatusResponse;

@RestController
public class EmployeeInputFlowController {

	@Autowired
	private EmployeeInputFlowService empInputFlowService;

	@RequestMapping(path = "/EmployeeInputFlow/getFlowState", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity getFlowState() {
		FlowStatusResponse flowResp = new FlowStatusResponse();
		flowResp.setStatus((empInputFlowService.getIsInitialized()) ? 1 : 0);
		return ResponseEntity.ok(flowResp);
	}

	@RequestMapping(path = "/EmployeeInputFlow/startFlow", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity startFlow() {
		empInputFlowService.initialize();
		FlowStatusResponse flowResp = new FlowStatusResponse();
		flowResp.setStatus(1);
		return ResponseEntity.ok(flowResp);
	}

	@RequestMapping(path = "/EmployeeInputFlow/stopFlow", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity stopFlow() {
		empInputFlowService.delete();
		FlowStatusResponse flowResp = new FlowStatusResponse();
		flowResp.setStatus(1);
		return ResponseEntity.ok(flowResp);
	}

	@RequestMapping(path = "/EmployeeInputFlow/reloadFlow", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity reloadFlow() {
		empInputFlowService.delete();
		try {
			Thread.sleep(5000);
		} catch (Exception ex) {
		}
		empInputFlowService.initialize();
		FlowStatusResponse flowResp = new FlowStatusResponse();
		flowResp.setStatus(1);
		return ResponseEntity.ok(flowResp);
	}
}
