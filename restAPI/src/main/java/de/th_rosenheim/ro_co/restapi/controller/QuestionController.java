package de.th_rosenheim.ro_co.restapi.controller;
import de.th_rosenheim.ro_co.restapi.dto.*;
import de.th_rosenheim.ro_co.restapi.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RequestMapping("api/v1/question")
@RestController
public class QuestionController {

    private QuestionService questionService;

    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @Operation(summary = "Get question by ID", description = "Retrieve the details of a question by their unique ID.")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OutQuestionDto> getQuestion(@PathVariable String id) {
        Optional<OutQuestionDto> question = this.questionService.getQuestion(id);
        return question.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all questions", description = "Retrieve a list of all questions.")
    @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OutQuestionDto>> getAllQuestions(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<OutQuestionDto> questions = this.questionService.getAllQuestions(page, size);
        return ResponseEntity.ok(questions.getContent());
    }

    @Operation(summary = "Create a new question", description = "Add a new question to the system.")
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OutQuestionDto> postQuestion(@Valid @RequestBody InQuestionDto questionDto) {
        Optional<OutQuestionDto> responseDTO = this.questionService.addQuestion(questionDto);
        if (responseDTO.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        URI uri = UriComponentsBuilder
                .fromPath("/api/v1/question")
                .buildAndExpand(responseDTO.get().getId())
                .toUri();
        return ResponseEntity.created(uri).body(responseDTO.get());
    }

    @Operation(summary = "Update question by ID", description = "Modify the details of an existing question by their unique ID.")
    @PutMapping("/{id}/status")
    public ResponseEntity<OutQuestionDto> updateQuestion(@PathVariable String id, @Valid @RequestBody InStatusQuestionDto statusQuestionDto) {
        String userMail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<OutQuestionDto> updatedQuestion = this.questionService.updateStatusQuestion(id, statusQuestionDto, userMail);
        return updatedQuestion.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }



    @Operation(summary = "Delete a question by ID", description = "Remove a question from the system by their unique ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteQuestion(@PathVariable String id) {
        String userMail = SecurityContextHolder.getContext().getAuthentication().getName();
        this.questionService.deleteQuestion(id, userMail);
        return ResponseEntity.ok().build();
    }




}
