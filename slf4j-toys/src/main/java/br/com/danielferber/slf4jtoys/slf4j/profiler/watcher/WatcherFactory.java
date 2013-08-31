/*
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.watcher;

import br.com.danielferber.slf4jtoys.slf4j.logger.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author Daniel Felix Ferber
 */
public final class WatcherFactory {

    private WatcherFactory() {
        // Impede instâncias
    }
    private static Watcher INSTANCE = new Watcher(LoggerFactory.getLogger("watcher"));

    public static Watcher getInstance() {
        return INSTANCE;
    }

    public static void setLogger(String name) {
        INSTANCE.setLogger(LoggerFactory.getLogger(name));
    }

    public static void setLogger(Class<?> clazz) {
        INSTANCE.setLogger(LoggerFactory.getLogger(clazz));
    }
}
