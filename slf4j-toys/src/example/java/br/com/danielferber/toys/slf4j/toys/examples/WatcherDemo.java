/*
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.watcher;

import br.com.danielferber.slf4jtoys.slf4j.profiler.ProfilingSession;
import br.com.danielferber.slf4jtoys.slf4j.profiler.meter.Meter;
import br.com.danielferber.slf4jtoys.slf4j.profiler.meter.MeterFactory;
import java.util.Random;

/**
 *
 * @author x7ws - Daniel Felix Ferber
 */
public class WatcherDemo {

    static Random random = new Random(System.currentTimeMillis());
            
    public static void main(String[] args) {
        ProfilingSession.startExecutor();
        ProfilingSession.startWatcher();
        
        Meter m = MeterFactory.getMeter(WatcherDemo.class).start();
        
        try {
            iterationMeter();

            m.ok();
        } catch (Exception ex) {
            m.fail(ex);
        }
        
        ProfilingSession.stopWatcher();
        ProfilingSession.stopExecutor();
    }

    private static void iterationMeter() {
        Meter m = MeterFactory.getMeter("iteration").m("Execute 1000 iterations").start();
        try {
            for (int i = 0; i < 1000; i++) {
                Thread.sleep(10+random.nextInt(10));
                m.inc();
            }
            m.ok();
        } catch (Exception e) {
            m.fail(e);
        }
    }
}
