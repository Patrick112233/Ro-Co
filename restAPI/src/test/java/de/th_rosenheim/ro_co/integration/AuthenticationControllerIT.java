package de.th_rosenheim.ro_co.integration;

import de.th_rosenheim.ro_co.restapi.model.Role;
import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;

import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static de.th_rosenheim.ro_co.restapi.model.User.instantiateUser;
import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@ActiveProfiles("IntTest")
@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = de.th_rosenheim.ro_co.restapi.RoCoRest.class
)
class AuthenticationControllerIT extends setUpIT {

    @BeforeAll
    static void setUp() { /* base class starts container */ }

    @AfterAll
    static void tearDown() { /* base class manages container lifecycle */ }

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    private String getBaseUrl() {
        return "http://localhost:" + port;
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
            User user = instantiateUser("not@mail.com", "Test123456!", username, Role.USER.getRole());
            userRepository.insert(user);

            //test if username is not available anymore
            response = Unirest.get(baseUrl).asString();
            assertEquals(409, response.getStatus(), "Username should not be available after registration");

        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Test for the complete signup and login and logout flow, including fetching the user details of restricted service and testing te refresh tokens.
     * Is simmpler to make it in one test, than to split it up due to the token handling.
     * @throws UnirestException
     */
    @Test
    void testSignupAndLoginAndGetUser() throws UnirestException, JSONException {
        Unirest.config().reset();
        Unirest.config().verifySsl(false);
        Unirest.config().addDefaultHeader("Content-Type", "application/json");
        Unirest.config().connectTimeout(5000);

        String baseUrl = getBaseUrl();

        // Signup
        JSONObject signupBody = new JSONObject();
            signupBody.put("username", "JohnDoe");
            signupBody.put("email", "John@Doe42.com");
            signupBody.put("password", "Test1234!");

        HttpResponse<String> signupResponse = Unirest.post(baseUrl + "/api/v1/auth/signup")
                .body(signupBody.toString())
                .asString();

        assertEquals(201, signupResponse.getStatus(), "Signup failed");

        // Login
        JSONObject loginBody = new JSONObject();
        loginBody.put("email", "John@Doe42.com");
        loginBody.put("password", "Test1234!");

        HttpResponse<String> loginResponse = Unirest.post(baseUrl + "/api/v1/auth/login")
                .body(loginBody.toString())
                .asString();

        assertEquals(200, loginResponse.getStatus(), "Login sollte erfolgreich sein");

        String accessToken, refreshToken, userId;
        JSONObject loginJson = new JSONObject(loginResponse.getBody());
        accessToken = loginJson.getString("token");
        refreshToken = loginJson.getString("refreshToken");
        userId = loginJson.getString("id");


        assertNotNull(accessToken);
        assertNotNull(refreshToken);
        assertNotNull(userId);

        // Get User (restricted service)
        HttpResponse<String> userResponse = Unirest.get(baseUrl + "/api/v1/user/" + userId)
                .header("Authorization", "Bearer " + accessToken)
                .asString();

        assertEquals(200, userResponse.getStatus(), "User-Endpoint sollte mit gültigem Token erreichbar sein");

        JSONObject userJson = new JSONObject(userResponse.getBody());
        assertEquals("JohnDoe", userJson.getString("username"));
        assertEquals("John@Doe42.com", userJson.getString("email"));


        // Refresh JWT-Token
        HttpResponse<String> refreshResponse = Unirest.post(baseUrl + "/api/v1/auth/refresh")
                .header("Authorization", "Bearer " + refreshToken)
                .asString();

        assertEquals(200, refreshResponse.getStatus(), "Refresh sollte erfolgreich sein");
        JSONObject refreshJson = new JSONObject(refreshResponse.getBody());
        String newAccessToken = refreshJson.getString("token");
        String newRefreshToken = refreshJson.getString("refreshToken");
        assertNotNull(newAccessToken);
        assertEquals(refreshToken, newRefreshToken, "Refresh-Token sollte gleich bleiben");

        // Logout
        JSONObject logoutBody = new JSONObject();
        logoutBody.put("refreshToken", refreshToken);

        HttpResponse<String> logoutResponse = Unirest.post(baseUrl + "/api/v1/auth/logout")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .body(logoutBody.toString())
                .asString();

        assertEquals(200, logoutResponse.getStatus(), "Logout sollte erfolgreich sein");

        // Try to refresh after logout -> should fail
        HttpResponse<String> refreshAfterLogout = Unirest.post(baseUrl + "/api/v1/auth/refresh")
                .header("Authorization", "Bearer " + refreshToken)
                .asString();

        assertEquals(400, refreshAfterLogout.getStatus(), "Refresh nach Logout sollte fehlschlagen");


    }



}