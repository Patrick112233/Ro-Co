package de.th_rosenheim.ro_co.restapi.controller;

import de.th_rosenheim.ro_co.restapi.dto.*;
import de.th_rosenheim.ro_co.restapi.service.AnswerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RequestMapping("api/v1/answer")
@RestController
public class AnswerController {

    private AnswerService answerService;

    @Autowired
    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @Operation(summary = "Get answer by ID", description = "Retrieve the details of a answer by their unique ID.")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OutAnswerDto> getAnswer(@PathVariable String id) {
        Optional<OutAnswerDto> answer = this.answerService.getAnswer(id);
        return answer.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all answers for a given question", description = "Retrieve a list of all answers for a question.")
    @GetMapping(path = "/all/{questionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OutAnswerDto>> getAllAnswers(@PathVariable String questionId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<OutAnswerDto> answers = this.answerService.getAllAnswers(questionId,page, size);
        return ResponseEntity.ok(answers.getContent());
    }


    @Operation(summary = "Create a new answer", description = "Add a new answer.")
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OutAnswerDto> postAnswer(@Valid @RequestBody InAnswerDto answerDto) {
        Optional<OutAnswerDto> responseDTO = this.answerService.addAnswer(answerDto);
        if (responseDTO.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        URI uri = UriComponentsBuilder
                .fromPath("/api/v1/answer")
                .buildAndExpand(responseDTO.get().getId())
                .toUri();
        return ResponseEntity.created(uri).body(responseDTO.get());
    }

    @Operation(summary = "Delete answer by ID", description = "Remove a answer from the system by their unique ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteAnswer(@PathVariable String id) {
        String userMail = SecurityContextHolder.getContext().getAuthentication().getName();

        this.answerService.deleteAnswer(id, userMail);
        return ResponseEntity.ok().build();
    }




}
