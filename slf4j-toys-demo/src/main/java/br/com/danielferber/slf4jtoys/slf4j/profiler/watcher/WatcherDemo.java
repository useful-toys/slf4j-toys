/*
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.watcher;

import br.com.danielferber.slf4jtoys.slf4j.profiler.ProfilingSession;

/**
 *
 * @author x7ws - Daniel Felix Ferber
 */
public class WatcherDemo {

    public static void main(String[] args) {
        ProfilingSession.startWatcher();
        try {
            Thread.sleep(15000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        ProfilingSession.stopWatcher();
    }
}
