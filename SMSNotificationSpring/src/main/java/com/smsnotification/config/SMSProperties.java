package com.smsnotification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
public class SMSProperties {

	@Value("${smpp.server.host}")
	private String SMPP_SERVER_HOST;
	@Value("${smpp.server.port}")
	private Integer SMPP_SERVER_PORT;
	@Value("${smpp.server.systemId}")
	private String SMPP_SERVER_SYSTEMID;
	@Value("${smpp.server.password}")
	private String SMPP_SERVER_PASSWORD;
	@Value("${smpp.client.bind.timeout}")
	private Long SMPP_CLIENT_BIND_TIMEOUT;
	@Value("${smpp.client.enquirelink.timeout}")
	private Long SMPP_CLIENT_ENQUIRELINK_TIMEOUT;
	@Value("${smpp.client.connection.timeout}")
	private Long SMPP_CLIENT_CONNECTION_TIMEOUT;
	@Value("${smpp.client.max.sessions}")
	private Integer SMPP_CLIENT_MAX_SESSIONS;
	@Value("${smpp.client.max.tps}")
	private Integer SMPP_CLIENT_MAX_TPS;
	@Value("${smpp.client.bind.type}")
	private String SMPP_CLIENT_BIND_TYPE;
	@Value("${smpp.client.system.type}")
	private String SMPP_CLIENT_SYSTEM_TYPE;
	@Value("${smpp.client.bind.ton}")
	private String SMPP_CLIENT_BIND_TON;
	@Value("${smpp.client.bind.npi}")
	private String SMPP_CLIENT_BIND_NPI;
	@Value("${smpp.client.delivery.flag}")
	private Boolean SMPP_CLIENT_DELIVERY_FLAG;
	@Value("${smpp.client.src.ton}")
	private String SMPP_CLIENT_SRC_TON;
	@Value("${smpp.client.dest.ton}")
	private String SMPP_CLIENT_DEST_TON;
	@Value("${smpp.client.src.npi}")
	private String SMPP_CLIENT_SRC_NPI;
	@Value("${smpp.client.dest.npi}")
	private String SMPP_CLIENT_DEST_NPI;
	@Value("${smpp.client.payload.flag}")
	private Boolean SMPP_CLIENT_PAYLOAD_FLAG;
	@Value("${smpp.client.multipart.sar.flag}")
	private Boolean SMPP_CLIENT_MULTIPART_SAR_FLAG;

}
