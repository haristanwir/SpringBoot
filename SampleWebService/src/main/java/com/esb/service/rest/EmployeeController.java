package com.esb.service.rest;

import java.util.List;
import java.util.Map.Entry;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeController {

	@Autowired
	private EmployeeService employeeService;

	@RequestMapping(path = { "/rest/employee/id", "/rest/employee/id/", "/rest/employee/id/{id}" }, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity getEmployeeById(@RequestHeader HttpHeaders headers, @PathVariable(required = false) String id) {
		for (Object _header : headers.entrySet()) {
			Entry<String, List<String>> header = (Entry<String, List<String>>) _header;
			System.out.println(header.getKey() + " -> " + header.getValue());
		}
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Baeldung-Example-Header", "Value-ResponseEntityBuilderWithHttpHeaders");
		return ResponseEntity.ok().headers(responseHeaders).body(employeeService.getEmployeeById(id));
	}

	@RequestMapping(path = "/rest/employee/name/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity getEmployeeByName(@PathVariable String name) {
		return ResponseEntity.ok(employeeService.getEmployeeByName(name));
	}

	@RequestMapping(path = "/rest/employee", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity postEmployee(@RequestBody String inputData) throws ParseException {
		JSONObject jsonObj = (JSONObject) new JSONParser().parse(inputData);
		return ResponseEntity.ok(employeeService.setEmployee(jsonObj));
	}

}
