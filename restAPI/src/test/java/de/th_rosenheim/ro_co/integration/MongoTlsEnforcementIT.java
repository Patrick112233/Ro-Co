package de.th_rosenheim.ro_co.integration;

import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.MongoClientSettings;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Testcontainers
class MongoTlsEnforcementIT extends setUpIT {

    private String host() { return mongoDBContainer.getHost(); }
    private int port() { return mongoDBContainer.getMappedPort(27017); }

    // Build an SSLContext that trusts our Root CA only (no client cert needed for handshake test)
    private SSLContext trustRootCAContext() throws Exception {
        ClassPathResource caRes = new ClassPathResource("certs/RoCoRootCA.pem");
        try (InputStream is = caRes.getInputStream()) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate caCert = (X509Certificate) cf.generateCertificate(is);

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null);
            trustStore.setCertificateEntry("rootCA", caCert);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, tmf.getTrustManagers(), new SecureRandom());
            return ctx;
        }
    }

    @Test
    void nonTlsConnectionIsRejected() {
        // Intentionally create a client without TLS and attempt a simple operation
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(b -> b.hosts(Collections.singletonList(new ServerAddress(host(), port()))))
                .applyToSslSettings(b -> b.enabled(false))
                .applyToSocketSettings(b -> b.connectTimeout(2000, java.util.concurrent.TimeUnit.MILLISECONDS))
                .applyToServerSettings(b -> b.heartbeatFrequency(1000, java.util.concurrent.TimeUnit.MILLISECONDS))
                .build();

        try (MongoClient client = MongoClients.create(settings)) {
            MongoException ex = assertThrows(MongoException.class, () -> client.listDatabaseNames().first());
            String msg = ex.getMessage();
            assertTrue(msg != null && (msg.contains("end of stream") || msg.toLowerCase().contains("ssl") || msg.toLowerCase().contains("tls")),
                    "Expected non-TLS connection to fail due to TLS enforcement, but got: " + msg);
        }
    }

    @Test
    void tls12HandshakeSucceeds() throws Exception {
        SSLContext ctx = trustRootCAContext();
        try (Socket socket = ctx.getSocketFactory().createSocket(host(), port());
             SSLSocket ssl = (SSLSocket) socket) {
            ssl.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
            ssl.startHandshake();
            // If no exception, handshake succeeded at >= TLS 1.2
        }
    }

    @Test
    void tls10HandshakeIsRejectedOrDisabled() throws Exception {
        SSLContext ctx = trustRootCAContext();
        try (Socket socket = ctx.getSocketFactory().createSocket(host(), port());
             SSLSocket ssl = (SSLSocket) socket) {
            try {
                ssl.setEnabledProtocols(new String[]{"TLSv1"});
            } catch (IllegalArgumentException iae) {
                // TLSv1 not supported by this JDK, treat as acceptable (client-side disabled)
                // As a complementary assertion, verify server log mentions TLS 1.0 disabled
                String logs = mongoDBContainer.getLogs();
                assertTrue(logs.contains("disabling TLS 1.0") || logs.contains("Automatically disabling TLS 1.0"),
                        "TLSv1 disabled not confirmed in server logs while client does not support TLSv1");
                return;
            }

            // If we managed to enable TLSv1 on client, handshake must fail with protocol_version or similar
            Exception ex = assertThrows(Exception.class, ssl::startHandshake);
            String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
            assertTrue(msg.contains("protocol") || msg.contains("handshake") || msg.contains("version"),
                    "Expected TLSv1 handshake to be rejected; got: " + ex);
        }
    }
}
