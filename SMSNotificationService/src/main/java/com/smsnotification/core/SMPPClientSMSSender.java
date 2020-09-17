package com.smsnotification.core;

import java.util.Random;

import org.apache.logging.log4j.Logger;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.impl.DefaultSmppSession;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.tlv.Tlv;
import com.cloudhopper.smpp.type.Address;
import com.smsnotification.utility.Constant;
import com.smsnotification.utility.ErrorHandling;
import com.smsnotification.utility.SMSLogger;
import com.smsnotification.utility.Utility;

/**
 *
 * @author Haris Tanwir
 */
public class SMPPClientSMSSender {

	private static final Logger logger = SMSLogger.getLogger(SMPPClientSMSSender.class.getName());
	private static final Logger Errorlogger = SMSLogger.getLogger(ErrorHandling.class.getName());

	private static byte deliveryReceipt = SmppConstants.REGISTERED_DELIVERY_SMSC_RECEIPT_NOT_REQUESTED;

	private static byte srcTON = SmppConstants.TON_UNKNOWN;
	private static byte destTON = SmppConstants.TON_UNKNOWN;
	private static byte srcNPI = SmppConstants.NPI_UNKNOWN;
	private static byte destNPI = SmppConstants.NPI_UNKNOWN;

	private static byte generalDataCodingDefault = SmppConstants.DATA_CODING_DEFAULT;
	private static byte generalDataCodingUCS2 = SmppConstants.DATA_CODING_UCS2;
	private static byte generalDataCodingDefaultFlash = (byte) 0x10;
	private static byte generalDataCodingUCS2Flash = (byte) 0x18;

	private static long timeout = SmppConstants.DEFAULT_CONNECT_TIMEOUT;
	private static Boolean isPayload = false;
	private static Boolean isLongSMS_SAR = false;

	private static ThroughputController tpsController = null;

	private DefaultSmppSession session = null;
	private byte generalDataCoding = 0;
	private int dataCodingValue = 0;
	private boolean priorityFlag = true;

	private byte replaceIfPresent = SmppConstants.SM_NOREPLACE;
	private String serviceType = null;
	private String validityPeriod = null;
	private byte protocolID = (byte) 0x00;
	private byte defaultMsgId = (byte) 0x00;

	static {
		Boolean isDeliveryRequested = Boolean.valueOf(Utility.getProperty(Constant.SMPP_CLIENT_DELIVERY_FLAG));
		if (isDeliveryRequested) {
			deliveryReceipt = SmppConstants.REGISTERED_DELIVERY_SMSC_RECEIPT_REQUESTED;
		}
		tpsController = new ThroughputController(Integer.parseInt(Utility.getProperty(Constant.SMPP_CLIENT_MAX_TPS)));
		srcTON = Utility.getTONByteValue(Utility.getProperty(Constant.SMPP_CLIENT_SRC_TON));
		destTON = Utility.getTONByteValue(Utility.getProperty(Constant.SMPP_CLIENT_DEST_TON));
		srcNPI = Utility.getNPIByteValue(Utility.getProperty(Constant.SMPP_CLIENT_SRC_NPI));
		destNPI = Utility.getNPIByteValue(Utility.getProperty(Constant.SMPP_CLIENT_DEST_NPI));
		timeout = Long.parseLong(Utility.getProperty(Constant.SMPP_CLIENT_BIND_TIMEOUT));
		isPayload = Boolean.valueOf(Utility.getProperty(Constant.SMPP_CLIENT_PAYLOAD_FLAG));
		isLongSMS_SAR = Boolean.valueOf(Utility.getProperty(Constant.SMPP_CLIENT_MULTIPART_SAR_FLAG));
	}

	public SMPPClientSMSSender(DefaultSmppSession session) {
		super();
		this.session = session;
	}

