package org.usefultoys.slf4j.watcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WatcherConfigTest {

    @BeforeEach
    void setUp() {
        System.clearProperty(WatcherConfig.PROP_NAME);
        System.clearProperty(WatcherConfig.PROP_DELAY);
        System.clearProperty(WatcherConfig.PROP_PERIOD);
        System.clearProperty(WatcherConfig.PROP_DATA_PREFIX);
        System.clearProperty(WatcherConfig.PROP_DATA_SUFFIX);
        System.clearProperty(WatcherConfig.PROP_MESSAGE_PREFIX);
        System.clearProperty(WatcherConfig.PROP_MESSAGE_SUFFIX);

        // Reinitialize WatcherConfig to ensure clean state for each test
        WatcherConfig.init();
    }

    @Test
    void testDefaultValues() {
        assertEquals("watcher", WatcherConfig.name, "Default value for name should be 'watcher'");
        assertEquals(60000L, WatcherConfig.delayMilliseconds, "Default value for delayMilliseconds should be 60000");
        assertEquals(600000L, WatcherConfig.periodMilliseconds, "Default value for periodMilliseconds should be 600000");
        assertEquals("", WatcherConfig.dataPrefix, "Default value for dataPrefix should be an empty string");
        assertEquals("", WatcherConfig.dataSuffix, "Default value for dataSuffix should be an empty string");
        assertEquals("", WatcherConfig.messagePrefix, "Default value for messagePrefix should be an empty string");
        assertEquals("", WatcherConfig.messageSuffix, "Default value for messageSuffix should be an empty string");
    }

    @Test
    void testDelayMillisecondsProperty() {
        System.setProperty(WatcherConfig.PROP_DELAY, "120000");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(120000L, WatcherConfig.delayMilliseconds, "delayMilliseconds should reflect the system property value");

        System.setProperty(WatcherConfig.PROP_DELAY, "120s");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(120000L, WatcherConfig.delayMilliseconds, "delayMilliseconds should reflect the system property value");

        System.setProperty(WatcherConfig.PROP_DELAY, "2m");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(120000L, WatcherConfig.delayMilliseconds, "delayMilliseconds should reflect the system property value");

        System.setProperty(WatcherConfig.PROP_DELAY, "1h");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(3600000L, WatcherConfig.delayMilliseconds, "delayMilliseconds should reflect the system property value");
    }

    @Test
    void testPeriodMillisecondsProperty() {
        System.setProperty(WatcherConfig.PROP_PERIOD, "120000");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(120000L, WatcherConfig.periodMilliseconds, "periodMilliseconds should reflect the system property value");

        System.setProperty(WatcherConfig.PROP_PERIOD, "120s");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(120000L, WatcherConfig.periodMilliseconds, "periodMilliseconds should reflect the system property value");

        System.setProperty(WatcherConfig.PROP_PERIOD, "2m");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(120000L, WatcherConfig.periodMilliseconds, "periodMilliseconds should reflect the system property value");

        System.setProperty(WatcherConfig.PROP_PERIOD, "1h");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(3600000L, WatcherConfig.periodMilliseconds, "periodMilliseconds should reflect the system property value");
    }

    @Test
    void testDataPrefixProperty() {
        System.setProperty(WatcherConfig.PROP_DATA_PREFIX, "data.");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals("data.", WatcherConfig.dataPrefix, "dataPrefix should reflect the system property value");
    }

    @Test
    void testDataSuffixProperty() {
        System.setProperty(WatcherConfig.PROP_DATA_SUFFIX, ".data");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(".data", WatcherConfig.dataSuffix, "dataSuffix should reflect the system property value");
    }

    @Test
    void testMessagePrefixProperty() {
        System.setProperty(WatcherConfig.PROP_MESSAGE_PREFIX, "message.");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals("message.", WatcherConfig.messagePrefix, "messagePrefix should reflect the system property value");
    }

    @Test
    void testMessageSuffixProperty() {
        System.setProperty(WatcherConfig.PROP_MESSAGE_SUFFIX, ".message");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(".message", WatcherConfig.messageSuffix, "messageSuffix should reflect the system property value");
    }
}
