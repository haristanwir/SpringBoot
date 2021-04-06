/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smsnotification.core;

import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.impl.DefaultSmppSession;
import com.smsnotification.config.SMSProperties;
import com.smsnotification.utility.ErrorHandling;

/**
 *
 * @author Haris Tanwir
 */
public class SMPPClientSession {

	private static final Logger logger = LogManager.getLogger(SMPPClientSession.class.getName());
	private static final Logger Errorlogger = LogManager.getLogger(ErrorHandling.class.getName());

	private DefaultSmppClient smppClient = null;
	private DefaultSmppSession session = null;

	public SMPPClientSession(SmppSessionConfiguration configuration, SMSProperties smsConfig) {
		smppClient = new DefaultSmppClient(Executors.newCachedThreadPool(), 1);
		try {
			session = (DefaultSmppSession) smppClient.bind(configuration, new SMPPClientSessionHandler());
			new EnquireLinkThread(session, smsConfig.getSMPP_CLIENT_ENQUIRELINK_TIMEOUT(), configuration.getSystemId()).start();
		} catch (Exception ex) {
			logger.info("Error while SMPP bind:" + ex.getMessage());
			Errorlogger.error(ErrorHandling.getStackTrace(ex));
		}
	}

	public DefaultSmppSession getSession() {
		return session;
	}

}
