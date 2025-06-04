package de.th_rosenheim.ro_co.restapi.integration;

import de.th_rosenheim.ro_co.restapi.model.Role;
import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import de.th_rosenheim.ro_co.restapi.security.AuthenticationProviderConfiguration;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@ActiveProfiles("test")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerIntegrationTest {

    static final String MONGO_INITDB_ROOT_USERNAME = "deinUser";
    static final String MONGO_INITDB_ROOT_PASSWORD = "deinPasswort";

    @Container
    public static GenericContainer mongoDBContainer = new GenericContainer(DockerImageName.parse("mongo:6.0.21"))
            .withExposedPorts(27017)
            .withEnv("MONGO_INITDB_ROOT_USERNAME", MONGO_INITDB_ROOT_USERNAME) // Passe an, falls in env_file gesetzt
            .withEnv("MONGO_INITDB_ROOT_PASSWORD", MONGO_INITDB_ROOT_PASSWORD) // Passe an, falls in env_file gesetzt
            .withCopyFileToContainer(
                    MountableFile.forHostPath("mongo-init.js"),
                    "/docker-entrypoint-initdb.d/mongo-init.js"
            )
            .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forLogMessage(".*MongoDB init process complete.*", 1));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String host = mongoDBContainer.getHost();
        Integer port = mongoDBContainer.getMappedPort(27017);
        registry.add("spring.data.mongodb.host", () -> host);
        registry.add("spring.data.mongodb.port", () -> port);
        registry.add("spring.data.mongodb.username", () -> MONGO_INITDB_ROOT_USERNAME);
        registry.add("spring.data.mongodb.password", () -> MONGO_INITDB_ROOT_PASSWORD);
        registry.add("TLSPWD", () -> "123456");
    }


    @BeforeAll
    static void setUp() {
        mongoDBContainer.start();
    }


    @AfterAll
    static void tearDown() {
        //get mongo logs
        mongoDBContainer.stop();
        mongoDBContainer.close();
        System.out.println("MongoDB logs: " + mongoDBContainer.getLogs());
    }

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    private String getBaseUrl() {
        return "https://localhost:" + port;
    }

    @Test
    void testUserNameAvailability() {
        try {

            String username = "testuser" + System.currentTimeMillis();
            Unirest.config().verifySsl(false);
            Unirest.config().addDefaultHeader("Content-Type", "application/json");
            Unirest.config().connectTimeout(5000);

            String baseUrl = getBaseUrl() + "/api/v1/auth/signup/username?username="+username;
            HttpResponse<String> response = null;
            response = Unirest.get(baseUrl).asString();
            assertEquals(200, response.getStatus());

            //Add username via direct DB access
            User user = new User("not@mail.com", AuthenticationProviderConfiguration.passwordEncoder().encode("test1234!"), Role.USER.getRole());
            user.setDisplayName(username);
            User dbUser = userRepository.insert(user);

            //test if username is not available anymore
            response = Unirest.get(baseUrl).asString();
            assertEquals(409, response.getStatus(), "Username should not be available after registration");

        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

}