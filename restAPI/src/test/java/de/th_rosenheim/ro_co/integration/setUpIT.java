package de.th_rosenheim.ro_co.integration;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

@ActiveProfiles("IntTest")
@Testcontainers
public abstract class setUpIT {

	// Exact DN of the client certificate subject used for $external user
	protected static final String MONGO_CERT_STRING = "C=GE,ST=BY,L=Rosenheim,O=RoCo,OU=RoCoAPI,CN=RoCoAPI";

	protected static final Path REPO_ROOT_DIR;
	static {
		try {
			// Resolve repository root robustly for both local and CI executions
			Path userDir = Paths.get(System.getProperty("user.dir")).toRealPath();
			if (userDir.getFileName() != null && userDir.getFileName().toString().equals("restAPI")) {
				REPO_ROOT_DIR = userDir.getParent().toRealPath();
			} else {
				REPO_ROOT_DIR = userDir;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Container
	@SuppressWarnings("resource")
	public static final GenericContainer<?> mongoDBContainer = new GenericContainer<>(DockerImageName.parse("mongo:6.0.21"))
			.withExposedPorts(27017)
			.withEnv("MONGO_ROCO_CERT_STRING", MONGO_CERT_STRING)
			.withCopyFileToContainer(MountableFile.forHostPath(REPO_ROOT_DIR.resolve("db").resolve("mongod.conf").toString()), "/etc/mongod.conf")
			.withFileSystemBind(REPO_ROOT_DIR.resolve("db").resolve("out").toString(), "/etc/ssl/", BindMode.READ_ONLY)
			.withCopyFileToContainer(MountableFile.forHostPath(REPO_ROOT_DIR.resolve("db").resolve("mongo-init.js").toString()), "/docker-entrypoint-initdb.d/mongo-init.js")
			.withCommand("mongod --quiet --config /etc/mongod.conf --auth")
			.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("MongoDBContainer")))
			// Wait for the MongoDB port to be ready; avoid relying on specific log messages that may not appear
			.waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(90)));

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		String host = mongoDBContainer.getHost();
		Integer port = mongoDBContainer.getMappedPort(27017);

		registry.add("spring.data.mongodb.host", () -> host);
		registry.add("spring.data.mongodb.port", () -> port);
		registry.add("spring.data.mongodb.database", () -> "RoCoDB");
		registry.add("spring.data.mongodb.auto-index-creation", () -> true);

		// TLS and X.509 properties expected by MongoTLSConfig
		registry.add("tls.caFile.name", () -> "certs/RoCoRootCA.pem");
		registry.add("tls.certFile.name", () -> "certs/RoCoAPI.pem");
		registry.add("security.x509.user", () -> MONGO_CERT_STRING);
		registry.add("security.enabled", () -> false); // allow invalidHostName in tests
		registry.add("security.keyPWD", () -> "123456");

		// Increase Mongo driver logging in CI to diagnose TLS settings and connections
		registry.add("logging.level.org.mongodb", () -> "DEBUG");
	}

	@BeforeAll
	static void ensurePemsOnClasspathAndStart() {
		// Ensure PEMs were generated and copy them into test resources used by classpath
		Path rootCA = REPO_ROOT_DIR.resolve("db").resolve("out").resolve("RoCoRootCA.pem");
		if (!new File(rootCA.toUri()).exists()) {
			Path certsScriptPath = REPO_ROOT_DIR.resolve("db").resolve("Certs.sh");
			throw new Error("Missing certificates. Please generate via " + certsScriptPath);
		}

		Path srcClientPem = REPO_ROOT_DIR.resolve("db").resolve("out").resolve("RoCoAPI.pem");
		Path dstClientPem = REPO_ROOT_DIR.resolve("restAPI").resolve("src").resolve("test").resolve("resources").resolve("certs").resolve("RoCoAPI.pem");
		Path srcRootCA = rootCA;
		Path dstRootCA = REPO_ROOT_DIR.resolve("restAPI").resolve("src").resolve("test").resolve("resources").resolve("certs").resolve("RoCoRootCA.pem");

		// If destination files already exist and are readable (e.g., created by CI setup), skip copying
		boolean dstClientExists = Files.isReadable(dstClientPem);
		boolean dstRootExists = Files.isReadable(dstRootCA);
		if (!dstClientExists || !dstRootExists) {
			try {
				Files.createDirectories(dstClientPem.getParent());
				Files.copy(srcClientPem, dstClientPem, StandardCopyOption.REPLACE_EXISTING);
				Files.copy(srcRootCA, dstRootCA, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				throw new Error("Could not copy PEM files to test resources", e);
			}
		}

		// Container will be started automatically by @Container when first accessed,
		// but we can start explicitly to fail-fast if needed.
		mongoDBContainer.start();
	}

	@org.junit.jupiter.api.AfterAll
	static void stopContainer() {
		if (mongoDBContainer != null) {
			mongoDBContainer.stop();
		}
	}
}
