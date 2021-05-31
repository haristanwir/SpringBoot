/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esb.utility;

/**
 *
 * @author Haris Tanwir
 */
public class ThroughputController {

	private Integer tps = null;
	private Long lastSecondStartTime = null;
	private Integer eventCounter = 0;
	private Boolean tpsEnabled = null;

	public ThroughputController(Integer tps) {
		if (tps > 0) {
			this.tps = (tps - 1 < 0) ? 0 : tps - 1;
			this.tpsEnabled = true;
		} else {
			this.tpsEnabled = false;
		}
	}

	public synchronized void evaluateTPS() throws InterruptedException {
		if (!tpsEnabled) {
			return;
		}
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
