package com.tcpserver.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SocketHandler implements Runnable {
	private final Logger logger = LogManager.getLogger(SocketHandler.class.getName());
	private final Logger Errorlogger = LogManager.getLogger(ErrorHandling.class.getName());

	private Socket clientSocket = null;
	private BufferedReader bufferedReader = null;
	private BufferedWriter bufferedWriter = null;

	public SocketHandler(Socket clientSocket) throws IOException {
		super();
		this.clientSocket = clientSocket;
		this.bufferedReader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
		this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(this.clientSocket.getOutputStream()));
	}

	public void shutdown() {
		if (clientSocket != null && !clientSocket.isClosed()) {
			try {
				clientSocket.close();
			} catch (Exception ex) {
				Errorlogger.error(ErrorHandling.getStackTrace(ex));
			}
			clientSocket = null;
		}
	}

	@Override
	public void run() {
		while (clientSocket != null && !clientSocket.isClosed()) {
			try {
				String inString = bufferedReader.readLine();
				if (inString != null) {
					byte[] utf16Bytes = inString.getBytes();
					try {
						byte[] utf8Bytes = new byte[utf16Bytes.length];
						int i = 0;
						for (byte utf16byte : utf16Bytes) {
							if (utf16byte != (byte) 0x00) {
								utf8Bytes[i] = utf16byte;
								i++;
							}
						}
						inString = new String(utf8Bytes, "UTF-8").trim();
					} catch (Exception ex) {
						inString = new String(utf16Bytes, "UTF-16").trim();
					}
					logger.info("Incoming message: " + inString);
					bufferedWriter.write("ACK");
					bufferedWriter.flush();
				} else {
					shutdown();
				}
			} catch (Exception ex) {
				Errorlogger.error(ErrorHandling.getStackTrace(ex));
				shutdown();
			}
		}
	}
}
