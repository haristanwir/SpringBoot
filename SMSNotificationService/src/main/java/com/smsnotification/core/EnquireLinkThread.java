/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smsnotification.core;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.Logger;

import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.pdu.EnquireLinkResp;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import com.smsnotification.utility.SMSLogger;

/**
 *
 * @author Haris Tanwir
 */
public class EnquireLinkThread extends Thread {

	private static final Logger logger = SMSLogger.getLogger(EnquireLinkThread.class.getName());
	private final AtomicBoolean sendingEnquireLink = new AtomicBoolean(true);
	private SmppSession session = null;
	private Long timeout = null;
	private String systemId = null;

	public EnquireLinkThread(SmppSession session, Long timeout, String systemID) {
		this.session = session;
		this.timeout = timeout;
		this.systemId = systemID;
	}

	public void stopThread() {
		sendingEnquireLink.set(false);
	}

	@Override
	public void run() {
		while (sendingEnquireLink.get()) {
			try {
				EnquireLinkResp enquireLinkResp = null;
				enquireLinkResp = session.enquireLink(new EnquireLink(), timeout);
				logger.debug("enquireLinkResp|systemId:" + systemId + "|session:" + session.hashCode());
			} catch (RecoverablePduException e1) {
				closeSMPPSession();
			} catch (UnrecoverablePduException e1) {
				closeSMPPSession();
			} catch (SmppTimeoutException e1) {
				closeSMPPSession();
			} catch (SmppChannelException e1) {
				closeSMPPSession();
			} catch (InterruptedException e1) {
				closeSMPPSession();
			} catch (Exception e1) {
				closeSMPPSession();
			}
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				closeSMPPSession();
			}
		}
	}

	private void closeSMPPSession() {
		try {
			if (session != null) {
				stopThread();
				session.unbind(timeout);
				session.close();
				session.destroy();
				session = null;
			}
		} catch (Exception ex) {
			session = null;
			stopThread();
		}
	}
}
