package com.smsnotification.service.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.smsnotification.model.SMSRequest;
import com.smsnotification.model.SMSResponse;

@RestController
public class SMSNotificationController {

	@Autowired
	SMSNotificationService smsService;

	@RequestMapping(path = "/notificationservice/v1/sendSMS", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SMSResponse> sendSMS(@RequestBody SMSRequest smsRequest) {
		return ResponseEntity.ok(smsService.sendSMS(smsRequest));
	}
}
