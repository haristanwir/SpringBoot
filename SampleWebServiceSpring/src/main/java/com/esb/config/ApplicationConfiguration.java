package com.esb.config;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.esb.msgflow.EmployeeMQInput;
import com.esb.msgflow.EmployeeTimer;
import com.esb.utility.ActiveMQProdConnectionPool;
import com.esb.utility.JDBCConnectionPool;
import com.esb.utility.RabbitMQConnection;
import com.esb.utility.RabbitMQProdConnectionPool;

@Configuration
public class ApplicationConfiguration {

	@Bean(destroyMethod = "shutdown")
	public ActiveMQProdConnectionPool activeMQProducerPool(@Value("${activemq.username}") String mq_username, @Value("${activemq.password}") String mq_password, @Value("${activemq.ip}") String mq_ip, @Value("${activemq.port}") Integer mq_port) throws Exception {
		ActiveMQProdConnectionPool activeMQProducerPool = new ActiveMQProdConnectionPool(mq_username, mq_password, mq_ip, mq_port);
		return activeMQProducerPool;
	}

	@Bean(destroyMethod = "shutdown")
	public RabbitMQConnection mqConnection(@Value("${rabbitmq.ip}") String mq_ip, @Value("${rabbitmq.port}") Integer mq_port) throws IOException, TimeoutException {
		RabbitMQConnection mqConnection = new RabbitMQConnection(mq_ip, mq_port);
		return mqConnection;
	}

	@Bean(destroyMethod = "shutdown")
	public RabbitMQProdConnectionPool mqProducerPool(@Value("${rabbitmq.ip}") String mq_ip, @Value("${rabbitmq.port}") Integer mq_port) {
		RabbitMQProdConnectionPool mqProducerPool = new RabbitMQProdConnectionPool(mq_ip, mq_port, ApplicationConfiguration.class.getName());
		return mqProducerPool;
	}

	@Bean
	public EmployeeMQInput employeeMQInput(@Value("${EmployeeMQInput.flow.pool.size}") Integer poolSize, @Value("${EmployeeMQInput.flow.tps}") Integer tps) {
		return new EmployeeMQInput("EMPLOYEE", poolSize, tps);
	}

	@Bean
	public EmployeeTimer employeeTimer(@Value("${EmployeeTimer.flow.timerid}") String timerID, @Value("${EmployeeTimer.flow.timeout.second}") Integer timeoutSec) {
		return new EmployeeTimer("EMPLOYEE", timerID, timeoutSec);
	}

	@Bean(destroyMethod = "shutdown")
	public JDBCConnectionPool jdbcPool(@Value("${db.driver.name}") String db_drivername, @Value("${db.connection.string}") String db_url, @Value("${db.username}") String db_user, @Value("${db.password}") String db_password) {
		return new JDBCConnectionPool(db_drivername, db_url, db_user, db_password);
	}

}
