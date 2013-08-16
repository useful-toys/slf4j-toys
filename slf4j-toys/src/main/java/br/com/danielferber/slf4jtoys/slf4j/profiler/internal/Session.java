/*
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.internal;

import java.util.Timer;
import java.util.UUID;

/**
 *
 * @author Daniel Felix Ferber
 */
public class Session {

    private Session() {
        // impede instâncias
    }
    public static final String uuid = UUID.randomUUID().toString().replace('-', '.');
    public static final Timer timer = new Timer("br.com.danielferber.slf4jtoys.slf4j");
}
