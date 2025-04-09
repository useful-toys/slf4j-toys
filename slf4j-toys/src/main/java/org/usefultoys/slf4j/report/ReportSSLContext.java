package org.usefultoys.slf4j.report;

import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.PrintStream;
import java.security.Provider;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reports details of SSL contexts supported by the JVM, including cipher suites, protocols, and supported SSL
 * parameters for each context.
 */
public class ReportSSLContext implements Runnable {
    private final Logger logger;

    final String contextNames[] = {
            "Default", "SSL", "SSLv2", "SSLv3", "TLS", "TLSv1", "TLSv1.1", "TLSv1.2"
    };

    public ReportSSLContext(final Logger logger) {
        this.logger = logger;
    }

    private void printList(final PrintStream ps, final String[] list, final String newLineSpace) {
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
            final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            ps.println("SSL Context " + contextName);

            try {
                SSLContext sslContext = SSLContext.getInstance(contextName);
                ps.println("   Protocol: " + sslContext.getProtocol());
                ps.println("   Class: " + sslContext.getClass());
                ps.println("   Provider:");
                final Provider provider = sslContext.getProvider();
                final TreeMap<Object, Object> sortedProperties = new TreeMap<>(provider);
                for (Map.Entry<Object, Object> entry : sortedProperties.entrySet()) {
                    ps.println("    - " + entry.getKey() + ": " + entry.getValue());
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
                ps.println("      EndpointIdentificationAlgorithm: " + p.getEndpointIdentificationAlgorithm());
                ps.println("      Need Client Auth: " + p.getNeedClientAuth());
                ps.println("      Want Client Auth: " + p.getWantClientAuth());
                ps.print("      Protocols: ");
                printList(ps, p.getProtocols(), "          ");
                ps.print("      Cipher Suites: ");
                printList(ps, p.getCipherSuites(), "          ");
                p = sslContext.getSupportedSSLParameters();
                ps.println("   Supported SSL Parameters:");
                ps.println("      EndpointIdentificationAlgorithm: " + p.getEndpointIdentificationAlgorithm());
                ps.println("      Need Client Auth: " + p.getNeedClientAuth());
                ps.println("      Want Client Auth: " + p.getWantClientAuth());
                ps.print("      Protocols: ");
                printList(ps, p.getProtocols(), "          ");
                ps.print("      Cipher Suites: ");
                printList(ps, p.getCipherSuites(), "          ");
            } catch (Exception e) {
                ps.println("Falha ao detalhar SSLContext: " + e.getMessage());
            }
            ps.close();
        }
    }
}
