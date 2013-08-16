/*
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.watcher;

import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.Session;

/**
 *
 * @author x7ws - Daniel Felix Ferber
 */
public class WatcherDemo {

    public static void main(String[] args) {
        Watcher watcher = WatcherFactory.getWatcher("watcher").start();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        watcher.stop();
        Session.timer.cancel();
    }
}
