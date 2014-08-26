/*
 */
package br.com.danielferber.toys.slf4j.toys.examples;

import java.util.Random;

import br.com.danielferber.slf4jtoys.slf4j.profiler.ProfilingSession;
import br.com.danielferber.slf4jtoys.slf4j.profiler.meter.Meter;
import br.com.danielferber.slf4jtoys.slf4j.profiler.meter.MeterFactory;

/**
 *
 * @author x7ws - Daniel Felix Ferber
 */
public class WatcherDemo {

    static Random random = new Random(System.currentTimeMillis());

    public static void main(final String[] args) {
        ProfilingSession.startExecutor();
        ProfilingSession.startWatcher();

        final Meter m = MeterFactory.getMeter(WatcherDemo.class).start();

        try {
            iterationMeter();

            m.ok();
        } catch (final Exception ex) {
            m.fail(ex);
        }

        ProfilingSession.stopWatcher();
        ProfilingSession.stopExecutor();
    }

    private static void iterationMeter() {
        final Meter m = MeterFactory.getMeter("iteration").m("Execute 1000 iterations").start();
        try {
            for (int i = 0; i < 1000; i++) {
                Thread.sleep(10+random.nextInt(10));
                m.inc().progress();
            }
            m.ok();
        } catch (final Exception e) {
            m.fail(e);
        }
    }
}
