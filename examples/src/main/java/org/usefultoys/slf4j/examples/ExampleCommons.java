/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.usefultoys.slf4j.examples;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.usefultoys.slf4j.examples.watcher.WithDefaultExecutor;

/**
 *
 * @author x7ws
 */
public class ExampleCommons {

    public static Random random = new Random(System.currentTimeMillis());

    /**
     * Customizes the SLF4J simple logger to display trace messages that contain detailed and parsable information. Enable additional information that
     * allow better undestanding of the log output.
     */
    public static void configureSLF4J() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "yy/MM/dd HH:mm");
    }

    public static class HeavyCalculation extends Thread {

        {
            setDaemon(true);
        }

        @Override
        public void run() {
            int i = Integer.MIN_VALUE;
            while (!Thread.interrupted()) {
                i++;
            }
        }
    };

    public static void doWork() {
        /* Start some calculation on separated thread to see CPU usage on Watcher log output. */
        HeavyCalculation c1 = new HeavyCalculation();
        HeavyCalculation c2 = new HeavyCalculation();
        c1.start();
        c2.start();

        try {
            Thread.sleep(10000L);
        } catch (InterruptedException ex) {
            Logger.getLogger(WithDefaultExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
