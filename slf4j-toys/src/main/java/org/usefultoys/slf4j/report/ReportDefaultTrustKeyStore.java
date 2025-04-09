package org.usefultoys.slf4j.report;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.PrintStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

/**
 * Reports the trusted certificate authorities from the default JVM trust store.
 */
@RequiredArgsConstructor
public class ReportDefaultTrustKeyStore implements Runnable {

    private final Logger logger;

    @Override
    public void run() {
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("Trust Keystore");

        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            TrustManager[] trustManagers = tmf.getTrustManagers();

            for (int i = 0; i < trustManagers.length; ++i) {
                TrustManager tm = trustManagers[i];
                ps.println(" - TrustManager: " + i + " (" + tm.getClass() + ")");
                if (tm instanceof X509TrustManager) {
                    X509TrustManager x509tm = (X509TrustManager) tm;
                    X509Certificate[] certificates = x509tm.getAcceptedIssuers();
                    for (int j = 0; j < certificates.length; j++) {
                        final X509Certificate cert = certificates[j];
                        ps.println("   - Certificate #" + j);
                        ps.println("       Subject: " + cert.getSubjectX500Principal());
                        ps.println("       Issuer: " + cert.getIssuerX500Principal());
                        ps.println("       #: " + cert.getSerialNumber() + " From: " + cert.getNotBefore() + " Until: " + cert.getNotAfter());
                    }
                }
            }
        } catch (Exception e) {
            ps.println("Cannot read TrustManager: " + e.getMessage());
        }
        ps.close();
    }
}
