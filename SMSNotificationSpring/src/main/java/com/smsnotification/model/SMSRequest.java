package com.smsnotification.model;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement
public class SMSRequest {
	private String msisdn = null;
	private String sourceSystem = null;
	private String transactionID = null;
	private String senderTitle = null;
	private Boolean flashMsgFlag = null;
	private String smsText = null;

}
