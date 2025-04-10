package org.usefultoys.slf4j.report;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.PrintStream;
import java.security.Provider;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Reports details of SSL contexts supported by the JVM, including cipher suites, protocols, and supported SSL
 * parameters for each context.
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportSSLContext implements Runnable {

    private final @NonNull Logger logger;

    final String[] contextNames = {
            "Default", "SSL", "SSLv2", "SSLv3", "TLS", "TLSv1", "TLSv1.1", "TLSv1.2"
    };

    private static void printList(final PrintStream ps, final String[] list, final String newLineSpace) {
        int i = 1;
        for (final String s : list) {
            if (i++ % 10 == 0) {
                ps.println();
                ps.print(newLineSpace);
            }
            ps.print(s);
            ps.print("; ");
        }
        ps.println();
    }

    @Override
    public void run() {
        for (final String contextName : contextNames) {
            @Cleanup
            final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            ps.printf("SSL Context %s%n", contextName);

            try {
                SSLContext sslContext = SSLContext.getInstance(contextName);
                ps.printf("   Protocol: %s%n", sslContext.getProtocol());
                ps.printf("   Class: %s%n", sslContext.getClass());
                ps.println("   Provider:");
                final Provider provider = sslContext.getProvider();
                final SortedMap<Object, Object> sortedProperties = new TreeMap<>(provider);
                for (Map.Entry<Object, Object> entry : sortedProperties.entrySet()) {
                    ps.printf("    - %s: %s%n", entry.getKey(), entry.getValue());
                }
                ps.println("   SocketFactory: ");
                ps.print("      Default Cipher Suites:");
                printList(ps, sslContext.getSocketFactory().getDefaultCipherSuites(), "          ");
                ps.print("      Supported Cipher Suites: ");
                printList(ps, sslContext.getSocketFactory().getSupportedCipherSuites(), "          ");
                ps.println("   ServerSocketFactory: ");
                ps.print("      Default Cipher Suites:");
                printList(ps, sslContext.getServerSocketFactory().getDefaultCipherSuites(), "          ");
                ps.print("      Supported Cipher Suites:");
                printList(ps, sslContext.getServerSocketFactory().getSupportedCipherSuites(), "          ");
                SSLParameters p = sslContext.getDefaultSSLParameters();
                ps.println("   Default SSL Parameters:");
                ps.printf("      EndpointIdentificationAlgorithm: %s%n", p.getEndpointIdentificationAlgorithm());
                ps.printf("      Need Client Auth: %s%n", p.getNeedClientAuth());
                ps.printf("      Want Client Auth: %s%n", p.getWantClientAuth());
                ps.print("      Protocols: ");
                printList(ps, p.getProtocols(), "          ");
                ps.print("      Cipher Suites: ");
                printList(ps, p.getCipherSuites(), "          ");
                p = sslContext.getSupportedSSLParameters();
                ps.println("   Supported SSL Parameters:");
                ps.printf("      EndpointIdentificationAlgorithm: %s%n", p.getEndpointIdentificationAlgorithm());
                ps.printf("      Need Client Auth: %s%n", p.getNeedClientAuth());
                ps.printf("      Want Client Auth: %s%n", p.getWantClientAuth());
                ps.print("      Protocols: ");
                printList(ps, p.getProtocols(), "          ");
                ps.print("      Cipher Suites: ");
                printList(ps, p.getCipherSuites(), "          ");
            } catch (Exception e) {
                ps.printf("Falha ao detalhar SSLContext: %s%n", e.getMessage());
            }
        }
    }
}
