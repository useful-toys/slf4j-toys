package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.usefultoys.slf4j.internal.Config;

import static org.junit.jupiter.api.Assertions.*;

class ReporterConfigTest {

    @BeforeEach
    void setUp() {
        // Limpa as propriedades do sistema para garantir um estado limpo
        System.clearProperty(ReporterConfig.PROP_VM);
        System.clearProperty(ReporterConfig.PROP_FILE_SYSTEM);
        System.clearProperty(ReporterConfig.PROP_MEMORY);
        System.clearProperty(ReporterConfig.PROP_USER);
        System.clearProperty(ReporterConfig.PROP_PROPERTIES);
        System.clearProperty(ReporterConfig.PROP_ENVIRONMENT);
        System.clearProperty(ReporterConfig.PROP_PHYSICAL_SYSTEM);
        System.clearProperty(ReporterConfig.PROP_OPERATING_SYSTEM);
        System.clearProperty(ReporterConfig.PROP_CALENDAR);
        System.clearProperty(ReporterConfig.PROP_LOCALE);
        System.clearProperty(ReporterConfig.PROP_CHARSET);
        System.clearProperty(ReporterConfig.PROP_NETWORK_INTERFACE);
        System.clearProperty(ReporterConfig.PROP_SSL_CONTEXT);
        System.clearProperty(ReporterConfig.PROP_DEFAULT_TRUST_KEYSTORE);
        System.clearProperty(ReporterConfig.PROP_NAME);

        // Reinicializa o ReporterConfig
        ReporterConfig.init();
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(ReporterConfig.PROP_VM);
        System.clearProperty(ReporterConfig.PROP_FILE_SYSTEM);
        System.clearProperty(ReporterConfig.PROP_MEMORY);
        System.clearProperty(ReporterConfig.PROP_USER);
        System.clearProperty(ReporterConfig.PROP_PROPERTIES);
        System.clearProperty(ReporterConfig.PROP_ENVIRONMENT);
        System.clearProperty(ReporterConfig.PROP_PHYSICAL_SYSTEM);
        System.clearProperty(ReporterConfig.PROP_OPERATING_SYSTEM);
        System.clearProperty(ReporterConfig.PROP_CALENDAR);
        System.clearProperty(ReporterConfig.PROP_LOCALE);
        System.clearProperty(ReporterConfig.PROP_CHARSET);
        System.clearProperty(ReporterConfig.PROP_NETWORK_INTERFACE);
        System.clearProperty(ReporterConfig.PROP_SSL_CONTEXT);
        System.clearProperty(ReporterConfig.PROP_DEFAULT_TRUST_KEYSTORE);
        System.clearProperty(ReporterConfig.PROP_NAME);
    }

    @Test
    void testDefaultValues() {
        assertTrue(ReporterConfig.reportVM, "Valor padrão de reportVM deve ser true");
        System.setProperty(ReporterConfig.PROP_MEMORY, "false");
        System.setProperty(ReporterConfig.PROP_USER, "false");
        System.setProperty(ReporterConfig.PROP_PROPERTIES, "false");
        System.setProperty(ReporterConfig.PROP_ENVIRONMENT, "true");
        System.setProperty(ReporterConfig.PROP_PHYSICAL_SYSTEM, "false");
        System.setProperty(ReporterConfig.PROP_OPERATING_SYSTEM, "false");
        System.setProperty(ReporterConfig.PROP_CALENDAR, "false");
        System.setProperty(ReporterConfig.PROP_LOCALE, "false");
        System.setProperty(ReporterConfig.PROP_CHARSET, "false");
        System.setProperty(ReporterConfig.PROP_NETWORK_INTERFACE, "true");
        System.setProperty(ReporterConfig.PROP_SSL_CONTEXT, "true");
        System.setProperty(ReporterConfig.PROP_DEFAULT_TRUST_KEYSTORE, "true");
        System.setProperty(ReporterConfig.PROP_NAME, "customReport");

        ReporterConfig.init();

        assertFalse(ReporterConfig.reportVM, "reportVM deve refletir o valor da propriedade do sistema");
        assertTrue(ReporterConfig.reportFileSystem, "reportFileSystem deve refletir o valor da propriedade do sistema");
        assertFalse(ReporterConfig.reportMemory, "reportMemory deve refletir o valor da propriedade do sistema");
        assertFalse(ReporterConfig.reportUser, "reportUser deve refletir o valor da propriedade do sistema");
        assertFalse(ReporterConfig.reportProperties, "reportProperties deve refletir o valor da propriedade do sistema");
        assertTrue(ReporterConfig.reportEnvironment, "reportEnvironment deve refletir o valor da propriedade do sistema");
        assertFalse(ReporterConfig.reportPhysicalSystem, "reportPhysicalSystem deve refletir o valor da propriedade do sistema");
        assertFalse(ReporterConfig.reportOperatingSystem, "reportOperatingSystem deve refletir o valor da propriedade do sistema");
        assertFalse(ReporterConfig.reportCalendar, "reportCalendar deve refletir o valor da propriedade do sistema");
        assertFalse(ReporterConfig.reportLocale, "reportLocale deve refletir o valor da propriedade do sistema");
        assertFalse(ReporterConfig.reportCharset, "reportCharset deve refletir o valor da propriedade do sistema");
        assertTrue(ReporterConfig.reportNetworkInterface, "reportNetworkInterface deve refletir o valor da propriedade do sistema");
        assertTrue(ReporterConfig.reportSSLContext, "reportSSLContext deve refletir o valor da propriedade do sistema");
        assertTrue(ReporterConfig.reportDefaultTrustKeyStore, "reportDefaultTrustKeyStore deve refletir o valor da propriedade do sistema");
        assertEquals("customReport", ReporterConfig.name, "name deve refletir o valor da propriedade do sistema");
    }
}
