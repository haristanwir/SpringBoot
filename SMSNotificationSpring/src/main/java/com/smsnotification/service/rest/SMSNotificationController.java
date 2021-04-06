package com.smsnotification.service.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
	private SMSNotificationService smsService;

	@RequestMapping(path = "/rest/notificationservice/v1/sendSMS", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SMSResponse> sendSMS(@RequestBody SMSRequest smsRequest) {
		SMSResponse smsResp = smsService.sendSMS(smsRequest);
		if (smsResp.getReturnCode().equals("000")) {
			return ResponseEntity.ok(smsResp);
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(smsResp);
		}
	}
}
