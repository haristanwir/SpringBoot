package com.smsnotification.service.rest;

import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cloudhopper.commons.charset.GSMCharset;
import com.cloudhopper.smpp.impl.DefaultSmppSession;
import com.smsnotification.core.SMPPClientManager;
import com.smsnotification.core.SMPPClientSMSSender;
import com.smsnotification.model.SMSRequest;
import com.smsnotification.model.SMSResponse;
import com.smsnotification.utility.ErrorHandling;
import com.smsnotification.utility.SMSLogger;

@Service
public class SMSNotificationService {

	private static final Logger logger = SMSLogger.getLogger(SMSNotificationService.class.getName());
	private static final Logger Errorlogger = SMSLogger.getLogger(ErrorHandling.class.getName());

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

		SMPPClientManager smppClientManager = SMPPClientManager.getManager();
		if (!smppClientManager.isInitialized()) {
			synchronized (smppClientManager) {
				if (!smppClientManager.isInitialized()) {
					smppClientManager.initialize();
				}
			}
		}
		Boolean submitSMSent = false;
		DefaultSmppSession smppClientSession = smppClientManager.getSession();

		long loopTime = System.currentTimeMillis() + 5000;
		while (!(smppClientSession != null && smppClientSession.isBound()) && (System.currentTimeMillis() < loopTime)) {
			try {
				Thread.sleep(10L);
			} catch (Exception ex) {
				Errorlogger.error(ErrorHandling.getStackTrace(ex));
			}
			smppClientSession = smppClientManager.getSession();
		}

		if (smppClientSession != null && smppClientSession.isBound()) {
			try {
				int sequenceNumber = smppClientManager.getSequenceNumber();
				SMPPClientSMSSender smsSender = new SMPPClientSMSSender(smppClientSession);
				int codingValue = 1;
				if (!unicodeFlag && !flashMsgFlag) {
					codingValue = 1;
				} else if (unicodeFlag && !flashMsgFlag) {
					codingValue = 2;
				} else if (!unicodeFlag && flashMsgFlag) {
					codingValue = 3;
				} else if (unicodeFlag && flashMsgFlag) {
					codingValue = 4;
				}
				long start = System.currentTimeMillis();
				smsSender.sendSMS(senderTitle, msisdn, smsText, codingValue, sequenceNumber);
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

	@PostConstruct
	private void postConstruct() {
		try {
			logger.info("--SMPPClientManager initialization started--");
			SMPPClientManager smppClientManager = SMPPClientManager.getManager();
			if (!smppClientManager.isInitialized()) {
				synchronized (smppClientManager) {
					if (!smppClientManager.isInitialized()) {
						smppClientManager.initialize();
					}
				}
			}
			logger.info("--SMPPClientManager initialization completed--");
		} catch (Throwable e) {
			logger.info("Error while SMPP client initialization: ", e.getMessage());
			Errorlogger.error(ErrorHandling.getStackTrace(e));
		}
	}

	@PreDestroy
	private void preDestroy() {
		SMPPClientManager smppClientManager = SMPPClientManager.getManager();
		if (smppClientManager.isInitialized()) {
			synchronized (smppClientManager) {
				if (smppClientManager.isInitialized()) {
					logger.info("--SMPPClientManager Deinitialization started--");
					smppClientManager.unbindAll();
					logger.info("--SMPPClientManager Deinitialization completed--");
				}
			}
		}
	}

}
