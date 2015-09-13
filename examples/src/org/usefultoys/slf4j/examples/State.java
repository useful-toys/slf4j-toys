/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.usefultoys.slf4j.examples;

import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.meter.MeterFactory;

/**
 *
 * @author Daniel Felix Ferber
 */
public class State {

    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    }

    public static void main(final String argv[]) {
        example1(2, 3);
    }

    private static void example1(int a, int b) {
        final Logger logger = LoggerFactory.getLogger("example");
        final Meter m1 = MeterFactory.getMeter(logger, "operation1")
                .ctx("a", a).ctx("b", b).start();
        // ...
        int sum = a + b;
        // ...

        m1.ctx("sum", sum).ok();
    }
}
