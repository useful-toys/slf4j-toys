package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.BeforeEach;
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

    @Test
    void testDefaultValues() {
        assertTrue(ReporterConfig.reportVM, "Valor padrão de reportVM deve ser true");
        assertFalse(ReporterConfig.reportFileSystem, "Valor padrão de reportFileSystem deve ser false");
        assertTrue(ReporterConfig.reportMemory, "Valor padrão de reportMemory deve ser true");
        assertTrue(ReporterConfig.reportUser, "Valor padrão de reportUser deve ser true");
        assertTrue(ReporterConfig.reportProperties, "Valor padrão de reportProperties deve ser true");
        assertFalse(ReporterConfig.reportEnvironment, "Valor padrão de reportEnvironment deve ser false");
        assertTrue(ReporterConfig.reportPhysicalSystem, "Valor padrão de reportPhysicalSystem deve ser true");
        assertTrue(ReporterConfig.reportOperatingSystem, "Valor padrão de reportOperatingSystem deve ser true");
        assertTrue(ReporterConfig.reportCalendar, "Valor padrão de reportCalendar deve ser true");
        assertTrue(ReporterConfig.reportLocale, "Valor padrão de reportLocale deve ser true");
        assertTrue(ReporterConfig.reportCharset, "Valor padrão de reportCharset deve ser true");
        assertFalse(ReporterConfig.reportNetworkInterface, "Valor padrão de reportNetworkInterface deve ser false");
        assertFalse(ReporterConfig.reportSSLContext, "Valor padrão de reportSSLContext deve ser false");
        assertFalse(ReporterConfig.reportDefaultTrustKeyStore, "Valor padrão de reportDefaultTrustKeyStore deve ser false");
        assertEquals("report", ReporterConfig.name, "Valor padrão de name deve ser 'report'");
    }

    @Test
    void testCustomValues() {
        System.setProperty(ReporterConfig.PROP_VM, "false");
        System.setProperty(ReporterConfig.PROP_FILE_SYSTEM, "true");
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
