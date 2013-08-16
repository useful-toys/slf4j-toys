/*
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.watcher;

import br.com.danielferber.slf4jtoys.slf4j.logger.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author x7ws - Daniel Felix Ferber
 */
public final class WatcherFactory {

    private WatcherFactory() {
        // Impede instâncias
    }

    public static Watcher getWatcher(final String name) {
        return new Watcher(LoggerFactory.getLogger(name));
    }

    public static Watcher getWatcher(final Class<?> clazz) {
        return new Watcher(LoggerFactory.getLogger(clazz));
    }

    public static Watcher getWatcher(final Class<?> clazz, final String name) {
        return new Watcher(LoggerFactory.getLogger(clazz, name));
    }

    public static Watcher getWatcher(final Logger logger, final String name) {
        return new Watcher(LoggerFactory.getLogger(logger, name));
    }
}
