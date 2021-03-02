package com.smsnotification.utility;

import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cloudhopper.smpp.SmppConstants;

public class Utility {

	private static final Logger logger = LogManager.getLogger(Utility.class.getName());
	public static Properties properties = null;

	private synchronized static void loadProperties() throws Exception {
		properties = new Properties();
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			InputStream inputStream = classLoader.getResourceAsStream(Constant.CONFIG_FILE_NAME);
			properties.load(inputStream);
		} catch (Exception e) {
			logger.error("Prop: loading failed", e);
			throw e;
		} finally {
		}
	}

	public static String getProperty(String propertyName) {
		String propertyValue = null;
		if (Constant.isProd) {
			try {
				propertyValue = System.getenv(propertyName);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (propertyValue == null) {
				logger.error("Prop: " + propertyName + " not found");
			}
		} else {
			try {
				if (properties == null) {
					Utility.loadProperties();
				}
				propertyValue = properties.getProperty(propertyName);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (propertyValue == null) {
					logger.error("Prop: " + propertyName + " not found");
				}
			}
		}
		return propertyValue;
	}

	public static Long getJavaMillis() {
		return System.currentTimeMillis();
	}

	public static byte getTONByteValue(String stringTON) {
		byte byteTON = SmppConstants.TON_UNKNOWN;
		if (stringTON.equalsIgnoreCase("TON_ABBREVIATED")) {
			byteTON = SmppConstants.TON_ABBREVIATED;
		} else if (stringTON.equalsIgnoreCase("TON_ALPHANUMERIC")) {
			byteTON = SmppConstants.TON_ALPHANUMERIC;
		} else if (stringTON.equalsIgnoreCase("TON_INTERNATIONAL")) {
			byteTON = SmppConstants.TON_INTERNATIONAL;
		} else if (stringTON.equalsIgnoreCase("TON_NATIONAL")) {
			byteTON = SmppConstants.TON_NATIONAL;
		} else if (stringTON.equalsIgnoreCase("TON_NETWORK")) {
			byteTON = SmppConstants.TON_NETWORK;
		} else if (stringTON.equalsIgnoreCase("TON_RESERVED_EXTN")) {
			byteTON = SmppConstants.TON_RESERVED_EXTN;
		} else if (stringTON.equalsIgnoreCase("TON_SUBSCRIBER")) {
			byteTON = SmppConstants.TON_SUBSCRIBER;
		} else if (stringTON.equalsIgnoreCase("TON_UNKNOWN")) {
			byteTON = SmppConstants.TON_UNKNOWN;
		}
		return byteTON;
	}

	public static byte getNPIByteValue(String stringNPI) {
		byte byteNPI = SmppConstants.NPI_UNKNOWN;
		if (stringNPI.equalsIgnoreCase("NPI_E164")) {
			byteNPI = SmppConstants.NPI_E164;
		} else if (stringNPI.equalsIgnoreCase("NPI_ERMES")) {
			byteNPI = SmppConstants.NPI_ERMES;
		} else if (stringNPI.equalsIgnoreCase("NPI_INTERNET")) {
			byteNPI = SmppConstants.NPI_INTERNET;
		} else if (stringNPI.equalsIgnoreCase("NPI_ISDN")) {
			byteNPI = SmppConstants.NPI_ISDN;
		} else if (stringNPI.equalsIgnoreCase("NPI_LAND_MOBILE")) {
			byteNPI = SmppConstants.NPI_LAND_MOBILE;
		} else if (stringNPI.equalsIgnoreCase("NPI_NATIONAL")) {
			byteNPI = SmppConstants.NPI_NATIONAL;
		} else if (stringNPI.equalsIgnoreCase("NPI_PRIVATE")) {
			byteNPI = SmppConstants.NPI_PRIVATE;
		} else if (stringNPI.equalsIgnoreCase("NPI_TELEX")) {
			byteNPI = SmppConstants.NPI_TELEX;
		} else if (stringNPI.equalsIgnoreCase("NPI_UNKNOWN")) {
			byteNPI = SmppConstants.NPI_UNKNOWN;
		} else if (stringNPI.equalsIgnoreCase("NPI_WAP_CLIENT_ID")) {
			byteNPI = SmppConstants.NPI_WAP_CLIENT_ID;
		} else if (stringNPI.equalsIgnoreCase("NPI_X121")) {
			byteNPI = SmppConstants.NPI_X121;
		}
		return byteNPI;
	}

}