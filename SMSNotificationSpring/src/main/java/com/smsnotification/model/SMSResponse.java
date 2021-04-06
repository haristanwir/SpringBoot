package com.smsnotification.model;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement
public class SMSResponse {
	private String appConnectUUID = null;
	private String transactionID = null;
	private String returnCode = null;
	private String returnDesc = null;

}
