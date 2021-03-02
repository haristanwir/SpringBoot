/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smsnotification.core;

import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.impl.DefaultSmppSession;
import com.cloudhopper.smpp.type.Address;
import com.smsnotification.utility.Constant;
import com.smsnotification.utility.ErrorHandling;
import com.smsnotification.utility.Utility;

/**
 *
 * @author Haris Tanwir
 */
public class SMPPClientSession {

	private static final Logger logger = LogManager.getLogger(SMPPClientSession.class.getName());
	private static final Logger Errorlogger = LogManager.getLogger(ErrorHandling.class.getName());

	private SmppSessionConfiguration configuration = null;
	private DefaultSmppClient smppClient = null;
	private DefaultSmppSession session = null;

	public SMPPClientSession() {
		configuration = new SmppSessionConfiguration();
		configuration.setHost(Utility.getProperty(Constant.SMPP_SERVER_HOST));
		configuration.setPort(Integer.parseInt(Utility.getProperty(Constant.SMPP_SERVER_PORT)));
		configuration.setSystemId(Utility.getProperty(Constant.SMPP_SERVER_SYSTEMID));
		configuration.setPassword(Utility.getProperty(Constant.SMPP_SERVER_PASSWORD));
		String bindType = Utility.getProperty(Constant.SMPP_CLIENT_BIND_TYPE);
		if (bindType.equalsIgnoreCase("TX")) {
			configuration.setType(SmppBindType.TRANSMITTER);
		} else if (bindType.equalsIgnoreCase("RX")) {
			configuration.setType(SmppBindType.RECEIVER);
		} else {
			configuration.setType(SmppBindType.TRANSCEIVER);
		}
		configuration.setInterfaceVersion(SmppConstants.VERSION_3_4);
		configuration.setBindTimeout(Long.parseLong(Utility.getProperty(Constant.SMPP_CLIENT_BIND_TIMEOUT)));
		configuration.setConnectTimeout(Long.parseLong(Utility.getProperty(Constant.SMPP_CLIENT_BIND_TIMEOUT)));
		configuration.setWindowSize(Integer.parseInt(Utility.getProperty(Constant.SMPP_CLIENT_MAX_TPS)));
		configuration.setRequestExpiryTimeout(Long.parseLong(Utility.getProperty(Constant.SMPP_CLIENT_BIND_TIMEOUT)));
		configuration.setSystemType(Utility.getProperty(Constant.SMPP_CLIENT_SYSTEM_TYPE));
		byte bindTON = Utility.getTONByteValue(Utility.getProperty(Constant.SMPP_CLIENT_BIND_TON));
		byte bindNPI = Utility.getNPIByteValue(Utility.getProperty(Constant.SMPP_CLIENT_BIND_NPI));
		configuration.setAddressRange(new Address(bindTON, bindNPI, null));
		configuration.setWindowWaitTimeout(configuration.getRequestExpiryTimeout());
		configuration.getLoggingOptions().setLogBytes(false);
		configuration.getLoggingOptions().setLogPdu(false);
		configuration.setCountersEnabled(false);
		smppClient = new DefaultSmppClient(Executors.newCachedThreadPool(), 1);
		try {
			session = (DefaultSmppSession) smppClient.bind(configuration, new SMPPClientSessionHandler());
			new EnquireLinkThread(session, Long.parseLong(Utility.getProperty(Constant.SMPP_CLIENT_ENQUIRELINK_TIMEOUT)), configuration.getSystemId()).start();
		} catch (Exception ex) {
			logger.info("Error while SMPP bind:" + ex.getMessage());
			Errorlogger.error(ErrorHandling.getStackTrace(ex));
		}
	}

	public DefaultSmppSession getSession() {
		return session;
	}

}
