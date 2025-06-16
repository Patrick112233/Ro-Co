package de.th_rosenheim.ro_co.restapi.controller;

import de.th_rosenheim.ro_co.restapi.dto.InAnswerDto;
import de.th_rosenheim.ro_co.restapi.dto.OutAnswerDto;
import de.th_rosenheim.ro_co.restapi.dto.OutUseAnonymDto;
import de.th_rosenheim.ro_co.restapi.service.AnswerService;
import de.th_rosenheim.ro_co.restapi.service.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@WebMvcTest(AnswerController.class)
class AnswerControllerTest {

    @MockitoBean
    private AnswerService answerService;

    @MockitoBean
    private JwtService jwtService;
    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public UserDetailsService userDetailsService() {
            return new InMemoryUserDetailsManager(
                    User.withUsername("user@mail.com")
                            .password("{noop}Test1234!")
                            .roles("USER")
                            .build()
            );
        }
    }

    @Test
    @WithMockUser(username = "user@mail.com", roles = {"USER"})
    void getAnswer() throws Exception {

        // Normalfall
        OutAnswerDto out = new OutAnswerDto();
        out.setId("1");
        out.setDescription("desc");
        out.setAuthor(new OutUseAnonymDto("1", "User Name"));
        out.setQuestionID("q1");
        out.setCreatedAt(new java.util.Date());

        Mockito.when(answerService.getAnswer("1")).thenReturn(Optional.of(out));
        mockMvc.perform(get("/api/v1/answer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.description").value("desc"))
                .andExpect(jsonPath("$.author").exists())
                .andExpect(jsonPath("$.questionID").value("q1"))
                .andExpect(jsonPath("$.createdAt").exists());

        // NotFound
        Mockito.when(answerService.getAnswer("2")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/answer/2"))
                .andExpect(status().isNotFound());

        // Fehlerfall (Exception)
        Mockito.when(answerService.getAnswer("3")).thenThrow(new IllegalArgumentException("Error"));
        mockMvc.perform(get("/api/v1/answer/3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    void getAnswer_noAuth() throws Exception {
        mockMvc.perform(get("/api/v1/answer/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@mail.com", roles = {"USER"})
    void getAllAnswers() throws Exception {

        // Paging
        List<OutAnswerDto> allAnswers = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            OutAnswerDto dto = new OutAnswerDto();
            dto.setId(String.valueOf(i));
            dto.setDescription("desc" + i);
            dto.setAuthor(new OutUseAnonymDto("1", "User Name"));
            dto.setQuestionID("q1");
            dto.setCreatedAt(new java.util.Date());
            allAnswers.add(dto);
        }
        Mockito.when(answerService.getAllAnswers("q1", 0, 10))
                .thenReturn(new PageImpl<>(allAnswers.subList(0, 10), PageRequest.of(0, 10), 21));
        Mockito.when(answerService.getAllAnswers("q1", 1, 10))
                .thenReturn(new PageImpl<>(allAnswers.subList(10, 20), PageRequest.of(1, 10), 21));
        Mockito.when(answerService.getAllAnswers("q1", 2, 10))
                .thenReturn(new PageImpl<>(allAnswers.subList(20, 21), PageRequest.of(2, 10), 21));

        mockMvc.perform(get("/api/v1/answer/all/q1?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("0"))
                .andExpect(jsonPath("$[9].id").value("9"));

        mockMvc.perform(get("/api/v1/answer/all/q1?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("10"))
                .andExpect(jsonPath("$[9].id").value("19"));

        mockMvc.perform(get("/api/v1/answer/all/q1?page=2&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("20"));

        // Simmulate Error in service
        Mockito.when(answerService.getAllAnswers(anyString(), anyInt(), anyInt()))
                .thenThrow(new IllegalArgumentException("Invalid page or size"));
        mockMvc.perform(get("/api/v1/answer/all/q1?page=-1&size=10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());

        // Default-Parameter
        List<OutAnswerDto> answers = List.of(new OutAnswerDto());
        Mockito.when(answerService.getAllAnswers(eq("q1"), eq(0), eq(10)))
                .thenReturn(new PageImpl<>(answers, PageRequest.of(0, 10), 1));
        mockMvc.perform(get("/api/v1/answer/all/q1"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllAnswers_noAuth() throws Exception {
        mockMvc.perform(get("/api/v1/answer/all/q1"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(username = "user@mail.com", roles = {"USER"})
    void postAnswer() throws Exception {
        // Normalfall
        OutAnswerDto out = new OutAnswerDto();
        out.setId("123");
        out.setDescription("desc");
        Mockito.when(answerService.addAnswer(any(InAnswerDto.class))).thenReturn(Optional.of(out));
        mockMvc.perform(post("/api/v1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"desc\",\"authorID\":\"1\",\"questionID\":\"q1\"}")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/answer"))
                .andExpect(jsonPath("$.id").value("123"));

        // Ungültiges JSON
        mockMvc.perform(post("/api/v1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"desc\",\"authorID\":\"1\"\"questionID\":\"q1\"}") // Semicolon instead of comma
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());

        // Ungültige Eingabe (zu kurz, null)
        mockMvc.perform(post("/api/v1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"\",\"authorID\":\"1\",\"questionID\":\"q1\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());

        mockMvc.perform(post("/api/v1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"desc\",\"authorID\":null,\"questionID\":\"q1\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());


        // Fehlerfall
        Mockito.when(answerService.addAnswer(any(InAnswerDto.class))).thenThrow(new RuntimeException("DB error"));
        mockMvc.perform(post("/api/v1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"desc\",\"authorID\":\"1\",\"questionID\":\"q1\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    void postAnswer_noAuth() throws Exception {
        mockMvc.perform(post("/api/v1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"desc\",\"authorID\":\"1\",\"questionID\":\"q1\"}")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.com", roles = {"USER"})
    void deleteAnswer() throws Exception {
        // Normalfall
        Mockito.doNothing().when(answerService).deleteAnswer("1", "user@mail.com");
        mockMvc.perform(delete("/api/v1/answer/1").with(csrf()))
                .andExpect(status().isOk());

        // Fehlerfall
        Mockito.doThrow(new RuntimeException("Delete error")).when(answerService).deleteAnswer(anyString(), anyString());
        mockMvc.perform(delete("/api/v1/answer/1").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    void deleteAnswer_noAuth() throws Exception {
        mockMvc.perform(delete("/api/v1/answer/1").with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}