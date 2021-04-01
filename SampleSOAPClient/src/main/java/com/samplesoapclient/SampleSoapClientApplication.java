package com.samplesoapclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.esb.soap.soap.employeews.EmployeeReqType;
import com.esb.soap.soap.employeews.GetEmployeeRequest;
import com.esb.soap.soap.employeews.GetEmployeeRequest.Request;
import com.esb.soap.soap.employeews.GetEmployeeResponse;
import com.esb.soap.soap.employeews.SetEmployeeRequest;
import com.esb.soap.soap.employeews.SetEmployeeResponse;
import com.samplesoapclient.client.SoapClient;

@RestController
@SpringBootApplication
public class SampleSoapClientApplication {

	@Autowired
	private SoapClient soapClient;

	@RequestMapping(path = "/get", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GetEmployeeResponse invokeSoapService_get() {
		GetEmployeeRequest request = new GetEmployeeRequest();
		Request req = new Request();
		req.setID("1220");
		request.setRequest(req);
		return soapClient.getEmployeeWS(request);
	}

	@RequestMapping(path = "/set", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public SetEmployeeResponse invokeSoapService_set() {
		SetEmployeeRequest request = new SetEmployeeRequest();
		EmployeeReqType req = new EmployeeReqType();
		req.setID("1221");
		req.setName("faiza");
		request.setRequest(req);
		return soapClient.setEmployeeWS(request);
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleSoapClientApplication.class, args);
	}

}