	public String sendSMS(String source, String destination, String smsText, int codingValue, int sequenceNumber) throws Exception {

		if (codingValue == 1) {
			generalDataCoding = generalDataCodingDefault;
		} else if (codingValue == 2) {
			generalDataCoding = generalDataCodingUCS2;
			dataCodingValue = 8;
		} else if (codingValue == 3) {
			generalDataCoding = generalDataCodingDefaultFlash;
		} else if (codingValue == 4) {
			generalDataCoding = generalDataCodingUCS2Flash;
			dataCodingValue = 8;
		}

		if ((dataCodingValue == 8 && smsText.length() > 60)) {
			if (!isPayload) {
				if (isLongSMS_SAR) {
					return submitLongSMS_SAR(source, destination, smsText, sequenceNumber)[0];
				} else {
					return submitLongSMS_UDH(source, destination, smsText, sequenceNumber)[0];
				}
			} else {
				return submit(source, destination, smsText, sequenceNumber, true);
			}
		} else if ((dataCodingValue == 0 && smsText.length() > 150)) {
			if (!isPayload) {
				if (isLongSMS_SAR) {
					return submitLongSMS_SAR(source, destination, smsText, sequenceNumber)[0];
				} else {
					return submitLongSMS_UDH(source, destination, smsText, sequenceNumber)[0];
				}
			} else {
				return submit(source, destination, smsText, sequenceNumber, true);
			}
		}
		return submit(source, destination, smsText, sequenceNumber, false);
	}

	private String submit(String source, String destination, String smsText, int sequenceNumber, boolean isPayload) throws Exception {
		String[] msgIds = new String[1];
		String Log_Prefix = "";
		try {
			SubmitSm request = new SubmitSm();
			request.setServiceType(serviceType);
			request.setSourceAddress(new Address(srcTON, srcNPI, source));
			request.setDestAddress(new Address(destTON, destNPI, destination));
			request.setReplaceIfPresent(replaceIfPresent);
			request.setRegisteredDelivery(deliveryReceipt);
			if (generalDataCoding == generalDataCodingUCS2 || generalDataCoding == generalDataCodingUCS2Flash) {
				byte[] textBytes = CharsetUtil.encode(smsText, CharsetUtil.CHARSET_UCS_2);
				if (isPayload) {
					Log_Prefix = "SUBMIT_SM|UTF8 ENCODING|PAYLOAD|";
					request.setShortMessage((new byte[0]));
					request.setOptionalParameter(new Tlv(SmppConstants.TAG_MESSAGE_PAYLOAD, textBytes));
				} else {
					Log_Prefix = "SUBMIT_SM|UTF8 ENCODING|NORMAL|";
					request.setShortMessage(textBytes);
				}
				logger.debug("UTF8 Message|sequenceNumber:" + sequenceNumber);
			} else {
				byte[] textBytes = CharsetUtil.encode(smsText, CharsetUtil.CHARSET_GSM);
				if (isPayload) {
					Log_Prefix = "SUBMIT_SM|SIMPLE ENCODING|PAYLOAD|";
					request.setShortMessage((new byte[0]));
					request.setOptionalParameter(new Tlv(SmppConstants.TAG_MESSAGE_PAYLOAD, textBytes));
				} else {
					Log_Prefix = "SUBMIT_SM|SIMPLE ENCODING|NORMAL|";
					request.setShortMessage(textBytes);
				}
				logger.debug("Simple Message|sequenceNumber:" + sequenceNumber);
			}

			request.setValidityPeriod(validityPeriod);
			request.setEsmClass(SmppConstants.ESM_CLASS_MM_DEFAULT);
			request.setProtocolId(protocolID);
			request.setPriority((byte) 0x00);
			if (priorityFlag) {
				request.setPriority((byte) 0x01);
			}
			request.setRegisteredDelivery(deliveryReceipt);
			request.setDataCoding(generalDataCoding);
			request.setDefaultMsgId(defaultMsgId);
			request.setSequenceNumber(sequenceNumber);
			tpsController.evaluateTPS();
			session.sendRequestPdu(request, timeout, false);
			msgIds[0] = "ASYNC_REQ";
			logger.debug(Log_Prefix + "ASYNC|length: " + smsText.length() + "|Sequence Number:" + sequenceNumber);
		} catch (Exception e) {
			Errorlogger.error("SUBMIT_SM|Submit operation failed|" + e);
		}

		return msgIds[0];
	}

