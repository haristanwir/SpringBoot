/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smsnotification.core;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppSession;
import com.smsnotification.config.SMSProperties;

/**
 *
 * @author Haris Tanwir
 */
@Component
public class SMPPClientManager {

	@Autowired
	private SMSProperties smsConfig;

	@Autowired
	private SmppSessionConfiguration configuration;

	private final Logger logger = LogManager.getLogger(SMPPClientManager.class.getName());
	private Integer maxSession = null;
	private boolean initialized = false;
	private Integer sequenceNumber = 0;
	private ArrayList<DefaultSmppSession> sessions = new ArrayList<>();
	private ScheduledExecutorService schedulerService = Executors.newSingleThreadScheduledExecutor();

	public boolean isInitialized() {
		return initialized;
	}

	public synchronized int getSequenceNumber() {
		sequenceNumber = (sequenceNumber % Integer.MAX_VALUE) + 1;
		return sequenceNumber.intValue();
	}

	@PostConstruct
	public synchronized void initialize() {
		if (this.initialized) {
			return;
		}
		logger.info("--SMPPClientManager initialization started--");
		maxSession = smsConfig.getSMPP_CLIENT_MAX_SESSIONS();
		for (int i = 0; i < maxSession; i++) {
			try {
				SMPPClientSession smppClientSession = new SMPPClientSession(configuration, smsConfig);
				sessions.add(smppClientSession.getSession());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		initialized = true;
		schedulerService.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				if (initialized) {
					ArrayList<DefaultSmppSession> faulty_sessions = new ArrayList<>();
					try {
						synchronized (sessions) {
							if (sessions.isEmpty()) {
								for (int i = 0; i < maxSession; i++) {
									try {
										sessions.add(new SMPPClientSession(configuration, smsConfig).getSession());
									} catch (Exception ex) {
									}
								}
							} else if (sessions.size() > maxSession) {
								for (int i = 0; i < (sessions.size() - maxSession); i++) {
									try {
										DefaultSmppSession session = sessions.remove(0);
										if (session != null) {
											try {
												session.unbind(smsConfig.getSMPP_CLIENT_BIND_TIMEOUT());
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
									} catch (Exception ex) {
									}
								}
							}
							for (DefaultSmppSession session : sessions) {
								if (!(session != null && session.isBound())) {
									faulty_sessions.add(session);
								}
							}
							sessions.removeAll(faulty_sessions);
							for (DefaultSmppSession faulty_session : faulty_sessions) {
								sessions.add(new SMPPClientSession(configuration, smsConfig).getSession());
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					faulty_sessions.clear();
					faulty_sessions = null;
				}
			}
		}, 0, smsConfig.getSMPP_CLIENT_ENQUIRELINK_TIMEOUT() / 2, TimeUnit.MILLISECONDS);
		logger.info("--SMPPClientManager initialization completed--");
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
//		if (session == null) {
//			session = new SMPPClientSession().getSession();
//		}
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

	@PreDestroy
	public synchronized void unbindAll() {
		if (!this.initialized) {
			return;
		}
		logger.info("--SMPPClientManager Deinitialization started--");
		synchronized (sessions) {
			for (DefaultSmppSession session : sessions) {
				if (session != null) {
					try {
						session.unbind(smsConfig.getSMPP_CLIENT_BIND_TIMEOUT());
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
		schedulerService.shutdown();
		logger.info("--SMPPClientManager Deinitialization completed--");
	}
}
