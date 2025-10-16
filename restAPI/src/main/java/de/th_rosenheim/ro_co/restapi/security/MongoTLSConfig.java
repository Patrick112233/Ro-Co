package de.th_rosenheim.ro_co.restapi.security;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMKeyPair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.lang.NonNull;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

@Configuration
public class MongoTLSConfig extends AbstractMongoClientConfiguration {
    // Configuration for MongoDB TLS/SSL

    @Value("${security.enabled}")
    private boolean secure;

    @Value("${spring.data.mongodb.port}")
    private String mongoDbPort;

    @Value("${spring.data.mongodb.host}")
    private String mongoDbHost;

    
    @Value("${spring.data.mongodb.database}")
    private String mongoDbName;

    @Value("${tls.caFile.name}")
    private String caFileName;
    
    @Value("${tls.certFile.name}")
    private String certificateKeyFileName;

    @Value("${security.keyPWD}")
    private String keyPWD;

    // Exact DN for the $external user created in Mongo (optional; if not provided, X.509 will not be set)
    @Value("${security.x509.user:}")
    private String x509User;


    @Override
    @NonNull
    public String getDatabaseName() {
        return mongoDbName;
    }

    @NonNull
    public String getMongoUri() {
        return "mongodb://" + mongoDbHost + ":" + mongoDbPort + "/" + mongoDbName;
    }


    @Override
    @NonNull
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(getMongoUri());
        SSLContext sslContext = null;
            try {
                sslContext = createSSLContext();
            } catch (Exception e) {
                //@TODO Logging
                throw new IllegalStateException("Failed to create SSLContext for MongoDB connection", e);
            }
            SSLContext finalSslContext = sslContext;
            MongoClientSettings.Builder settingsBuilder = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .applyToSslSettings(builder -> {
                        builder.enabled(true);
                        builder.context(finalSslContext);
                        builder.invalidHostNameAllowed(!secure);
                    })
                    // no-op, placeholder kept from previous code
                    .applyToClusterSettings(builder -> {});

            if (x509User != null && !x509User.isBlank()) {
                settingsBuilder.credential(MongoCredential.createMongoX509Credential(x509User));
            } else {
                throw new IllegalStateException("MongoDB X.509 user DN not configured; cannot authenticate");
            }

        System.out.println("[MongoTLSConfig] TLS enabled for Mongo client. secure=" + secure + ", caFile=" + caFileName + ", certFile=" + certificateKeyFileName + ", x509User set=" + (x509User != null && !x509User.isBlank()));
        MongoClientSettings settings = settingsBuilder.build();
            MongoClient client = MongoClients.create(settings);
            return client;
    }

    /**
     * Ensure both sync and reactive Mongo clients created by Spring Boot auto-config
     * use the same SSLContext and (optionally) X.509 credentials.
     */
    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer() {
        return builder -> {
            try {
                SSLContext sslContext = createSSLContext();
                builder.applyToSslSettings(ssl -> {
                    ssl.enabled(true);
                    ssl.context(sslContext);
                    ssl.invalidHostNameAllowed(!secure);
                });
                if (x509User != null && !x509User.isBlank()) {
                    builder.credential(MongoCredential.createMongoX509Credential(x509User));
                }
            } catch (Exception e) {
                throw new IllegalStateException("Failed to configure Mongo SSL/X.509 from PEMs", e);
            }
        };
    }

    /**
     * https://dev-diaries.hashnode.dev/spring-data-mongodb-configuring-secure-connection-between-spring-boot-and-mongodb-over-tls-using-certificates
     * @return
     * @throws Exception
     */
    public SSLContext createSSLContext() throws Exception {
        // Add Root CA to TrustStore
        TrustManagerFactory tmf;

        ClassPathResource resource = new ClassPathResource(caFileName);
        String pem = new String(resource.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        String certPem = pem.replaceAll("(?s).*?(-----BEGIN CERTIFICATE-----.*?-----END CERTIFICATE-----).*", "$1");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate caCert;
        try (InputStream certStream = new java.io.ByteArrayInputStream(certPem.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            caCert = (X509Certificate) cf.generateCertificate(certStream);
        }
        /*
        ClassPathResource resource = new ClassPathResource(caFileName);
        InputStream is = resource.getInputStream(); // Expect plain certificate in Base64 encoded and with -----BEGIN CERTIFICATE----- and -----END CERTIFICATE-----
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        X509Certificate caCert = (X509Certificate) cf.generateCertificate(is);*/

        tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null); // You don't need the KeyStore instance to come from a file.
        ks.setCertificateEntry("caCert", caCert);
        tmf.init(ks);

        // Add Client Certificate to KeyStore to authenticate to mongo DB
        KeyManagerFactory keyFac;
        SSLContext sslContext = null;
        resource = new ClassPathResource(certificateKeyFileName);
        InputStream is = resource.getInputStream();
        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(null); // needs to be initialised, otherwise throws exception

            @SuppressWarnings("resource")
            PEMParser pemParser = new PEMParser(new InputStreamReader(is));
            
            Object object;
            X509Certificate certificate = null;
            PrivateKey privateKey = null;
            while ((object = pemParser.readObject()) != null) {
                if (object instanceof X509CertificateHolder x509CertificateHolder) {
                    JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();
                    X509CertificateHolder certificateHolder = x509CertificateHolder;
                    certificate = certConverter.getCertificate(certificateHolder);
                } else if (object instanceof PEMKeyPair pemKeyPair) {
                    KeyPair kp = new JcaPEMKeyConverter().getKeyPair(pemKeyPair);
                    privateKey = kp.getPrivate();
                }
            }
            pemParser.close();
            if (certificate == null || privateKey == null) {
                throw new IllegalStateException("Could not parse certificate or private key from PEM file");
            }

            keystore.setKeyEntry("mongo", privateKey, keyPWD.toCharArray(), new Certificate[]{certificate});
            keystore.setCertificateEntry("mongo-ca", caCert);
            keyFac = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyFac.init(keystore, keyPWD.toCharArray());

            // Initialize with a TLS context supporting 1.2/1.3 depending on JDK
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyFac.getKeyManagers(), tmf.getTrustManagers(), SecureRandom.getInstanceStrong());
        } catch (Exception e) {
            //LOG.error("Error creating SSL context", e);
            //@TODO Logging
            throw new IllegalStateException("Failed to create SSLContext for MongoDB connection", e);
        } finally {
            is.close();
        }
        return sslContext;
    }



}
