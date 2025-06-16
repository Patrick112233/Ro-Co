package de.th_rosenheim.ro_co.restapi.controller;

import de.th_rosenheim.ro_co.restapi.dto.InQuestionDto;
import de.th_rosenheim.ro_co.restapi.dto.InStatusQuestionDto;
import de.th_rosenheim.ro_co.restapi.dto.OutQuestionDto;
import de.th_rosenheim.ro_co.restapi.service.JwtService;
import de.th_rosenheim.ro_co.restapi.service.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@WebMvcTest(QuestionController.class)
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestionService questionService;
    @MockitoBean
    private JwtService jwtService;

    private OutQuestionDto questionDto;


    @Nested
    @TestConfiguration
    class TestSecurityConfig {
        @Bean
        public UserDetailsService userDetailsService() {
            return new InMemoryUserDetailsManager(
                    User.withUsername("user@mail.com")
                            .password("Test1234!")
                            .roles("USER")
                            .build()
            );
        }
    }

    @BeforeEach
    void setUp() {
        questionDto = new OutQuestionDto();
        questionDto.setId("1");
        questionDto.setTitle("TestTitle");
        questionDto.setDescription("TestDescription");
    }

    @Test
    @WithMockUser(username = "user@mail.com", roles = {"USER"}, password ="Test1234!")
    void getQuestion() throws Exception {
        Mockito.when(questionService.getQuestion("1")).thenReturn(Optional.of(questionDto));

        mockMvc.perform(get("/api/v1/question/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("TestTitle"))
                .andExpect(jsonPath("$.description").value("TestDescription"));


        // Test for a question that does not exist
        Mockito.when(questionService.getQuestion("2")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/question/2"))
                .andExpect(status().isNotFound());

        // Test for a question that throws an error
        Mockito.when(questionService.getQuestion("3")).thenThrow(new IllegalArgumentException("Error"));

        mockMvc.perform(get("/api/v1/question/3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());
    }


    @Test
    @WithMockUser(username = "user@mail.com", roles = {"USER"}, password ="Test1234!")
    void getAllQuestions() throws Exception {
        OutQuestionDto q2 = new OutQuestionDto();
        q2.setId("2");
        q2.setTitle("Title2");
        q2.setDescription("Desc2");

        Mockito.when(questionService.getAllQuestions(0, 1))
                .thenReturn(new PageImpl<>(List.of(questionDto), PageRequest.of(0, 1), 1));
        Mockito.when(questionService.getAllQuestions(1, 1))
                .thenReturn(new PageImpl<>(List.of(q2), PageRequest.of(1, 1), 1));

        mockMvc.perform(get("/api/v1/question/all?page=0&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"));

        mockMvc.perform(get("/api/v1/question/all?page=1&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("2"));


        // Test for trhowing an error
        Mockito.when(questionService.getAllQuestions(anyInt(), anyInt()))
                .thenThrow(new IllegalArgumentException("Invalid page or size"));

        mockMvc.perform(get("/api/v1/question/all?page=-1&size=10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());

    }


    @Test
    @WithMockUser(username = "user@mail.com", roles = {"USER"}, password ="Test1234!")
    void postQuestionl() throws Exception {
        //normal case
        OutQuestionDto outDto = new OutQuestionDto();
        outDto.setId("123");
        outDto.setTitle("Title");
        outDto.setDescription("Desc");

        Mockito.when(questionService.addQuestion(any(InQuestionDto.class))).thenReturn(Optional.of(outDto));

        mockMvc.perform(post("/api/v1/question")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Title\",\"description\":\"Desc\",\"authorId\":\"authorId\"}")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.title").value("Title"));

        // title null
        mockMvc.perform(post("/api/v1/question")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":null,\"description\":\"Desc\",\"authorId\":\"authorId\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        //throws an error
        Mockito.when(questionService.addQuestion(any(InQuestionDto.class))).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/api/v1/question")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Title\",\"description\":\"Desc\",\"authorId\":\"authorId\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());
    }



    @Test
    @WithMockUser(username = "user@mail.com", roles = {"USER"}, password ="Test1234!")
    void updateQuestion() throws Exception {
        OutQuestionDto outDto = new OutQuestionDto();
        outDto.setId("1");
        outDto.setTitle("TestTitle");
        outDto.setDescription("TestDescription");
        outDto.setAnswered(true);

        //1. noraml Context
        Mockito.when(questionService.updateStatusQuestion(eq("1"), any(InStatusQuestionDto.class), eq("user@mail.com")))
                .thenAnswer(invocation -> {
                    InStatusQuestionDto inputDto = invocation.getArgument(1);
                    outDto.setAnswered(inputDto.isAnswered());
                    return Optional.of(outDto);
                });

        mockMvc.perform(put("/api/v1/question/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answered\":true}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.answered").value("true"));


        //2.Test with missing answered field
        Mockito.when(questionService.updateStatusQuestion(eq("2"), any(InStatusQuestionDto.class), eq("user@mail.com")))
                .thenAnswer(invocation -> {
                    InStatusQuestionDto inputDto = invocation.getArgument(1);
                    outDto.setAnswered(inputDto.isAnswered());
                    return Optional.of(outDto);
                });
        mockMvc.perform(put("/api/v1/question/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answered").value("false"));



            //3. Test with invalid JSON
        Mockito.when(questionService.updateStatusQuestion(eq("3"), any(InStatusQuestionDto.class), eq("user@mail.com")))
                .thenAnswer(invocation -> {
                    InStatusQuestionDto inputDto = invocation.getArgument(1);
                    outDto.setAnswered(inputDto.isAnswered());
                    return Optional.of(outDto);
                });

            mockMvc.perform(put("/api/v1/question/3/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"answered\": \"invlidType\"}")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorMessage").exists());

            // 4. Test with trowing an error
            Mockito.when(questionService.updateStatusQuestion(eq("4"), any(InStatusQuestionDto.class), eq("user@mail.com")))
                    .thenThrow(new RuntimeException("Update error"));

            mockMvc.perform(put("/api/v1/question/4/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"answered\":true}")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorMessage").exists());

    }



    @Test
    @WithMockUser(username = "user@mail.com", roles = {"USER"}, password ="Test1234!")
    void deleteQuestion() throws Exception {
            Mockito.doNothing().when(questionService).deleteQuestion("1", "user@mail.com");

            mockMvc.perform(delete("/api/v1/question/1")
                    .with(csrf()))
                    .andExpect(status().isOk());

            //test with throwing an error
            Mockito.doThrow(new RuntimeException("Delete error")).when(questionService).deleteQuestion(anyString(), anyString());

            mockMvc.perform(delete("/api/v1/question/1")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorMessage").exists());

    }
}