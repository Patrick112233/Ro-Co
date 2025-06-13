package de.th_rosenheim.ro_co.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.th_rosenheim.ro_co.restapi.model.Question;
import de.th_rosenheim.ro_co.restapi.model.Role;
import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.QuestionRepository;
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
class QuestionControllerIT {

    static final String MONGO_INITDB_ROOT_USERNAME = "deinUser";
    static final String MONGO_INITDB_ROOT_PASSWORD = "deinPasswort";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @LocalServerPort
    private int port;

    private static String accessToken;
    private static String userId;


    @Container
    public static GenericContainer mongoDBContainer = new GenericContainer(DockerImageName.parse("mongo:6.0.21"))
            .withExposedPorts(27017)
            .withEnv("MONGO_INITDB_ROOT_USERNAME", MONGO_INITDB_ROOT_USERNAME) // Passe an, falls in env_file gesetzt
            .withEnv("MONGO_INITDB_ROOT_PASSWORD", MONGO_INITDB_ROOT_PASSWORD) // Passe an, falls in env_file gesetzt
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
        Unirest.config().verifySsl(false);
        Unirest.config().addDefaultHeader("Content-Type", "application/json");
        Unirest.config().connectTimeout(5000);
    }

    @AfterAll
    static void tearDown() {
        //get mongo logs
        mongoDBContainer.stop();
        mongoDBContainer.close();
        System.out.println("MongoDB logs: " + mongoDBContainer.getLogs());
    }

    @BeforeEach
    void setupUserAndLogin() throws JSONException {
        // User anlegen, falls nicht vorhanden
        String email = "testuser@qa.com";
        String username = "testuser";
        String password = "Test1234!";
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User u = instantiateUser(email, password, username, Role.USER.getRole());
            u.setVerified(true);
            return userRepository.insert(u);
        });
        userId = user.getId();

        // Login nur, wenn noch kein Token vorhanden
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
    }

    private String getBaseUrl() {
        return "https://localhost:" + port;
    }

    @Test
    void testBasicQuestionHandling() throws JSONException {
        // Creat Question
        JSONObject questionBody = new JSONObject();
        questionBody.put("title", "Is Programming Hard");
        questionBody.put("description", "I Did wonder weather Programming is considered hard?");
        questionBody.put("authorId", userId);

        HttpResponse<String> createResponse = Unirest.post(getBaseUrl() + "/api/v1/question")
                .header("Authorization", "Bearer " + accessToken)
                .body(questionBody.toString())
                .asString();

        assertEquals(201, createResponse.getStatus());
        JSONObject createdQuestion = new JSONObject(createResponse.getBody());
        String questionId = createdQuestion.getString("id");

        // Get Question by ID
        HttpResponse<String> getResponse = Unirest.get(getBaseUrl() + "/api/v1/question/" + questionId)
                .header("Authorization", "Bearer " + accessToken)
                .asString();

        assertEquals(200, getResponse.getStatus());
        JSONObject getQuestion = new JSONObject(getResponse.getBody());
        assertEquals("Is Programming Hard", getQuestion.getString("title"));
        assertEquals("I Did wonder weather Programming is considered hard?", getQuestion.getString("description"));
        assertEquals(userId, new JSONObject(getQuestion.getString("author")).getString("id"));
        assertFalse(getQuestion.getBoolean("answered"));

        //Set Question Status
        JSONObject statusBody = new JSONObject();
        statusBody.put("answered", true);

        HttpResponse<String> statusResponse = Unirest.put(getBaseUrl() + "/api/v1/question/" + questionId + "/status")
                .header("Authorization", "Bearer " + accessToken)
                .body(statusBody.toString())
                .asString();

        assertEquals(200, statusResponse.getStatus());

        // Get question to check status change
        HttpResponse<String> getAfterStatus = Unirest.get(getBaseUrl() + "/api/v1/question/" + questionId)
                .header("Authorization", "Bearer " + accessToken)
                .asString();

        assertEquals(200, getAfterStatus.getStatus());
        JSONObject afterStatus = new JSONObject(getAfterStatus.getBody());
        assertTrue(afterStatus.getBoolean("answered"));

        // Delete Question
        HttpResponse<String> deleteResponse = Unirest.delete(getBaseUrl() + "/api/v1/question/" + questionId)
                .header("Authorization", "Bearer " + accessToken)
                .asString();

        assertEquals(200, deleteResponse.getStatus());

        // Check if question is deleted
        assertFalse(questionRepository.findById(questionId).isPresent());
    }

    @Test
    void testGetAllQuestionsWithPagination() throws Exception {
        questionRepository.deleteAll();

        for (int i = 1; i <= 21; i++) {
            Question q = new Question();
            q.setTitle("Question " + i);
            q.setDescription("Description " + i);
            q.setAuthor(userRepository.findById(userId).orElseThrow());
            questionRepository.save(q);
        }

        int totalQuestions = 21;
        int pageSize = 7;
        int totalPages = (int) Math.ceil((double) totalQuestions / pageSize);
        int received = 0;
        ObjectMapper objectMapper = new ObjectMapper();

        for (int page = 0; page < totalPages; page++) {
            HttpResponse<String> response = Unirest.get(getBaseUrl() + "/api/v1/question/all")
                    .header("Authorization", "Bearer " + accessToken)
                    .queryString("page", page)
                    .queryString("size", pageSize)
                    .asString();

            assertEquals(200, response.getStatus());
            Object[] arr = objectMapper.readValue(response.getBody(), Object[].class);
            if (page < totalPages - 1) {
                assertEquals(pageSize, arr.length);
            } else {
                assertEquals(totalQuestions - received, arr.length);
            }
            received += arr.length;
        }
        assertEquals(totalQuestions, received);
    }
}