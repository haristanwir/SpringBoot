package com.tcpserver.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TCPServer extends Thread {

	private final Logger logger = LogManager.getLogger(TCPServer.class.getName());
	private final Logger Errorlogger = LogManager.getLogger(ErrorHandling.class.getName());

	private ServerSocket serverSocket = null;
	private Integer port = null;
	private ExecutorService executorService = null;
	private Boolean isInitialized = false;
	private ArrayList<SocketHandler> socketList = new ArrayList<SocketHandler>();

	public TCPServer(Integer port) throws IOException {
		super();
		this.port = port;
		this.serverSocket = new ServerSocket(this.port);
		this.executorService = Executors.newCachedThreadPool();
	}

	@PostConstruct
	public synchronized void init() {
		if (isInitialized) {
			return;
		}
		this.start();
		isInitialized = true;
	}

	@PreDestroy
	public synchronized void shutdown() {
		if (!isInitialized) {
			return;
		}
		executorService.shutdown();
		for (SocketHandler socketHandler : socketList) {
			socketHandler.shutdown();
		}
		isInitialized = false;
	}

	@Override
	public void run() {
		Socket clientSocket = null;
		while (isInitialized) {
			clientSocket = null;
			try {
				clientSocket = serverSocket.accept();
			} catch (Exception ex) {
				Errorlogger.error(ErrorHandling.getStackTrace(ex));
			}
			if (isInitialized && clientSocket != null) {
				try {
					SocketHandler socketHandler = new SocketHandler(clientSocket);
					socketList.add(socketHandler);
					executorService.execute(socketHandler);
				} catch (Exception ex) {
					Errorlogger.error(ErrorHandling.getStackTrace(ex));
				}
			}
		}
	}
}
