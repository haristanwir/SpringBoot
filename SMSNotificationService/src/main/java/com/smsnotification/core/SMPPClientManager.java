/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smsnotification.core;

import java.util.ArrayList;

import org.apache.logging.log4j.Logger;

import com.cloudhopper.smpp.impl.DefaultSmppSession;
import com.smsnotification.utility.Constant;
import com.smsnotification.utility.SMSLogger;
import com.smsnotification.utility.Utility;

/**
 *
 * @author Haris Tanwir
 */
public class SMPPClientManager {

	private static final Logger logger = SMSLogger.getLogger(SMPPClientManager.class.getName());
	private static SMPPClientManager manager = new SMPPClientManager();
	private Integer maxSession = Integer.parseInt(Utility.getProperty(Constant.SMPP_CLIENT_MAX_SESSIONS));
	private boolean initialized = false;
	private Integer sequenceNumber = 0;
	private ArrayList<DefaultSmppSession> sessions = new ArrayList<>();

	public static SMPPClientManager getManager() {
		return manager;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public synchronized int getSequenceNumber() {
		sequenceNumber = (sequenceNumber % Integer.MAX_VALUE) + 1;
		return sequenceNumber.intValue();
	}

	public synchronized void initialize() {
		if (this.initialized) {
			return;
		}
		for (int i = 0; i < maxSession; i++) {
			try {
				SMPPClientSession smppClientSession = new SMPPClientSession();
				sessions.add(smppClientSession.getSession());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		initialized = true;
		new Thread() {
			public void run() {
				while (initialized) {
					try {
						synchronized (sessions) {
							ArrayList<DefaultSmppSession> faulty_sessions = new ArrayList<>();
							for (DefaultSmppSession session : sessions) {
								if (!(session != null && session.isBound())) {
									faulty_sessions.add(session);
								}
							}
							sessions.removeAll(faulty_sessions);
							for (DefaultSmppSession faulty_session : faulty_sessions) {
								sessions.add(new SMPPClientSession().getSession());
							}
						}
						Thread.sleep(Long.parseLong(Utility.getProperty(Constant.SMPP_CLIENT_ENQUIRELINK_TIMEOUT)) / 2);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}.start();
	}

	public DefaultSmppSession getSession() {
		DefaultSmppSession session = null;
		try {
			synchronized (sessions) {
				session = sessions.remove(0);
				while (!session.isBound()) {
					session = null;
					session = sessions.remove(0);
				}
			}
		} catch (Exception ex) {
		}
		if (session == null) {
			session = new SMPPClientSession().getSession();
		}
		return session;
	}

	public void releaseSession(DefaultSmppSession session) {
		if (session != null) {
			if (session.isBound()) {
				synchronized (sessions) {
					sessions.add(session);
				}
			}
		}
	}

	public synchronized void unbindAll() {
		if (!this.initialized) {
			return;
		}
		synchronized (sessions) {
			for (DefaultSmppSession session : sessions) {
				if (session != null) {
					try {
						session.unbind(Long.parseLong(Utility.getProperty(Constant.SMPP_CLIENT_BIND_TIMEOUT)));
					} catch (Exception ex) {
					}
					try {
						session.close();
					} catch (Exception ex) {
					}
					try {
						session.destroy();
					} catch (Exception ex) {
					}
					session = null;
				}
			}
			sessions.clear();
		}
		initialized = false;
	}
}
