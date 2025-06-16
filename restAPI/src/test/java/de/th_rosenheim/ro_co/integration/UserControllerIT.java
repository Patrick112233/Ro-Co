package de.th_rosenheim.ro_co.integration;

import de.th_rosenheim.ro_co.restapi.model.Role;
import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import static de.th_rosenheim.ro_co.restapi.model.User.instantiateUser;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("IntTest")
@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = de.th_rosenheim.ro_co.restapi.RoCoRest.class
)
class UserControllerIT {

    static final String MONGO_INITDB_ROOT_USERNAME = "deinUser";
    static final String MONGO_INITDB_ROOT_PASSWORD = "deinPasswort";

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    private static String accessToken;
    private static String userId;
    private static String adminId;
    private static String adminToken;

    @Container
    public static GenericContainer mongoDBContainer = new GenericContainer(DockerImageName.parse("mongo:6.0.21"))
            .withExposedPorts(27017)
            .withEnv("MONGO_INITDB_ROOT_USERNAME", MONGO_INITDB_ROOT_USERNAME)
            .withEnv("MONGO_INITDB_ROOT_PASSWORD", MONGO_INITDB_ROOT_PASSWORD)
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("mongo-init.js"),
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
        registry.add("server.ssl.key-password", () -> "123456");
        registry.add("server.ssl.key-store-password", () -> "123456");
        registry.add("server.ssl.key-store", () -> "classpath:certs/RoCoTLS.p12");
    }

    @BeforeAll
    static void setupUnirest() {
        mongoDBContainer.start();
        Unirest.config().reset();
        Unirest.config().verifySsl(false);
        Unirest.config().addDefaultHeader("Content-Type", "application/json");
        Unirest.config().connectTimeout(5000);
    }

    @AfterAll
    static void tearDown() {
        mongoDBContainer.stop();
        mongoDBContainer.close();
        System.out.println("MongoDB logs: " + mongoDBContainer.getLogs());
    }

    @BeforeEach
    void setupUsersAndLogin() throws JSONException {
        // User anlegen
        String email = "test@mail.com";
        String username = "testuser";
        String password = "Test1234!";
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User u = instantiateUser(email, password, username, Role.USER.getRole());
            u.setVerified(true);
            return userRepository.insert(u);
        });
        this.userId = user.getId();

        // Admin anlegen
        String adminEmail = "admin@mail.com";
        String adminPassword = "Admin1234!";
        User admin = userRepository.findByEmail(adminEmail).orElseGet(() -> {
            User a = instantiateUser(adminEmail, adminPassword, "admin", Role.ADMIN.getRole());
            a.setVerified(true);
            return userRepository.insert(a);
        });
        this.adminId = admin.getId();

        // Login User
        if (accessToken == null) {
            JSONObject loginBody = new JSONObject();
            loginBody.put("email", email);
            loginBody.put("password", password);

            HttpResponse<String> loginResponse = Unirest.post(getBaseUrl() + "/api/v1/auth/login")
                    .body(loginBody.toString())
                    .asString();

            assertEquals(200, loginResponse.getStatus());
            JSONObject loginJson = new JSONObject(loginResponse.getBody());
            accessToken = loginJson.getString("token");
        }

        // Login Admin
        if (adminToken == null) {
            JSONObject loginBody = new JSONObject();
            loginBody.put("email", adminEmail);
            loginBody.put("password", adminPassword);

            HttpResponse<String> loginResponse = Unirest.post(getBaseUrl() + "/api/v1/auth/login")
                    .body(loginBody.toString())
                    .asString();

            assertEquals(200, loginResponse.getStatus());
            JSONObject loginJson = new JSONObject(loginResponse.getBody());
            adminToken = loginJson.getString("token");
        }
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void testGetAndGenerateUserIcon() {
        // Sicherstellen, dass kein Icon gesetzt ist
        User user = userRepository.findById(userId).orElseThrow();
        assertNull(user.getImage() );

        // Generate Icon
        HttpResponse<byte[]> getIconResp = Unirest.get(getBaseUrl() + "/api/v1/user/" + userId + "/icon")
                .header("Authorization", "Bearer " + accessToken)
                .asBytes();
        assertEquals(200, getIconResp.getStatus());
        String icon1 = new String(getIconResp.getBody());
        assertTrue(icon1.contains("<svg"));

        // Get Icon again (should be the same)
        HttpResponse<byte[]> getIconResp2 = Unirest.get(getBaseUrl() + "/api/v1/user/" + userId + "/icon")
                .header("Authorization", "Bearer " + accessToken)
                .asBytes();
        String icon2 = new String(getIconResp2.getBody());
        assertEquals(icon1, icon2);

        // chaneg Icon
        HttpResponse<byte[]> putIconResp = Unirest.put(getBaseUrl() + "/api/v1/user/icon")
                .header("Authorization", "Bearer " + accessToken)
                .asBytes();
        assertEquals(200, putIconResp.getStatus());
        String icon3 = new String(putIconResp.getBody());
        //check if new icon is different
        assertTrue(icon3.contains("<svg"));
        assertNotEquals(icon1, icon3);
    }

    @Test
    void testUpdateUser() throws Exception {
        // Username ändern
        JSONObject updateBody = new JSONObject();
        updateBody.put("username", "newName");

        HttpResponse<String> updateResp = Unirest.put(getBaseUrl() + "/api/v1/user/")
                .header("Authorization", "Bearer " + accessToken)
                .body(updateBody.toString())
                .asString();

        assertEquals(201, updateResp.getStatus());
        JSONObject updatedUser = new JSONObject(updateResp.getBody());
        assertEquals("newName", updatedUser.getString("username"));

        // In DB prüfen
        User user = userRepository.findById(userId).orElseThrow();
        assertEquals("newName", user.getDisplayName());
    }

    @Test
    void testResetPassword() throws Exception {
        // Altes Passwort (verschlüsselt) merken
        User user = userRepository.findById(userId).orElseThrow();
        String oldEncrypted = user.getPassword();

        // Passwort ändern
        JSONObject pwBody = new JSONObject();
        pwBody.put("email", "test@mail.com");
        pwBody.put("password", "TEst1234!");

        HttpResponse<String> pwResp = Unirest.put(getBaseUrl() + "/api/v1/user/password")
                .header("Authorization", "Bearer " + accessToken)
                .body(pwBody.toString())
                .asString();

        assertEquals(200, pwResp.getStatus());

        // Neues Passwort in DB prüfen
        User updated = userRepository.findById(userId).orElseThrow();
        assertNotEquals(oldEncrypted, updated.getPassword());

        // Login mit neuem Passwort
        JSONObject loginBody = new JSONObject();
        loginBody.put("email", "test@mail.com");
        loginBody.put("password", "TEst1234!");

        HttpResponse<String> loginResp = Unirest.post(getBaseUrl() + "/api/v1/auth/login")
                .body(loginBody.toString())
                .asString();
        assertEquals(200, loginResp.getStatus());
        JSONObject loginJson = new JSONObject(loginResp.getBody());
        assertNotNull(loginJson.getString("token"));
    }

    @Test
    void testDeleteUser() {
        // User löschen
        HttpResponse<String> delResp = Unirest.delete(getBaseUrl() + "/api/v1/user/" + userId)
                .header("Authorization", "Bearer " + adminToken) // Admin-Rechte nötig
                .asString();
        assertEquals(200, delResp.getStatus());

        // In DB prüfen
        assertFalse(userRepository.findById(userId).isPresent());
    }
}