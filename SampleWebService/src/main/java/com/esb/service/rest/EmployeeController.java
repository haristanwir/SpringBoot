package com.esb.service.rest;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeController {

	@Autowired
	private EmployeeService employeeService;

	@RequestMapping(path = { "/rest/employee/id", "/rest/employee/id/", "/rest/employee/id/{id}" }, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity getEmployeeById(@PathVariable(required = false) String id) {
		return ResponseEntity.ok(employeeService.getEmployeeById(id));
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
