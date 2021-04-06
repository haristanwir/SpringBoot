/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smsnotification.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTimeZone;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.PduAsyncResponse;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.tlv.Tlv;
import com.cloudhopper.smpp.util.DeliveryReceipt;
import com.smsnotification.utility.ErrorHandling;

/**
 *
 * @author Haris Tanwir
 */
public class SMPPClientSessionHandler extends DefaultSmppSessionHandler {

	private static final Logger logger = LogManager.getLogger(SMPPClientSessionHandler.class.getName());
	private static final Logger Errorlogger = LogManager.getLogger(ErrorHandling.class.getName());

	public SMPPClientSessionHandler() {
	}

	@Override
	public void firePduRequestExpired(PduRequest pduRequest) {
		logger.debug("firePduRequestExpired|pduRequest:" + pduRequest.toString());
	}

	@Override
	public PduResponse firePduRequestReceived(PduRequest pduRequest) {
		int commandId = pduRequest.getCommandId();
		if (commandId == SmppConstants.CMD_ID_DELIVER_SM) {
			DeliverSm deliverSm = (DeliverSm) pduRequest;
			logger.debug("DELIVER_SM|firePduRequestReceived|deliverSm:" + deliverSm.toString());
			if (deliverSm.getEsmClass() == SmppConstants.ESM_CLASS_MT_SMSC_DELIVERY_RECEIPT) {
				// Delivery Receipt
				try {
					String shortMessage = new String(deliverSm.getShortMessage());
					DeliveryReceipt deliveryReceipt = DeliveryReceipt.parseShortMessage(shortMessage, DateTimeZone.getDefault(), false);
					String messageId = null;
					Tlv receipted_msg_id = deliverSm.getOptionalParameter(SmppConstants.TAG_RECEIPTED_MSG_ID);
					if (receipted_msg_id != null) {
						logger.debug("DELIVER_SM|TLV found receipted_msg_id is : " + receipted_msg_id.toString() + " , " + receipted_msg_id.getValueAsString().toLowerCase());
						messageId = receipted_msg_id.getValueAsString().toLowerCase();
					} else {
						messageId = deliveryReceipt.getMessageId().toLowerCase();
					}

					String dlvrstat = null;
					Tlv message_state = deliverSm.getOptionalParameter(SmppConstants.TAG_MSG_STATE);
					if (message_state != null) {
						logger.debug("DELIVER_SM|TLV found message_state is : " + message_state.toString() + " , " + new Integer(message_state.getValueAsByte()));
						dlvrstat = DeliveryReceipt.toStateText(message_state.getValueAsByte());
					} else {
						dlvrstat = DeliveryReceipt.toStateText(deliveryReceipt.getState());
					}

					logger.info("DELIVER_SM|Receiving delivery receipt for messageId:" + messageId + ", dlvrstat:" + dlvrstat + ", shortMessage:" + shortMessage);
				} catch (Exception ex) {
					logger.error("DELIVER_SM|Error in DeliveryReceipt : " + ex.getMessage());
					Errorlogger.error(ErrorHandling.getStackTrace(ex));
				}
			} else {
				// User Reply
				logger.info("DELIVER_SM|ESMClass:" + deliverSm.getEsmClass() + "|Seq#:" + deliverSm.getSequenceNumber() + ", Source:" + deliverSm.getSourceAddress().getAddress() + ", Destination:" + deliverSm.getDestAddress().getAddress());

				String optin = deliverSm.getDestAddress().getAddress();
				String msisdn = deliverSm.getSourceAddress().getAddress();

				String text = "";
				if (deliverSm.getShortMessage() != null) {
					if (deliverSm.getDataCoding() == SmppConstants.DATA_CODING_DEFAULT) {
						text = CharsetUtil.decode(deliverSm.getShortMessage(), CharsetUtil.CHARSET_GSM);
					} else if (deliverSm.getDataCoding() == SmppConstants.DATA_CODING_LATIN1) {
						text = CharsetUtil.decode(deliverSm.getShortMessage(), CharsetUtil.CHARSET_ISO_8859_1);
					} else if (deliverSm.getDataCoding() == SmppConstants.DATA_CODING_UCS2) {
						text = CharsetUtil.decode(deliverSm.getShortMessage(), CharsetUtil.CHARSET_UCS_2);
					} else {
						text = new String(deliverSm.getShortMessage());
					}
				}
				logger.info("DELIVER_SM|User response received|" + msisdn + "|" + optin + "|" + text);
			}
		} else {
			logger.debug("firePduRequestReceived|pduRequest:" + pduRequest.toString());
		}
		PduResponse response = pduRequest.createResponse();
		return response;
	}

	@Override
	public void fireExpectedPduResponseReceived(PduAsyncResponse pduAsyncResponse) {
		int commandId = pduAsyncResponse.getResponse().getCommandId();
		if (commandId == SmppConstants.CMD_ID_SUBMIT_SM_RESP) {
			SubmitSmResp submitResp = (SubmitSmResp) pduAsyncResponse.getResponse();
			logger.debug("SUBMIT_SM_RESP|fireExpectedPduResponseReceived|submitResp:" + submitResp.toString());
			if (submitResp.getCommandStatus() == SmppConstants.STATUS_THROTTLED) {
				logger.info("SUBMIT_SM_RESP|SMS Throttled|Seq#:" + submitResp.getSequenceNumber());
			} else if (submitResp.getCommandStatus() == SmppConstants.STATUS_OK) {
				logger.info("SUBMIT_SM_RESP|Seq#:" + submitResp.getSequenceNumber() + "|messageId:" + submitResp.getMessageId());
			} else {
				logger.info("SUBMIT_SM_RESP|Invalid SubmitSM_Resp state|" + submitResp.toString());
			}
		} else {
			logger.debug("fireExpectedPduResponseReceived|pduAsyncResponse:" + pduAsyncResponse.toString());
		}
	}

	@Override
	public void fireUnexpectedPduResponseReceived(PduResponse pduResponse) {
		logger.debug("fireUnexpectedPduResponseReceived|pduResponse:" + pduResponse.toString());
	}

}
