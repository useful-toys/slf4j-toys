/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.danielferber.slf4jtoys.slf4j.demo;

import org.junit.Test;

import br.com.danielferber.slf4jtoys.slf4j.profiler.meter.Meter;
import br.com.danielferber.slf4jtoys.slf4j.profiler.meter.MeterFactory;

/**
 *
 * @author X7WS
 */
public class ProgressTest {
	@Test
	public void testFastProgress() {
		final Meter m = MeterFactory.getMeter("teste").iterations(20).start();
		for (int i = 0; i < 20; i++) {
			m.inc().progress();
			try {
				Thread.sleep(500);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		m.ok();
	}

	@Test
	public void testFastProgressLargeMeterProgressPeriod() {
		System.setProperty("meter.progress.period", "5s");
		final Meter m = MeterFactory.getMeter("teste").iterations(20).start();
		for (int i = 0; i < 20; i++) {
			m.inc().progress();
			try {
				Thread.sleep(500);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		m.ok();
	}

	@Test
	public void testFastProgressSmallMeterProgressPeriod() {
		System.setProperty("meter.progress.period", "500ms");
		final Meter m = MeterFactory.getMeter("teste").iterations(20).start();
		for (int i = 0; i < 20; i++) {
			m.inc().progress();
			try {
				Thread.sleep(500);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		m.ok();
	}
}
