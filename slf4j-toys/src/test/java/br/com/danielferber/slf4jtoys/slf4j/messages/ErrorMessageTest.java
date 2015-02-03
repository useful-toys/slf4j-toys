/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.danielferber.slf4jtoys.slf4j.messages;

import br.com.danielferber.slf4jtoys.slf4j.logger.LoggerFactory;
import br.com.danielferber.slf4jtoys.slf4j.profiler.meter.Meter;
import br.com.danielferber.slf4jtoys.slf4j.profiler.meter.MeterFactory;
import org.junit.Test;

/**
 *
 * @author X7WS
 */
public class ErrorMessageTest {

    @Test
    public void testMeterConfirmedButNotStarted() {
        final Meter m = MeterFactory.getMeter("teste");
        m.ok();
    }

    @Test
    public void testMeterAlreadyRefusedOrConfirmed1() {
        final Meter m = MeterFactory.getMeter("teste").start();
        m.ok();
        m.ok();
    }

    @Test
    public void testMeterAlreadyRefusedOrConfirmed2() {
        final Meter m = MeterFactory.getMeter("teste").start();
        m.ok();
        m.fail();
    }

    @Test
    public void testMeterRefusedButNotStarted() {
        final Meter m = MeterFactory.getMeter("teste");
        m.fail();
    }

    @Test
    public void testMeterAlreadyRefusedOrConfirmed3() {
        final Meter m = MeterFactory.getMeter("teste").start();
        m.fail();
        m.fail();
    }

    @Test
    public void testMeterAlreadyRefusedOrConfirmed4() {
        final Meter m = MeterFactory.getMeter("teste").start();
        m.fail();
        m.ok();
    }

    @Test
    public void testMeterAlreadyStarted() {
        final Meter m = MeterFactory.getMeter("teste").start().start();
        m.ok();
    }

    @Test
    public void testMeterNotRefusedNorConfirmed() throws InterruptedException {
        subMeterX();
        // Wait and force garbage colletor to finalize meter
        System.gc();
        Thread.sleep(1000);
        System.gc();
        Thread.sleep(1000);
        System.gc();
    }

    private void subMeterX() {
        final Meter m = MeterFactory.getMeter("teste").start();
    }

    @Test
    public void testMeterInternalException() {
        final Meter m = new Meter(LoggerFactory.getLogger("teste")) {
            @Override
            protected void collectSystemStatus() {
                throw new RuntimeException();
            }
          
        };
        m.start();
        m.ok();
    }

}
