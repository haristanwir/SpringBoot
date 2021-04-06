package com.smsnotification.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.type.Address;
import com.smsnotification.utility.Utility;

@Configuration
public class SMSNotificationConfiguration {

	@Autowired
	private SMSProperties smsConfig;

	@Autowired
	private Utility utility;

	@Bean
	public SmppSessionConfiguration configuration() {
		SmppSessionConfiguration configuration = new SmppSessionConfiguration();
		configuration.setHost(smsConfig.getSMPP_SERVER_HOST());
		configuration.setPort(smsConfig.getSMPP_SERVER_PORT());
		configuration.setSystemId(smsConfig.getSMPP_SERVER_SYSTEMID());
		configuration.setPassword(smsConfig.getSMPP_SERVER_PASSWORD());
		String bindType = smsConfig.getSMPP_CLIENT_BIND_TYPE();
		if (bindType.equalsIgnoreCase("TX")) {
			configuration.setType(SmppBindType.TRANSMITTER);
		} else if (bindType.equalsIgnoreCase("RX")) {
			configuration.setType(SmppBindType.RECEIVER);
		} else {
			configuration.setType(SmppBindType.TRANSCEIVER);
		}
		configuration.setInterfaceVersion(SmppConstants.VERSION_3_4);
		configuration.setBindTimeout(smsConfig.getSMPP_CLIENT_BIND_TIMEOUT());
		configuration.setConnectTimeout(smsConfig.getSMPP_CLIENT_BIND_TIMEOUT());
		configuration.setWindowSize(smsConfig.getSMPP_CLIENT_MAX_TPS());
		configuration.setRequestExpiryTimeout(smsConfig.getSMPP_CLIENT_BIND_TIMEOUT());
		configuration.setSystemType(smsConfig.getSMPP_CLIENT_SYSTEM_TYPE());
		byte bindTON = utility.getTONByteValue(smsConfig.getSMPP_CLIENT_BIND_TON());
		byte bindNPI = utility.getNPIByteValue(smsConfig.getSMPP_CLIENT_BIND_NPI());
		configuration.setAddressRange(new Address(bindTON, bindNPI, null));
		configuration.setWindowWaitTimeout(configuration.getRequestExpiryTimeout());
		configuration.getLoggingOptions().setLogBytes(false);
		configuration.getLoggingOptions().setLogPdu(false);
		configuration.setCountersEnabled(false);
		return configuration;
	}

}
