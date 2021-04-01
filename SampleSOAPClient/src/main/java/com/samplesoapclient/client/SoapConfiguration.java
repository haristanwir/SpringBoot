package com.samplesoapclient.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;

@Configuration
public class SoapConfiguration {

	@Bean
	public Jaxb2Marshaller marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setPackagesToScan(new String[] { "com.esb", "com.unicacorp" });
		return marshaller;
	}

	@Bean
	public WebServiceTemplate wsTemplate() {
		WebServiceTemplate wsTemplate = new WebServiceTemplate(marshaller());
		return wsTemplate;
	}

}
