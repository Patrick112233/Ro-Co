package de.th_rosenheim.ro_co.restapi.controller;

import de.th_rosenheim.ro_co.restapi.dto.InUserDto;
import de.th_rosenheim.ro_co.restapi.dto.LoginUserDto;
import de.th_rosenheim.ro_co.restapi.dto.OutUserDto;
import de.th_rosenheim.ro_co.restapi.service.JwtService;
import de.th_rosenheim.ro_co.restapi.service.UserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@AutoConfigureMockMvc(addFilters = false)
//@WebMvcTest(UserController.class)

@ActiveProfiles("test")
@AutoConfigureMockMvc
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    @Nested
    @TestConfiguration
    class TestSecurityConfig {
        @Bean
        public UserDetailsService userDetailsService() {
            return new InMemoryUserDetailsManager(
                    User.withUsername("not@mail.com")
                            .password("Test1234!")
                            .roles("USER")
                            .build(),
                    User.withUsername("notadmin@mail.com")
                            .password("Test1234!")
                            .roles("ADMIN")
                            .build()
            );
        }
    }


    @Test
    @WithMockUser(username = "not@mail.com", roles = {"USER"}, password ="Test1234!")
    void getUser() throws Exception {

        // Normall fall
        OutUserDto user = new OutUserDto();
        user.setId("1");
        user.setUsername("TestUser");
        Mockito.when(userService.getUser("1")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/v1/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.username").value("TestUser"));

        // User not found
        Mockito.when(userService.getUser("2")).thenReturn(Optional.empty());
        var res = mockMvc.perform(get("/api/v1/user/2")).andExpect(status().isNotFound());
        assertTrue(res.andReturn().getResponse().getContentAsString().isEmpty());

    }

    @Test
    @WithMockUser(username = "not@mail.com", roles = {"USER"}, password ="Test1234!")
    void updateUser() throws Exception {

        OutUserDto updatedUser = new OutUserDto("1", "Jon Doe", "USER", "not@mail.com", true);
        InUserDto inUserDto = new InUserDto("Jon Doe");
        String id = "1";
        Mockito.when(userService.updateUser("1", inUserDto)).thenReturn(updatedUser);

        mockMvc.perform(put("/api/v1/user/" + id)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"" + inUserDto.getUsername() + "\"}")
        .with(csrf()))
        .andExpect(jsonPath("$.id").value(updatedUser.getId()))
        .andExpect(jsonPath("$.username").value(updatedUser.getUsername()))
        .andExpect(jsonPath("$.role").value(updatedUser.getRole()))
        .andExpect(jsonPath("$.email").value(updatedUser.getEmail()))
        .andExpect(jsonPath("$.verified").value(updatedUser.isVerified()));


        //test with invalid data
        mockMvc.perform(put("/api/v1/user/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"\"}")
                        .with(csrf())) // Empty username
                .andExpect(status().isBadRequest());

        //test with non-existing user
        Mockito.when(userService.updateUser("2", inUserDto)).thenThrow(new IllegalArgumentException("User not found"));
        mockMvc.perform(put("/api/v1/user/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + inUserDto.getUsername() + "\"}")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());

    }

    @Test
    @WithMockUser(username = "not@mail.com", roles = {"USER"}, password ="Test1234!")
    void testResetPassword() throws Exception {
        LoginUserDto inUserDto = new LoginUserDto("not@mail.com", "OldPW1234!");
        String id = "1";
        String newPassword = "newPwd1234!";

        Mockito.doNothing().when(userService).resetPassword(id, inUserDto);
        mockMvc.perform(put("/api/v1/user/" + id + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + inUserDto.getEmail() + "\", \"password\":\"" + newPassword + "\"}")
                .with(csrf()))
                .andExpect(status().isOk());

        // Test with invalid email
        mockMvc.perform(put("/api/v1/user/" + id + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"invalid-email\", \"password\":\"" + newPassword + "\"}")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());

        // Test with empty password
        mockMvc.perform(put("/api/v1/user/" + id + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + inUserDto.getEmail() + "\", \"password\":\"\"}")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());

        // Test with non-existing user
        Mockito.doThrow(new UsernameNotFoundException("User not found")).when(userService).resetPassword(Mockito.anyString(), Mockito.any(LoginUserDto.class));
        mockMvc.perform(put("/api/v1/user/" + id + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + inUserDto.getEmail() + "\", \"password\":\"" + newPassword + "\"}")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    @WithMockUser(username = "notadmin@mail.com", roles = {"ADMIN"}, password ="Test1234!")
    void testGetAllUsers() throws Exception {

        //Mock the service to return a list of users
        OutUserDto user1 = new OutUserDto("1", "UserOne", "USER", "not@mail.com", true);
        OutUserDto user2 = new OutUserDto("2", "UserTwo", "USER", "not@mail.com", true);
        OutUserDto user3 = new OutUserDto("3", "UserThree", "USER", "not@mail.com", true);

        Mockito.when(userService.getAllUsers(Mockito.anyInt(), Mockito.anyInt()))
               .thenReturn(new PageImpl<>(java.util.List.of(user1, user2, user3))); // Test with valid page and size
        mockMvc.perform(get("/api/v1/user/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].username").value("UserOne"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].username").value("UserTwo"))
                .andExpect(jsonPath("$[2].id").value("3"))
                .andExpect(jsonPath("$[2].username").value("UserThree"));

        // Test with specific page and size
        mockMvc.perform(get("/api/v1/user/all?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].username").value("UserOne"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].username").value("UserTwo"))
                .andExpect(jsonPath("$[2].id").value("3"))
                .andExpect(jsonPath("$[2].username").value("UserThree"));


        // Test Illigal Argument
        Mockito.when(userService.getAllUsers(Mockito.anyInt(), Mockito.anyInt()))
                .thenThrow(new IllegalArgumentException("Invalid page or size"));
        mockMvc.perform(get("/api/v1/user/all?page=-1&size=10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());

    }

    @Test
    @WithMockUser(username = "notadmin@mail.com", roles = {"ADMIN"}, password ="Test1234!")
    void deleteUser_asAdmin() throws Exception {
        String id = "1";
        Mockito.doNothing().when(userService).deleteUser(id);

        mockMvc.perform(delete("/api/v1/user/" + id).with(csrf()))
                .andExpect(status().isOk());
        Mockito.verify(userService, Mockito.times(1)).deleteUser(id);

        // Test mit nicht vorhandenem User
        Mockito.doThrow(new IllegalArgumentException("User not found")).when(userService).deleteUser("2");
        mockMvc.perform(delete("/api/v1/user/2").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    @WithMockUser(username = "not@mail.com", roles = {"USER"}, password ="Test1234!")
    void deleteUser_asUser_forbidden() throws Exception {
        String id = "1";
        mockMvc.perform(delete("/api/v1/user/" + id))
                .andExpect(status().isForbidden());
    }
}