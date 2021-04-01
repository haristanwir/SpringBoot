package com.samplesoapclient.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;

import com.esb.soap.soap.employeews.GetEmployeeRequest;
import com.esb.soap.soap.employeews.GetEmployeeResponse;
import com.esb.soap.soap.employeews.SetEmployeeRequest;
import com.esb.soap.soap.employeews.SetEmployeeResponse;

@Service
public class SoapClient {

	@Autowired
	private Jaxb2Marshaller marshaller;

	public GetEmployeeResponse getEmployeeWS(GetEmployeeRequest request) {
		WebServiceTemplate wsTemplate = new WebServiceTemplate(marshaller);
		GetEmployeeResponse response = (GetEmployeeResponse) wsTemplate.marshalSendAndReceive("http://localhost:9090/soap/EmployeeWS/Interface/", request);
		return response;
	}

	public SetEmployeeResponse setEmployeeWS(SetEmployeeRequest request) {
		WebServiceTemplate wsTemplate = new WebServiceTemplate(marshaller);
		SetEmployeeResponse response = (SetEmployeeResponse) wsTemplate.marshalSendAndReceive("http://localhost:9090/soap/EmployeeWS/Interface/", request);
		return response;
	}

}
