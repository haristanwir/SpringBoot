package com.esb.msgflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.esb.msgflow.core.EmployeeMQInput;
import com.esb.msgflow.core.EmployeeTimer;
import com.esb.utility.JDBCConnectionPool;
import com.esb.utility.RabbitMQConsConnectionPool;
import com.esb.utility.RabbitMQProdConnectionPool;

@Configuration
public class ApplicationConfiguration {

	@Bean
	public RabbitMQConsConnectionPool mqConsumerPool(@Value("${rabbitmq.ip}") String mq_ip, @Value("${rabbitmq.port}") Integer mq_port) {
		RabbitMQConsConnectionPool mqConsumerPool = new RabbitMQConsConnectionPool(mq_ip, mq_port, ApplicationConfiguration.class.getName());
		return mqConsumerPool;
	}

	@Bean
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
		return new EmployeeTimer(timerID, timeoutSec);
	}

	@Bean
	public JDBCConnectionPool jdbcPool(@Value("${db.driver.name}") String db_drivername, @Value("${db.connection.string}") String db_url, @Value("${db.username}") String db_user, @Value("${db.password}") String db_password) {
		return new JDBCConnectionPool(db_drivername, db_url, db_user, db_password);
	}

}
