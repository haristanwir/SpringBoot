package com.esb.service.soap;

import java.util.List;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadValidatingInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableWs
@Configuration
public class SoapWebServiceConfiguration extends WsConfigurerAdapter {

	@Bean
	public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
		MessageDispatcherServlet messageDispatcherServlet = new MessageDispatcherServlet();
		messageDispatcherServlet.setApplicationContext(applicationContext);
		messageDispatcherServlet.setTransformWsdlLocations(true);
		return new ServletRegistrationBean<>(messageDispatcherServlet, "/springboot/EmployeeWS/Interface/*");
	}

	@Bean(name = "EmployeeWS")
	public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema employeeWSSchema) {
		DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
		wsdl11Definition.setPortTypeName("EmployeeWS");
		wsdl11Definition.setLocationUri("/springboot/EmployeeWS/Interface/");
		wsdl11Definition.setTargetNamespace(employeeWSSchema.getTargetNamespace() + "/Interface");
		wsdl11Definition.setSchema(employeeWSSchema);
		return wsdl11Definition;
	}

	@Bean
	public XsdSchema employeeWSSchema() {
		return new SimpleXsdSchema(new ClassPathResource("/soap/EmployeeWS.xsd"));
	}

	@Override
	public void addInterceptors(List<EndpointInterceptor> interceptors) {
		PayloadValidatingInterceptor validatingInterceptor = new PayloadValidatingInterceptor();
		validatingInterceptor.setValidateRequest(true);
		validatingInterceptor.setValidateResponse(true);
		validatingInterceptor.setXsdSchema(employeeWSSchema());
		interceptors.add(validatingInterceptor);
	}
}
