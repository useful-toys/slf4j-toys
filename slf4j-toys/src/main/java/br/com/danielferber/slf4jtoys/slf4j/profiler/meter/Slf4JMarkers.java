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
    public static final Marker OK_MARKER = MarkerFactory.getMarker("METER_OK");
    public static final Marker FAIL_MARKER = MarkerFactory.getMarker("METER_FAIL");
    public static final Marker FINALIZED_MARKER = MarkerFactory.getMarker("METER_FINALIZED");
    public static final Marker INCONSISTENT_START = MarkerFactory.getMarker("METER_INCONSISTENT_START");
    public static final Marker INCONSISTENT_OK = MarkerFactory.getMarker("METER_INCONSISTENT_OK");
    public static final Marker INCONSISTENT_FAIL = MarkerFactory.getMarker("METER_INCONSISTENT_FAIL");
}
