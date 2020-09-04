package com.smsnotification.model;

import lombok.Data;

@Data
public class SMSRequest {
	private String msisdn = null;
	private String sourceSystem = null;
	private String transactionID = null;
	private String senderTitle = null;
	private Boolean unicodeFlag = null;
	private Boolean flashMsgFlag = null;
	private String smsText = null;

}
