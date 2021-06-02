package com.esb.client.soap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;

@Configuration
public class SoapClientConfiguration {

	@Bean
	public Jaxb2Marshaller interactMarshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setPackagesToScan(new String[] { "com.unicacorp" });
		return marshaller;
	}

	@Bean
	public WebServiceTemplate wsTemplate() {
		WebServiceTemplate wsTemplate = new WebServiceTemplate(interactMarshaller());
		return wsTemplate;
	}

}
