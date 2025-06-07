package de.th_rosenheim.ro_co.restapi.exceptions;

import de.th_rosenheim.ro_co.restapi.dto.ErrorDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.View;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Configuration
    static class TestConfig {

        @Bean
        public GlobalExceptionHandler globalExceptionHandler(View error) {
            return new GlobalExceptionHandler(error);
        }
        @Bean
        public TestController testController() {
            return new TestController();
        }
    }

    static class TestController {
        @GetMapping("/exception")
        public void throwException() {
            throw new RuntimeException("Test Exception");
        }
    }

    @Test
    void handleException_returnsBadRequestAndErrorDto() throws Exception {

        var res = mockMvc.perform(get("/exception"));
        assertEquals(400, res.andReturn().getResponse().getStatus());
        assertEquals("{\"errorMessage\":\"Bad Request\"}",  res.andReturn().getResponse().getContentAsString());

    }

}