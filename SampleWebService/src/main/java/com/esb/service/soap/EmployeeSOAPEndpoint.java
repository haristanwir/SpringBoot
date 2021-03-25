package com.esb.service.soap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.esb.soap.soap.employeews.GetEmployeeRequest;
import com.esb.soap.soap.employeews.GetEmployeeResponse;
import com.esb.soap.soap.employeews.SetEmployeeRequest;
import com.esb.soap.soap.employeews.SetEmployeeResponse;

@Endpoint
public class EmployeeSOAPEndpoint {

	@Autowired
	private EmployeeSOAPService empSoapService;

	private static final String NAMESPACE_URI = "http://soap.esb.com/soap/EmployeeWS";

	@ResponsePayload
	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "SetEmployeeRequest")
	public SetEmployeeResponse SetEmployee(@RequestPayload SetEmployeeRequest setEmpReq) {
		return empSoapService.setEmployee(setEmpReq);
	}

	@ResponsePayload
	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetEmployeeRequest")
	public GetEmployeeResponse GetEmployee(@RequestPayload GetEmployeeRequest getEmpReq) {
		return empSoapService.getEmployee(getEmpReq);
	}
}
