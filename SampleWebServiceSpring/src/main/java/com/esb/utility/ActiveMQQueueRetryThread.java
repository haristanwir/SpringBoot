package com.esb.utility;

public class ActiveMQQueueRetryThread implements Runnable {
	
	private ActiveMQProdConnectionPool activeMQProducerPool = null;
	private String queueName = null;
	private String message = null;
	private Integer priority = null;
	private Integer persistance = null;
	private Long retryInterval = null;

	public ActiveMQQueueRetryThread(ActiveMQProdConnectionPool activeMQProducerPool, String queueName, String message, Integer persistance, Integer priority, Long retryInterval) {
		this.activeMQProducerPool = activeMQProducerPool;
		this.queueName = queueName;
		this.message = message;
		this.persistance = persistance;
		this.priority = priority;
		this.retryInterval = retryInterval;
	}

	@Override
	public void run() {
		boolean isQueued = false;
		while (!isQueued) {
			try {
				isQueued = activeMQProducerPool.enqueue(message, queueName, persistance, priority);
			} catch (Exception ex) {
			}
			if (!isQueued) {
				try {
					Thread.sleep(retryInterval);
				} catch (Exception ex) {
				}
			}
		}
	}
}