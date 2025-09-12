import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;

import com.mongodb.client.MongoClient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;

import javax.net.ssl.SSLContext;

import de.th_rosenheim.ro_co.restapi.security.MongoTLSConfig;

package de.th_rosenheim.ro_co.restapi.security;




public class MongoTLSConfigTest {

    private MongoTLSConfig mongoTLSConfig;

     private static final String ROOT_CA_PEM = """
            -----BEGIN CERTIFICATE-----
            MIICHDCCAcGgAwIBAgIUKeizNTVJQj9mltKGopv6m7CTLsIwCgYIKoZIzj0EAwIw
            YzETMBEGA1UEAwwKUm9Db1Jvb3RDQTEPMA0GA1UECwwGUm9Db0NBMQ0wCwYDVQQK
            DARSb0NvMRIwEAYDVQQHDAlSb3NlbmhlaW0xCzAJBgNVBAgMAkJZMQswCQYDVQQG
            EwJHRTAeFw0yNTA5MTIxOTM2MTBaFw0zNTA5MTAxOTM2MTBaMGMxEzARBgNVBAMM
            ClJvQ29Sb290Q0ExDzANBgNVBAsMBlJvQ29DQTENMAsGA1UECgwEUm9DbzESMBAG
            A1UEBwwJUm9zZW5oZWltMQswCQYDVQQIDAJCWTELMAkGA1UEBhMCR0UwWTATBgcq
            hkjOPQIBBggqhkjOPQMBBwNCAATuVuDeqT+1wqxjX3vcQO8Ihy4oDtAK83vlJj0U
            nkxnQcDDq3kyB4OYIO4LI1DdAdMRKTk0+I/pgaAuoArgMqETo1MwUTAdBgNVHQ4E
            FgQUKlGtmjQ9bNH/kpJRCuVslqUc+xQwHwYDVR0jBBgwFoAUKlGtmjQ9bNH/kpJR
            CuVslqUc+xQwDwYDVR0TAQH/BAUwAwEB/zAKBggqhkjOPQQDAgNJADBGAiEAyA+7
            Wf4NqAnvWfmt8WJ8Og19E2XteMtzN7PdqomfnnICIQDaiusvNEtlmI97vOm8tOSh
            iIfWC31DOdl3rMoN7QSlhQ==
            -----END CERTIFICATE-----
            """;

    private static final String API_PEM = """
            -----BEGIN EC PARAMETERS-----
            BggqhkjOPQMBBw==
            -----END EC PARAMETERS-----
            -----BEGIN EC PRIVATE KEY-----
            MHcCAQEEIOhsf3hR2koXerZ+ZEOunOQGqWKmDILSH1NSyMfzvh6woAoGCCqGSM49
            AwEHoUQDQgAETnDFdDDuXjJxeGE/zusbawfLk/dw96TATUBp002idllHlFx37/k7
            IgJJDP+PxB1qziWjvIyBmaKu+TbsXoUbxg==
            -----END EC PRIVATE KEY-----
            -----BEGIN CERTIFICATE-----
            MIICKjCCAdCgAwIBAgIUPenmUMqR1FHIDJ61aFodbeBy4rwwCgYIKoZIzj0EAwIw
            YzETMBEGA1UEAwwKUm9Db1Jvb3RDQTEPMA0GA1UECwwGUm9Db0NBMQ0wCwYDVQQK
            DARSb0NvMRIwEAYDVQQHDAlSb3NlbmhlaW0xCzAJBgNVBAgMAkJZMQswCQYDVQQG
            EwJHRTAeFw0yNTA5MTIyMDIxNDdaFw0zNTA5MTAyMDIxNDdaMGExEDAOBgNVBAMM
            B1JvQ29BUEkxEDAOBgNVBAsMB1JvQ29BUEkxDTALBgNVBAoMBFJvQ28xEjAQBgNV
            BAcMCVJvc2VuaGVpbTELMAkGA1UECAwCQlkxCzAJBgNVBAYTAkdFMFkwEwYHKoZI
            zj0CAQYIKoZIzj0DAQcDQgAETnDFdDDuXjJxeGE/zusbawfLk/dw96TATUBp002i
            dllHlFx37/k7IgJJDP+PxB1qziWjvIyBmaKu+TbsXoUbxqNkMGIwCwYDVR0PBAQD
            AgeAMBMGA1UdJQQMMAoGCCsGAQUFBwMBMB0GA1UdDgQWBBTGIJz29HI9L/CuLDGf
            zIG9zLN6qzAfBgNVHSMEGDAWgBQCSyl3fryhWrp+a98uZl23/WTf7TAKBggqhkjO
            PQQDAgNIADBFAiArzT4bHOLNZ5qtUktvXtR4yVUF9VSvdis6n2E+Jpy1VAIhAJ31
            1naUovAl7HpNv3O8bE8dH5viPsyfTFgjTj5EXRJP
            -----END CERTIFICATE-----
            """;


    @Before
    public void setUp() {
        mongoTLSConfig = new MongoTLSConfig();
        // Set required fields via reflection
        ReflectionTestUtils.setField(mongoTLSConfig, "mongoDbUri", "mongodb://localhost:27017/testdb");
        ReflectionTestUtils.setField(mongoTLSConfig, "mongoDbName", "testdb");
        ReflectionTestUtils.setField(mongoTLSConfig, "caFileName", "ca.pem");
        ReflectionTestUtils.setField(mongoTLSConfig, "certificateKeyFileName", "api.pem");
        ReflectionTestUtils.setField(mongoTLSConfig, "keyPWD", "testpassword");
        ReflectionTestUtils.setField(mongoTLSConfig, "secure", true);
    }

    @Test
    public void testGetDatabaseName() {
        assertEquals("testdb", mongoTLSConfig.getDatabaseName());
    }

    @Test
    public void testMongoClientThrowsWhenSSLContextFails() {
        MongoTLSConfig configSpy = Mockito.spy(mongoTLSConfig);
        try {
            doThrow(new RuntimeException("SSL error")).when(configSpy).createSSLContext();
            configSpy.mongoClient();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Failed to create SSLContext"));
        }
    }

    @Test
    public void testCreateSSLContext() throws Exception {
        // Mock the MongoTLSConfig class
        MongoTLSConfig config = new MongoTLSConfig();

        ReflectionTestUtils.setField(config, "caFileName", new ByteArrayResource(ROOT_CA_PEM.getBytes(StandardCharsets.UTF_8)).getFilename());
        ReflectionTestUtils.setField(config, "certificateKeyFileName", new ByteArrayResource(API_PEM.getBytes(StandardCharsets.UTF_8)).getFilename());

        SSLContext sslContext = config.createSSLContext();
        assertNotNull("SSLContext should not be null", sslContext);
    }

    // Note: Integration tests for successful SSLContext creation would require actual PEM files in test resources.
}