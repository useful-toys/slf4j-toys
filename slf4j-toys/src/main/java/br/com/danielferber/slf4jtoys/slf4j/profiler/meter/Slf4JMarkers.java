/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.meter;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 *
 * @author Daniel Felix Ferber
 */
public class Slf4JMarkers {
    public static final Marker START_MARKER = MarkerFactory.getMarker("METER_START");
    public static final Marker START_WATCH_MARKER = MarkerFactory.getMarker("WATCHER_START");
    public static final Marker OK_MARKER = MarkerFactory.getMarker("METER_OK");
    public static final Marker OK_WATCH_MARKER = MarkerFactory.getMarker("WATCHER_OK");
    public static final Marker FAIL_MARKER = MarkerFactory.getMarker("METER_FAIL");
    public static final Marker FAIL_WATCH_MARKER = MarkerFactory.getMarker("WATCHER_FAIL");
    public static final Marker FINALIZED_MARKER = MarkerFactory.getMarker("METER_FINALIZED");
}
