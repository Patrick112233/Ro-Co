package de.th_rosenheim.ro_co.integration;

import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@ActiveProfiles("IntTest")
@Testcontainers
@SpringBootTest(classes = de.th_rosenheim.ro_co.restapi.RoCoRest.class)
class MongoTLSConnectionIT {

    static final String MONGO_CERT_STRING = "C=GE,ST=BY,L=Rosenheim,O=RoCo,OU=RoCoAPI,CN=RoCoAPI";//"CN=RoCoAPI,OU=RoCoAPI,O=RoCo,L=Rosenheim,ST=BY,C=GE";
    static final Path REPO_ROOT_DIR;

    static {
        try {
            // Ermittle das Projekt-Root-Verzeichnis OS-agnostisch
            REPO_ROOT_DIR = Paths.get(System.getProperty("user.dir")).resolve("..").toRealPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    private UserRepository userRepository;

    private static final GenericContainer<?> mongoDBContainer = new GenericContainer<>(DockerImageName.parse("mongo:6.0.21"))
            .withExposedPorts(27017)
            .withEnv("MONGO_ROCO_CERT_STRING", MONGO_CERT_STRING)
            .withCopyFileToContainer(MountableFile.forHostPath(REPO_ROOT_DIR.resolve("db").resolve("mongod.conf").toString()), "/etc/mongod.conf")
            .withFileSystemBind(REPO_ROOT_DIR.resolve("db").resolve("out").toString(), "/etc/ssl/", BindMode.READ_ONLY)
            .withCopyFileToContainer(MountableFile.forHostPath(REPO_ROOT_DIR.resolve("db").resolve("mongo-init.js").toString()), "/docker-entrypoint-initdb.d/mongo-init.js")
            .withCommand("mongod --quiet --config /etc/mongod.conf --auth")
            .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forListeningPort())
            .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forLogMessage(".*MongoDB init process complete.*", 1));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String host = mongoDBContainer.getHost();
        Integer port = mongoDBContainer.getMappedPort(27017);
        registry.add("spring.data.mongodb.host", () -> host);
        registry.add("spring.data.mongodb.port", () -> port);
        registry.add("spring.data.mongodb.database", () -> "RoCoDB");
        registry.add("spring.data.mongodb.auto-index-creation", () -> true);
        // Use the Root CA for verification, not the server certificate
        registry.add("tls.caFile.name", () -> "certs/RoCoRootCA.pem");
        registry.add("tls.certFile.name", () -> "certs/RoCoAPI.pem");
        registry.add("security.enabled", () -> false);
        registry.add("security.keyPWD", () -> "123456");
        registry.add("security.x509.user", () -> MONGO_CERT_STRING);

    }

    @BeforeAll
    static void setup() {
        // check for pem files for DB certs
        if (!new File(REPO_ROOT_DIR.resolve("db").resolve("out").resolve("RoCoRootCA.pem").toUri()).exists()) {
            Path certsScriptPath = REPO_ROOT_DIR.resolve("db").resolve("Certs.sh"); //reqires bash!
            System.out.println("missing certificate, pleas generating new one via " + certsScriptPath);
            throw new Error("missing certificate, pleas generating new one via " + certsScriptPath);
        } else {
            // Copy required PEMs to src/test/resources/certs
            //Path srcServerPem = REPO_ROOT_DIR.resolve("db").resolve("out").resolve("RoCoDB.pem");
            //Path dstServerPem = REPO_ROOT_DIR.resolve("restapi").resolve("src").resolve("test").resolve("resources").resolve("certs").resolve("RoCoDB.pem");
            Path srcClientPem = REPO_ROOT_DIR.resolve("db").resolve("out").resolve("RoCoAPI.pem");
            Path dstClientPem = REPO_ROOT_DIR.resolve("restapi").resolve("src").resolve("test").resolve("resources").resolve("certs").resolve("RoCoAPI.pem");
            Path srcRootCA = REPO_ROOT_DIR.resolve("db").resolve("out").resolve("RoCoRootCA.pem");
            Path dstRootCA = REPO_ROOT_DIR.resolve("restapi").resolve("src").resolve("test").resolve("resources").resolve("certs").resolve("RoCoRootCA.pem");
            try {
                //Files.copy(srcServerPem, dstServerPem, StandardCopyOption.REPLACE_EXISTING);
                Files.copy(srcClientPem, dstClientPem, StandardCopyOption.REPLACE_EXISTING);
                Files.copy(srcRootCA, dstRootCA, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new Error("Could not copy pem file to test resources", e);
            }
        }
        mongoDBContainer.start();
    }

    @Test
    public void testDBConnection() {
        long userCount = userRepository.count();
        System.out.println("User count in the database: " + userCount);
    }

    @AfterAll
    static void tearDown() {
        System.out.println("MongoDB logs: " + mongoDBContainer.getLogs());
    }
}