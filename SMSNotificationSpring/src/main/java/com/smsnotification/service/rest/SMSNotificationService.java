package com.smsnotification.service.rest;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloudhopper.commons.charset.GSMCharset;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.impl.DefaultSmppSession;
import com.smsnotification.config.SMSProperties;
import com.smsnotification.core.SMPPClientManager;
import com.smsnotification.core.SMPPClientSMSSender;
import com.smsnotification.model.SMSRequest;
import com.smsnotification.model.SMSResponse;
import com.smsnotification.utility.ErrorHandling;

@Service
public class SMSNotificationService {

	@Autowired
	private SMPPClientManager smppClientManager;

	@Autowired
	private SMSProperties smsConfig;

	@Autowired
	private SMPPClientSMSSender smsSender;

	private final Logger logger = LogManager.getLogger(SMSNotificationService.class.getName());
	private final Logger Errorlogger = LogManager.getLogger(ErrorHandling.class.getName());

	private byte generalDataCodingDefault = SmppConstants.DATA_CODING_DEFAULT;
	private byte generalDataCodingUCS2 = SmppConstants.DATA_CODING_UCS2;
	private byte generalDataCodingDefaultFlash = (byte) 0x10;
	private byte generalDataCodingUCS2Flash = (byte) 0x18;

	public SMSResponse sendSMS(SMSRequest smsRequest) {
		String msisdn = smsRequest.getMsisdn();
		String transactionID = smsRequest.getTransactionID();
		String sourceSystem = smsRequest.getSourceSystem();
		String senderTitle = smsRequest.getSenderTitle();
		String smsText = smsRequest.getSmsText();
		Boolean unicodeFlag = !GSMCharset.canRepresent(smsText);
		Boolean flashMsgFlag = smsRequest.getFlashMsgFlag();

		String appConnectUUID = UUID.randomUUID().toString();

		logger.info("SUBMIT_SM|Incoming request|appConnectUUID:" + appConnectUUID + "|smsRequest:" + smsRequest);

		Boolean submitSMSent = false;
		DefaultSmppSession smppClientSession = smppClientManager.getSession();

		long loopTime = System.currentTimeMillis() + (smsConfig.getSMPP_CLIENT_ENQUIRELINK_TIMEOUT() / 2);
		while (!(smppClientSession != null && smppClientSession.isBound()) && (System.currentTimeMillis() < loopTime)) {
			try {
				Thread.sleep(500L);
			} catch (Exception ex) {
				Errorlogger.error(ErrorHandling.getStackTrace(ex));
			}
			smppClientSession = smppClientManager.getSession();
		}

		if (smppClientSession != null && smppClientSession.isBound()) {
			try {
				int sequenceNumber = smppClientManager.getSequenceNumber();
				int codingValue = 1;
				byte generalDataCoding = 0;
				int dataCodingValue = 0;

				if (!unicodeFlag && !flashMsgFlag) {
					codingValue = 1;
					generalDataCoding = generalDataCodingDefault;
				} else if (unicodeFlag && !flashMsgFlag) {
					codingValue = 2;
					generalDataCoding = generalDataCodingUCS2;
					dataCodingValue = 8;
				} else if (!unicodeFlag && flashMsgFlag) {
					codingValue = 3;
					generalDataCoding = generalDataCodingDefaultFlash;
				} else if (unicodeFlag && flashMsgFlag) {
					codingValue = 4;
					generalDataCoding = generalDataCodingUCS2Flash;
					dataCodingValue = 8;
				}

				long start = System.currentTimeMillis();
				smsSender.sendSMS(smppClientSession, generalDataCoding, dataCodingValue, senderTitle, msisdn, smsText, sequenceNumber);
				long endtime = System.currentTimeMillis() - start;
				submitSMSent = true;
				logger.info("SUBMIT_SM|SMS sent for delivery|appConnectUUID:" + appConnectUUID + "|transactionID:" + transactionID + "|msisdn:" + msisdn + "|senderTitle:" + senderTitle + "|sequenceNumber:" + sequenceNumber + "|codingValue:" + codingValue + "|smppSessionID:" + smppClientSession.hashCode() + "|" + endtime + " msec");
			} catch (Exception ex) {
				logger.info("SUBMIT_SM|Error occurred while sending submitSm|appConnectUUID:" + appConnectUUID + "|transactionID:" + transactionID + "|msisdn:" + msisdn + "|Error:" + ex.getMessage());
				Errorlogger.error(ErrorHandling.getStackTrace(ex));
			}
		} else {
			logger.info("SUBMIT_SM|Error occurred while sending submitSm|appConnectUUID:" + appConnectUUID + "|transactionID:" + transactionID + "|msisdn:" + msisdn + "|Error: Invalid SMPP session");
		}
		smppClientManager.releaseSession(smppClientSession);
		String returnCode = "";
		String returnDesc = "";
		if (submitSMSent) {
			returnCode = "000";
			returnDesc = "SMS sent to SMSC for delivery";
		} else {
			returnCode = "999";
			returnDesc = "SMS sending failed, please retry";
		}

		SMSResponse smsResponse = new SMSResponse();
		smsResponse.setAppConnectUUID(appConnectUUID);
		smsResponse.setTransactionID(transactionID);
		smsResponse.setReturnCode(returnCode);
		smsResponse.setReturnDesc(returnDesc);

		logger.info("SUBMIT_SM|Sending response|appConnectUUID:" + appConnectUUID + "|smsResponse:" + smsResponse);

		return smsResponse;
	}

}
