package de.th_rosenheim.ro_co.restapi.security;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMKeyPair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.lang.NonNull;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
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

    
    @Value("${spring.data.mongodb.name}")
    private String mongoDbName;

    @Value("${tls.caFile.name}")
    private String caFileName;
    
    @Value("${tls.certificateKeyFile.name}")
    private String certificateKeyFileName;

    @Value("${security.keyPWD:}")
    private String keyPWD;


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
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .applyToSslSettings(builder -> {
                        builder.enabled(true);
                        builder.context(finalSslContext);
                        if (!secure) {
                            builder.invalidHostNameAllowed(true);
                        }
                    })
                    .build();
            MongoClient client = MongoClients.create(settings);
            return client;
    }

    /**
     * https://dev-diaries.hashnode.dev/spring-data-mongodb-configuring-secure-connection-between-spring-boot-and-mongodb-over-tls-using-certificates
     * @return
     * @throws Exception
     */
    public SSLContext createSSLContext() throws Exception {
        // root CA
        TrustManagerFactory tmf;
        ClassPathResource resource = new ClassPathResource(caFileName);
        InputStream is = resource.getInputStream();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate caCert = (X509Certificate) cf.generateCertificate(is);
        tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null); // You don't need the KeyStore instance to come from a file.
        ks.setCertificateEntry("caCert", caCert);
        tmf.init(ks);

        // client key
        KeyManagerFactory keyFac;
        SSLContext sslContext = null;
        try {
            resource = new ClassPathResource(certificateKeyFileName);
            is = resource.getInputStream();
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(null); // needs to be initialised, otherwise throws exception

            @SuppressWarnings("resource")
            PEMParser pemParser = new PEMParser(new InputStreamReader(is));
            
            Object object = null;
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
                }else{
                    System.out.println("Unknown object: " + object.getClass().getName());
                }
            }

            keystore.setKeyEntry("alias", privateKey, keyPWD.toCharArray(), new Certificate[]{certificate});
            keyFac = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyFac.init(keystore, keyPWD.toCharArray());

            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(keyFac.getKeyManagers(), tmf.getTrustManagers(), null);
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
