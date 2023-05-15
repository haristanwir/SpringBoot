/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esb.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Haris Tanwir
 */
public class JDBCConnectionPool {

	private final Logger logger = LogManager.getLogger(JDBCConnectionPool.class.getName());
	private final Logger Errorlogger = LogManager.getLogger(ErrorHandling.class.getName());

	private String drivername = null;
	private String url = null;
	private String user = null;
	private String password = null;
	private Integer poolSize = 0;
	private Long connectionWait = 500L;
	private Long connectionEvictionTimeout = (long) (1000 * 60 * 5);
	private ScheduledExecutorService schedulerService = Executors.newSingleThreadScheduledExecutor();
	private List<InnerConnection> connectionPool = null;

	public JDBCConnectionPool(String drivername, String url, String user, String password) {
		this.drivername = drivername;
		this.url = url;
		this.user = user;
		this.password = password;
		this.connectionPool = new ArrayList<InnerConnection>();
		schedulerService.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					if (!connectionPool.isEmpty()) {
						synchronized (connectionPool) {
							if (!connectionPool.isEmpty()) {
								ArrayList<InnerConnection> invalidConnections = new ArrayList<InnerConnection>();
								for (InnerConnection innerConnection : connectionPool) {
									Long timestamp = innerConnection.getTimestamp();
									if (timestamp < System.currentTimeMillis() - connectionEvictionTimeout) {
										invalidConnections.add(innerConnection);
									}
								}
								if (!invalidConnections.isEmpty()) {
									int oldpoolsize = connectionPool.size();
									connectionPool.removeAll(invalidConnections);
									int newpoolsize = connectionPool.size();
									for (InnerConnection innerConnection : invalidConnections) {
										Connection connection = innerConnection.getConnection();
										try {
											connection.close();
										} catch (SQLException ex) {
										}
										connection = null;
									}
									if (newpoolsize < oldpoolsize) {
										logger.info("DB Connections closed:" + invalidConnections.size());
									}
								}
							}
						}
					}
				} catch (Exception ex) {
					logger.error(ErrorHandling.getMessage(ex));
					Errorlogger.error(ErrorHandling.getStackTrace(ex));
				}
			}
		}, 0, connectionEvictionTimeout + (1000 * 60), TimeUnit.MILLISECONDS);
	}

//	public JDBCConnectionPool(String drivername, String url, String user, String password, Integer poolSize) {
//		this.drivername = drivername;
//		this.url = url;
//		this.user = user;
//		this.password = password;
//		if (poolSize > this.poolSize) {
//			this.poolSize = poolSize;
//		}
//		this.connectionPool = new ArrayList<Connection>();
//		for (int i = 0; i < this.poolSize; i++) {
//			Connection connection = createConnection(drivername, url, user, password);
//			if (connection != null) {
//				connectionPool.add(connection);
//			}
//		}
//	}

	public Connection getConnection() {
		Connection connection = null;
		if (!connectionPool.isEmpty()) {
			synchronized (connectionPool) {
				if (!connectionPool.isEmpty()) {
					InnerConnection innerConnection = connectionPool.remove(0);
					connection = innerConnection != null ? innerConnection.getConnection() : null;
				} else {
					connection = createConnection(drivername, url, user, password);
				}
			}
		} else {
			if (poolSize == 0) {
				connection = createConnection(drivername, url, user, password);
			} else {
				while (connection == null) {
					try {
						Thread.sleep(connectionWait);
					} catch (InterruptedException ex) {
					}
					if (!connectionPool.isEmpty()) {
						synchronized (connectionPool) {
							if (!connectionPool.isEmpty()) {
								InnerConnection innerConnection = connectionPool.remove(0);
								connection = innerConnection != null ? innerConnection.getConnection() : null;
							} else {
								connection = createConnection(drivername, url, user, password);
							}
						}
					}
				}
			}
		}
		if (connection == null) {
			connection = createConnection(drivername, url, user, password);
		}
		try {
			if (!connection.isValid(0)) {
				try {
					connection.close();
				} catch (SQLException ex) {
				}
				connection = null;
				connection = createConnection(drivername, url, user, password);
			}
		} catch (Exception ex) {
		}
		return connection;
	}

	public boolean releaseConnection(Connection connection) {
		if (connection != null) {
			synchronized (connectionPool) {
//				return connectionPool.add(connection);
				connectionPool.add(0, new InnerConnection(connection, System.currentTimeMillis()));
				return true;
			}
		} else {
			return false;
		}
	}

	private Connection createConnection(String drivername, String url, String user, String password) {
		Connection connection = null;
		try {
			Class.forName(drivername);
			connection = DriverManager.getConnection(url, user, password);
		} catch (SQLException ex) {
			logger.error(ErrorHandling.getMessage(ex));
			Errorlogger.error(ErrorHandling.getStackTrace(ex));
		} catch (ClassNotFoundException ex) {
			logger.error(ErrorHandling.getMessage(ex));
			Errorlogger.error(ErrorHandling.getStackTrace(ex));
		}
		return connection;
	}

	public void shutdown() {
		synchronized (connectionPool) {
			for (InnerConnection innerConnection : connectionPool) {
				Connection connection = innerConnection.getConnection();
				try {
					connection.close();
				} catch (SQLException ex) {
				}
				connection = null;
				innerConnection = null;
			}
			try {
				connectionPool.clear();
			} catch (Exception ex) {
			}
		}
//		schedulerService.shutdownNow();
	}

	public int getSize() {
		return connectionPool.size();
	}

	public String getDrivername() {
		return drivername;
	}

	public Long getConnectionWait() {
		return connectionWait;
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public List<InnerConnection> getConnectionPool() {
		return connectionPool;
	}

	private class InnerConnection {
		private Connection connection = null;
		private Long timestamp = null;

		public InnerConnection(Connection connection, Long timestamp) {
			super();
			this.connection = connection;
			this.timestamp = timestamp;
		}

		public Connection getConnection() {
			return connection;
		}

		public void setConnection(Connection connection) {
			this.connection = connection;
		}

		public Long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(Long timestamp) {
			this.timestamp = timestamp;
		}
	}

}