package org.usefultoys.slf4j.meter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.usefultoys.slf4j.meter.MeterConfig;

import static org.junit.jupiter.api.Assertions.*;

class MeterConfigTest {

    @BeforeEach
    void setUp() {
        // Limpa as propriedades do sistema para garantir um estado limpo antes de cada teste
        System.clearProperty(MeterConfig.PROP_PROGRESS_PERIOD);
        System.clearProperty(MeterConfig.PROP_PRINT_CATEGORY);
        System.clearProperty(MeterConfig.PROP_PRINT_STATUS);
        System.clearProperty(MeterConfig.PROP_PRINT_POSITION);
        System.clearProperty(MeterConfig.PROP_PRINT_LOAD);
        System.clearProperty(MeterConfig.PROP_PRINT_MEMORY);
        System.clearProperty(MeterConfig.PROP_DATA_PREFIX);
        System.clearProperty(MeterConfig.PROP_DATA_SUFFIX);
        System.clearProperty(MeterConfig.PROP_MESSAGE_PREFIX);
        System.clearProperty(MeterConfig.PROP_MESSAGE_SUFFIX);

        MeterConfig.init(); // Reinitialize MeterConfig
    }

    @Test
    void testDefaultValues() {
        assertEquals(2000L, MeterConfig.progressPeriodMilliseconds, "Default value for progressPeriodMilliseconds should be 2000ms");
        assertFalse(MeterConfig.printCategory, "Default value for printCategory should be false");
        assertTrue(MeterConfig.printStatus, "Default value for printStatus should be true");
        assertFalse(MeterConfig.printPosition, "Default value for printPosition should be false");
        assertFalse(MeterConfig.printLoad, "Default value for printLoad should be false");
        assertFalse(MeterConfig.printMemory, "Default value for printMemory should be false");
        assertEquals("", MeterConfig.dataPrefix, "Default value for dataPrefix should be an empty string");
        assertEquals("", MeterConfig.dataSuffix, "Default value for dataSuffix should be an empty string");
        assertEquals("", MeterConfig.messagePrefix, "Default value for messagePrefix should be an empty string");
        assertEquals("", MeterConfig.messageSuffix, "Default value for messageSuffix should be an empty string");
    }

    @Test
    void testProgressPeriodMillisecondsProperty() {
        System.setProperty("slf4jtoys.meter.progress.period", "5000");
        MeterConfig.init();
        assertEquals(5000L, MeterConfig.progressPeriodMilliseconds, "progressPeriodMilliseconds should reflect the system property value");
    }

    @Test
    void testPrintCategoryProperty() {
        System.setProperty("slf4jtoys.meter.print.category", "true");
        MeterConfig.init();
        assertTrue(MeterConfig.printCategory, "printCategory should reflect the system property value");
    }

    @Test
    void testPrintStatusProperty() {
        System.setProperty("slf4jtoys.meter.print.status", "false");
        MeterConfig.init();
        assertFalse(MeterConfig.printStatus, "printStatus should reflect the system property value");
    }

    @Test
    void testPrintPositionProperty() {
        System.setProperty("slf4jtoys.meter.print.position", "true");
        MeterConfig.init();
        assertTrue(MeterConfig.printPosition, "printPosition should reflect the system property value");
    }

    @Test
    void testPrintLoadProperty() {
        System.setProperty("slf4jtoys.meter.print.load", "true");
        MeterConfig.init();
        assertTrue(MeterConfig.printLoad, "printLoad should reflect the system property value");
    }

    @Test
    void testPrintMemoryProperty() {
        System.setProperty("slf4jtoys.meter.print.memory", "true");
        MeterConfig.init();
        assertTrue(MeterConfig.printMemory, "printMemory should reflect the system property value");
    }

    @Test
    void testDataPrefixProperty() {
        System.setProperty("slf4jtoys.meter.data.prefix", "data.");
        MeterConfig.init();
        assertEquals("data.", MeterConfig.dataPrefix, "dataPrefix should reflect the system property value");
    }

    @Test
    void testDataSuffixProperty() {
        System.setProperty("slf4jtoys.meter.data.suffix", ".data");
        MeterConfig.init();
        assertEquals(".data", MeterConfig.dataSuffix, "dataSuffix should reflect the system property value");
    }

    @Test
    void testMessagePrefixProperty() {
        System.setProperty("slf4jtoys.meter.message.prefix", "message.");
        MeterConfig.init();
        assertEquals("message.", MeterConfig.messagePrefix, "messagePrefix should reflect the system property value");
    }

    @Test
    void testMessageSuffixProperty() {
        System.setProperty("slf4jtoys.meter.message.suffix", ".message");
        MeterConfig.init();
        assertEquals(".message", MeterConfig.messageSuffix, "messageSuffix should reflect the system property value");
    }
}
