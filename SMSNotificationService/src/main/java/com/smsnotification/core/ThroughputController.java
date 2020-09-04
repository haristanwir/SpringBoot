/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smsnotification.core;

/**
 *
 * @author Haris Tanwir
 */
public class ThroughputController {

	private Integer tps = null;
	private Long lastSecondStartTime = null;
	private Integer eventCounter = 0;

	public ThroughputController(Integer tps) {
		this.tps = (tps - 1 < 0) ? 0 : tps - 1;
	}

	public synchronized void evaluateTPS() throws InterruptedException {
		if (lastSecondStartTime == null) {
			lastSecondStartTime = System.currentTimeMillis();
		}
		if (eventCounter >= tps) {
			if (!(((System.currentTimeMillis() - lastSecondStartTime) / 1000.0) > 1.0)) {
				Thread.sleep(1000 - (System.currentTimeMillis() - lastSecondStartTime));
				lastSecondStartTime = System.currentTimeMillis();
				eventCounter = 0;
				return;
			}
		}
		if ((((System.currentTimeMillis() - lastSecondStartTime) / 1000.0) > 1.0)) {
			lastSecondStartTime = System.currentTimeMillis();
			eventCounter = 0;
		}
		eventCounter++;
	}
}
