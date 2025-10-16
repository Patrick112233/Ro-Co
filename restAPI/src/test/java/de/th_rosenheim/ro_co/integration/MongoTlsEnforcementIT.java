package de.th_rosenheim.ro_co.integration;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Testcontainers
class MongoTlsEnforcementIT extends setUpIT {

    private String host() { return mongoDBContainer.getHost(); }
    private int port() { return mongoDBContainer.getMappedPort(27017); }

    // Build an SSLContext that both trusts the Root CA and presents the RoCoAPI client certificate for mTLS
    private SSLContext clientCertContext() throws Exception {
        // Trust store with the root CA
        ClassPathResource caRes = new ClassPathResource("certs/RoCoRootCA.pem");
        X509Certificate caCert;
        try (InputStream is = caRes.getInputStream()) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            caCert = (X509Certificate) cf.generateCertificate(is);
        }

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null);
        trustStore.setCertificateEntry("rootCA", caCert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        char[] keyPassword = "123456".toCharArray();

        // Key store with client cert + key
        ClassPathResource clientRes = new ClassPathResource("certs/RoCoAPI.pem");
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null);
        try (PEMParser pemParser = new PEMParser(new InputStreamReader(clientRes.getInputStream()))) {
            Object object;
            X509Certificate clientCert = null;
            PrivateKey privateKey = null;
            JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();
            JcaPEMKeyConverter keyConverter = new JcaPEMKeyConverter();
            while ((object = pemParser.readObject()) != null) {
                if (object instanceof X509CertificateHolder holder) {
                    clientCert = certConverter.getCertificate(holder);
                } else if (object instanceof PEMKeyPair pemKeyPair) {
                    KeyPair kp = keyConverter.getKeyPair(pemKeyPair);
                    privateKey = kp.getPrivate();
                } else if (object instanceof PrivateKeyInfo privateKeyInfo) {
                    privateKey = keyConverter.getPrivateKey(privateKeyInfo);
                }
            }
            if (clientCert == null || privateKey == null) {
                throw new IllegalStateException("RoCoAPI.pem must contain both certificate and private key");
            }
            Certificate[] chain = new Certificate[]{clientCert, caCert};
            keyStore.setKeyEntry("mongo-client", privateKey, keyPassword, chain);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyPassword);

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        return ctx;
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
        SSLContext ctx = clientCertContext();
        try (Socket socket = ctx.getSocketFactory().createSocket(host(), port());
             SSLSocket ssl = (SSLSocket) socket) {
            ssl.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
            ssl.startHandshake();
            // If no exception, handshake succeeded at >= TLS 1.2
        }
    }

    @Test
    void tls10HandshakeIsRejectedOrDisabled() throws Exception {
        // For TLSv1 negative test, client cert is still required to reach mutual TLS stage
        SSLContext ctx = clientCertContext();
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
