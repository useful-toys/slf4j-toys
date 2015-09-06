/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.usefultoys.slf4j.examples;

import java.util.Random;

import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.meter.MeterFactory;

/**
 *
 * @author Daniel Felix Ferber
 */
public class ExecutionFlowExample {

	static final Logger logger = LoggerFactory.getLogger("Essential");

	static final Random random = new Random(System.currentTimeMillis());

	int state = random.nextInt();

	public static void main(final String argv[]) {

		example1();	
		example2();	
		example3(1000);
		example3(9);
	}

	private static void example1() {
		final Meter m1 = MeterFactory.getMeter(logger, "example1").start();
		// Generate 1000 random numbers
		for (int i = 0 ; i < 1000 ; i++) {
			random.nextInt();
		}
		m1.ok();
	}

	private static void example2() {
		Meter m2 = MeterFactory.getMeter(logger, "example2").start();
		int value = random.nextInt();
		if ((value % 2) == 0) {
			// Generate even random numbers
			for (int i = 0 ; i < 1000 ; i++) {
				int number = random.nextInt(100) * 2;
			}		
			m2.ok("Even");
		} else {
			// Generate odd random numbers
			for (int i = 0 ; i < 1000 ; i++) {
				int number = random.nextInt(100) * 2 + 1;
			}		
			m2.ok("Odd");			
		}
	}
	
	private static void example3(int quantity) {
		Meter m3 = MeterFactory.getMeter(logger, "example3").start();
		if ((quantity % 2) == 0) {
			// Generate even random numbers
			for (int i = 0 ; i < 1000 ; i++) {
				int number = random.nextInt(100) * 2;
			}		
			m3.ok();
		} else {
			// Quantity was 'not found'
			m3.reject("NotFound");			
		}
	}
}
