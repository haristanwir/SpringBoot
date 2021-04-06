package com.smsnotification.utility;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.cloudhopper.smpp.SmppConstants;

@Component
public class Utility {

	private final Logger logger = LogManager.getLogger(Utility.class.getName());

	public Long getJavaMillis() {
		return System.currentTimeMillis();
	}

	public byte getTONByteValue(String stringTON) {
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

	public byte getNPIByteValue(String stringNPI) {
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