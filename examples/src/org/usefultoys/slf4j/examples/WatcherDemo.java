/*
 */
package org.usefultoys.slf4j.examples;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.usefultoys.slf4j.ProfilingSession;
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.meter.MeterFactory;

/**
 *
 * @author Daniel Felix Ferber
 */
public class WatcherDemo {

    static Random random = new Random(System.currentTimeMillis());

    public static void main(final String[] args) {
        System.setProperty("profiler.usePlatformManagedBean", "true");
        System.setProperty("watcher.period", "1s");
        System.setProperty("watcher.delay", "1s");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");

        ProfilingSession.startExecutor();
        ProfilingSession.startWatcher();
        ProfilingSession.logReport();

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = Integer.MIN_VALUE;
                while (!Thread.interrupted()) {
                    i++;
                }
            }
        });
        t1.setDaemon(true);
        t1.start();

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = Integer.MIN_VALUE;
                while (!Thread.interrupted()) {
                    i++;
                }
            }
        });
        t2.setDaemon(true);
        t2.start();

        try {
            Thread.sleep(3000L);
        } catch (InterruptedException ex) {
            Logger.getLogger(WatcherDemo.class.getName()).log(Level.SEVERE, null, ex);
        }

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
                Thread.sleep(500 + random.nextInt(500));
//                m.inc().progress();
            }
            m.ok();
        } catch (final Exception e) {
            m.fail(e);
        }
    }
}