	private String[] submitLongSMS_SAR(String source, String destination, String smsText, int sequenceNumber) throws Exception {
		String[] msgId = null;
		int splitSize = 0;
		if (dataCodingValue == 8) {
			splitSize = 67;
		} else if (dataCodingValue == 0) {
			splitSize = 153;
		}
		byte totalSegments = 0;
		if ((dataCodingValue == 8 && smsText.length() <= 60) || (dataCodingValue == 0 && smsText.length() <= 150)) {
			msgId = new String[1];
			msgId[0] = submit(source, destination, smsText, sequenceNumber, false);
			return msgId;
		}
		if (smsText != null) {
			totalSegments = (byte) getTotalSegmentsForTextMessage(smsText, splitSize);
		}
		Random random = new Random();
		Short sarSegRefNum = (short) (random.nextInt(Short.MAX_VALUE) + 1);
		String[] segmentData = splitIntoStringArray(smsText, splitSize, totalSegments);

		msgId = new String[totalSegments];
		byte seqNum = 0;
		String Log_Prefix = "SUBMIT_SM|MULTI_PART_SAR|";
		logger.debug(Log_Prefix + "refnum: " + sarSegRefNum + "|total segments:" + totalSegments + "|Collection Size:" + segmentData.length);

		for (byte i = 0; i < totalSegments; i++) {
			seqNum = (byte) (i + 1);
			logger.debug(Log_Prefix + "length: " + segmentData[i].length() + "|refnum: " + sarSegRefNum + "|total segments:" + totalSegments + "|Sequence Num:" + seqNum + "|Collection index:" + i + "|" + segmentData[i]);

			try {
				SubmitSm request = new SubmitSm();

				request.setServiceType(serviceType);
				request.setSourceAddress(new Address(srcTON, srcNPI, source));
				request.setDestAddress(new Address(destTON, destNPI, destination));
				request.setReplaceIfPresent(replaceIfPresent);
				if (generalDataCoding == generalDataCodingUCS2 || generalDataCoding == generalDataCodingUCS2Flash) {
					byte[] textBytes = CharsetUtil.encode(segmentData[i], CharsetUtil.CHARSET_UCS_2);
					request.setShortMessage(textBytes);
					logger.debug("UTF8 Message|sequenceNumber:" + sequenceNumber);
				} else {
					byte[] textBytes = CharsetUtil.encode(segmentData[i], CharsetUtil.CHARSET_GSM);
					request.setShortMessage(textBytes);
					logger.debug("Simple Message|sequenceNumber:" + sequenceNumber);
				}

				request.setValidityPeriod(validityPeriod);
				request.setEsmClass(SmppConstants.ESM_CLASS_MM_DEFAULT);
				request.setProtocolId(protocolID);
				request.setPriority((byte) 0x00);
				if (priorityFlag) {
					request.setPriority((byte) 0x01);
				}
				request.setRegisteredDelivery(deliveryReceipt);
				request.setDataCoding(generalDataCoding);
				request.setDefaultMsgId(defaultMsgId);

				request.setOptionalParameter(new Tlv(SmppConstants.TAG_SAR_MSG_REF_NUM, new byte[] { (byte) (sarSegRefNum.shortValue() & 0xff), (byte) ((sarSegRefNum.shortValue() >> 8) & 0xff) }));
				request.setOptionalParameter(new Tlv(SmppConstants.TAG_SAR_TOTAL_SEGMENTS, new byte[] { totalSegments }));
				request.setOptionalParameter(new Tlv(SmppConstants.TAG_SAR_SEGMENT_SEQNUM, new byte[] { seqNum }));

				if (i == 0) {
					request.setSequenceNumber(sequenceNumber);
				}
				tpsController.evaluateTPS();
				session.sendRequestPdu(request, timeout, false);
				msgId[i] = "ASYNC_REQ";
			} catch (Exception ex) {
				Errorlogger.error(Log_Prefix + "submitLongSMS_SAR internal exception|" + ex);
				Errorlogger.error(ErrorHandling.getCustomStackTrace(ex, ex.getMessage()));
			}
		}
		return msgId;
	}

	private String[] submitLongSMS_UDH(String source, String destination, String smsText, int sequenceNumber) {
		String[] msgId = null;
		byte[] textBytes = null;
		int splitSize = 0;
		if (dataCodingValue == 8) {
			splitSize = 60;
			textBytes = CharsetUtil.encode(smsText, CharsetUtil.CHARSET_UCS_2);
		} else if (dataCodingValue == 0) {
			splitSize = 130;
			textBytes = CharsetUtil.encode(smsText, CharsetUtil.CHARSET_GSM);
		}

		int maximumMultipartMessageSegmentSize = splitSize;
		byte[] byteSingleMessage = textBytes;
		byte[][] byteMessagesArray = splitUnicodeMessage(byteSingleMessage, maximumMultipartMessageSegmentSize, sequenceNumber);

		msgId = new String[byteMessagesArray.length];

		for (int i = 0; i < byteMessagesArray.length; i++) {
			try {
				SubmitSm request = new SubmitSm();
				request.setEsmClass(SmppConstants.ESM_CLASS_UDHI_MASK);
				request.setServiceType(serviceType);
				request.setSourceAddress(new Address(srcTON, srcNPI, source));
				request.setDestAddress(new Address(destTON, destNPI, destination));
				request.setReplaceIfPresent(replaceIfPresent);
				request.setShortMessage(byteMessagesArray[i]);

				request.setValidityPeriod(validityPeriod);
				request.setProtocolId(protocolID);
				request.setPriority((byte) 0x00);
				if (priorityFlag) {
					request.setPriority((byte) 0x01);
				}
				request.setRegisteredDelivery(deliveryReceipt);
				request.setDataCoding(generalDataCoding);
				if (i == 0) {
					request.setSequenceNumber(sequenceNumber);
				}
				tpsController.evaluateTPS();
				session.sendRequestPdu(request, timeout, false);
				msgId[i] = "ASYNC_REQ";
				logger.info("SUBMIT_SM|MULTI_PART_UDH|seqNumber:" + sequenceNumber + "|msgid:" + msgId[i]);
			} catch (Exception ex) {
				Errorlogger.error("SUBMIT_SM|MULTI_PART_UDH|submitLongSMS_UDH internal exception|" + ex);
				Errorlogger.error(ErrorHandling.getCustomStackTrace(ex, ex.getMessage()));
			}
		}
		return msgId;
	}

