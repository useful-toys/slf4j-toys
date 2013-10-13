/*
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.watcher;

import br.com.danielferber.slf4jtoys.slf4j.profiler.ProfilingSession;
import br.com.danielferber.slf4jtoys.slf4j.profiler.meter.Meter;
import br.com.danielferber.slf4jtoys.slf4j.profiler.meter.MeterFactory;

/**
 *
 * @author x7ws - Daniel Felix Ferber
 */
public class WatcherDemo {

    public static void main(String[] args) {
        ProfilingSession.startExecutor();
        ProfilingSession.startWatcher();
        
        Meter m = MeterFactory.getMeter(WatcherDemo.class).start();
        
        try {
            Thread.sleep(5000);
            Meter m2 = m.sub("task1").start();
            try {
                throw new IllegalStateException();
            } catch (Exception e) {
                m2.fail(e);
            }
            m.ok();
        } catch (InterruptedException ex) {
            m.fail(ex);
        }
        
        ProfilingSession.stopWatcher();
        ProfilingSession.stopExecutor();
    }
}
