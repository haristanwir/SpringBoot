package com.smsnotification.model;

import lombok.Data;

@Data
public class SMSResponse {
	private String appConnectUUID = null;
	private String transactionID = null;
	private String returnCode = null;
	private String returnDesc = null;

}
