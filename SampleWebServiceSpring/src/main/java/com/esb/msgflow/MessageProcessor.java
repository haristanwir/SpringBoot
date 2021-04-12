package com.esb.msgflow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esb.utility.ErrorHandling;
import com.esb.utility.JDBCConnectionPool;
import com.esb.utility.RabbitMQProdConnectionPool;

@Service
public class MessageProcessor {

	@Autowired
	private JDBCConnectionPool jdbcPool;

	@Autowired
	private RabbitMQProdConnectionPool mqProducerPool;

	private final Logger logger = LogManager.getLogger(MessageProcessor.class.getName());
	private final Logger Errorlogger = LogManager.getLogger(ErrorHandling.class.getName());

	public void processMessage(String message) throws Exception {
		Connection connection = jdbcPool.getConnection();
		try {
			long threadStart = System.currentTimeMillis();
			logger.info("Message Received message:" + message);
			String sql = "SELECT * FROM CX_SMS_NTFY_TMP WHERE rownum < 10";
			PreparedStatement statement = connection.prepareStatement(sql);
			ResultSet rs = statement.executeQuery();
			String msisdn = null;
			while (rs.next()) {
				msisdn = rs.getString("MSISDN");
			}
			logger.info("msisdn: " + msisdn);
			try {
				rs.close();
			} catch (Exception ex) {
			}
			try {
				statement.close();
			} catch (Exception ex) {
			}
			logger.info("DB time is " + (System.currentTimeMillis() - threadStart));
		} catch (Exception ex) {
			throw ex;
		} finally {
			jdbcPool.releaseConnection(connection);
		}
	}
}
