/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.danielferber.toys.slf4j.toys.examples;

import java.util.Random;

import org.slf4j.Logger;

import br.com.danielferber.slf4jtoys.slf4j.logger.LoggerFactory;
import br.com.danielferber.slf4jtoys.slf4j.profiler.meter.Meter;
import br.com.danielferber.slf4jtoys.slf4j.profiler.meter.MeterFactory;

/**
 *
 * @author Daniel Felix Ferber
 */
public class MeterExample1 {

	static final Logger logger = LoggerFactory.getLogger("MeterExample1");

	static final Random random = new Random(System.currentTimeMillis());

	int state = random.nextInt();

	public static void main(final String argv[]) {
		new MeterExample1().run();
	}

	private void run() {
		example1();
		example2(random.nextInt());
		example3();
		example4();
		example5();
		example6();
	}


	private void example1() {
		/*
		 * Simpliest Meter workflow. Create Meter based on logger and suffix
		 * that identifies the operation. Start it. Run stuff. Confirm success
		 * or report failure.
		 */
		final Meter m = MeterFactory.getMeter(logger, "example1").start();
		try {
			runStuff();
			m.ok();
		} catch (final RuntimeException e) {
			m.fail(e);
		}
	}

	private void example2(final int param) {
		/*
		 * Meter workflow with context given by properties at startup. Create
		 * Meter based on logger and suffix that identifies the operation. Set
		 * one or more properties that describe the context where stuff will
		 * run, then start it. Run stuff. Confirm success or report failure.
		 */
		final Meter m = MeterFactory.getMeter(logger, "example2").ctx("param", param)
				.ctx("state", state).start();
		try {
			runStuff();
			m.ok();
		} catch (final RuntimeException e) {
			m.fail(e);
		}
	}

	private void example3() {
		/*
		 * Meter workflow with context given by a label at startup. Create Meter
		 * based on logger and suffix that identifies the operation. This might
		 * be less verbose for properties with self describing values. Set
		 * label, then start it. Run stuff. Confirm success or report failure.
		 */
		final Meter m = MeterFactory.getMeter(logger, "example3");
		if (random.nextBoolean()) {

			m.ctx("turnRight").start();
			try {
				runStuff();
				m.ok();
			} catch (final RuntimeException e) {
				m.fail(e);
			}
		} else {
			m.ctx("turnLeft").start();
			try {
				runStuff();
				m.ok();
			} catch (final RuntimeException e) {
				m.fail(e);
			}
		}
	}

	private void example4() {
		/*
		 * Meter workflow with context given by properties or label during
		 * execution. Create Meter based on logger and suffix that identifies
		 * the operation. Start it. Run stuff and set properties as needed.
		 * Confirm success or report failure.
		 */
		final Meter m = MeterFactory.getMeter(logger, "example4").start();
		try {
			runStuff();
			if (random.nextBoolean()) {
				m.ctx("turnLeft");
			} else {
				m.ctx("turnRight");
			}
			runStuff();
			m.ctx("value", random.nextInt());
			runStuff();
			m.ok();
		} catch (final RuntimeException e) {
			m.fail(e);
		}
	}

	private void example5() {
		/*
		 * Meter workflow with context given by properties or label as execution
		 * result. Create Meter based on logger and suffix that identifies the
		 * operation. Start it. Run stuff. Set context according to execution
		 * result before confirming success. Or report failure.
		 */
		final Meter m = MeterFactory.getMeter(logger, "example5").start();
		try {
			runStuff();
			final int value = runStuff();

			m.ctx("value", value).ok();
		} catch (final RuntimeException e) {
			m.fail(e);
		}
	}

	private int runStuff() {
		try {
			Thread.sleep(random.nextInt(200));
		} catch (final InterruptedException ex) {
		}
		return random.nextInt();
	}

	private void example6() {
		/*
		 *
		 */
		final Meter m = MeterFactory.getMeter(logger, "example1").iterations(10).start();
		try {
			for (int i = 0; i < 10; i++) {
				m.inc();
				m.progress();
			}
			m.ok();
		} catch (final RuntimeException e) {
			m.fail(e);
		}
	}

}