	private int getTotalSegmentsForTextMessage(String message, int splitPos) {
		int totalsegments = 1;
		if (message.length() > splitPos) {
			totalsegments = (message.length() / splitPos) + ((message.length() % splitPos > 0) ? 1 : 0);
		}
		return totalsegments;
	}

	private String[] splitIntoStringArray(String msg, int pos, int totalSegments) {
		String[] segmentData = new String[totalSegments];
		if (totalSegments > 1) {
			int splitPos = pos;
			int startIndex = 0;
			segmentData[startIndex] = new String();
			if (totalSegments == 1) {
				segmentData[startIndex] = msg;
				return segmentData;
			} else {
				segmentData[startIndex] = msg.substring(startIndex, splitPos);
			}
			for (int i = 1; i < totalSegments; i++) {
				segmentData[i] = new String();
				startIndex = splitPos;
				if (msg.length() - startIndex <= pos) {
					segmentData[i] = msg.substring(startIndex, msg.length());
				} else {
					splitPos = startIndex + pos;
					segmentData[i] = msg.substring(startIndex, splitPos);
				}
			}
		}
		return segmentData;
	}

	private byte[][] splitUnicodeMessage(byte[] aMessage, int maximumMultipartMessageSegmentSize, int seqNumber) {
		final byte UDHIE_HEADER_LENGTH = 0x05;
		final byte UDHIE_IDENTIFIER_SAR = 0x00;
		final byte UDHIE_SAR_LENGTH = 0x03;

		// determine how many messages have to be sent
		int numberOfSegments = aMessage.length / maximumMultipartMessageSegmentSize;
		int messageLength = aMessage.length;
		if (numberOfSegments > 255) {
			numberOfSegments = 255;
			messageLength = numberOfSegments * maximumMultipartMessageSegmentSize;
		}
		if ((messageLength % maximumMultipartMessageSegmentSize) > 0) {
			numberOfSegments++;
		}

		// prepare array for all of the msg segments
		byte[][] segments = new byte[numberOfSegments][];

		int lengthOfData;

		// generate new reference number
		byte[] referenceNumber = new byte[1];
		new Random().nextBytes(referenceNumber);

		// split the message adding required headers
		for (int i = 0; i < numberOfSegments; i++) {
			if (numberOfSegments - i == 1) {
				lengthOfData = messageLength - i * maximumMultipartMessageSegmentSize;
			} else {
				lengthOfData = maximumMultipartMessageSegmentSize;
			}
			// new array to store the header
			segments[i] = new byte[6 + lengthOfData];

			// UDH header
			// doesn't include itself, its header length
			segments[i][0] = UDHIE_HEADER_LENGTH;
			// SAR identifier
			segments[i][1] = UDHIE_IDENTIFIER_SAR;
			// SAR length
			segments[i][2] = UDHIE_SAR_LENGTH;
			// reference number (same for all messages)
			segments[i][3] = referenceNumber[0];
			// total number of segments
			segments[i][4] = (byte) numberOfSegments;
			// segment number
			segments[i][5] = (byte) (i + 1);
			// copy the data into the array
			System.arraycopy(aMessage, (i * maximumMultipartMessageSegmentSize), segments[i], 6, lengthOfData);

		}
		logger.debug("UDH split message|seqNumber:" + seqNumber + "|referenceNumber:" + referenceNumber[0]);
		return segments;
	}
}
