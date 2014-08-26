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
public class TimeLimitTest {

    @Test
    public void testFast() {
        final Meter m = MeterFactory.getMeter("teste").limitMilliseconds(2000).start();
        try {
            Thread.sleep(500);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        m.ok();
    }
    
    @Test
    public void testSlow() {
        final Meter m = MeterFactory.getMeter("teste").limitMilliseconds(2000).start();
        try {
            Thread.sleep(3000);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        m.ok();
    }

    
}
