package de.th_rosenheim.ro_co.restapi.service;

import de.th_rosenheim.ro_co.restapi.dto.*;
import de.th_rosenheim.ro_co.restapi.mapper.AnswerMapper;
import de.th_rosenheim.ro_co.restapi.mapper.UserMapper;
import de.th_rosenheim.ro_co.restapi.model.Answer;
import de.th_rosenheim.ro_co.restapi.model.Question;
import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.AnswerRepository;
import de.th_rosenheim.ro_co.restapi.repository.QuestionRepository;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

import static de.th_rosenheim.ro_co.restapi.mapper.Validator.validationCheck;

@Service
public class AnswerService {

    private UserRepository userRepository;
    private AnswerRepository answerRepository;
    private QuestionRepository questionRepository;


    public AnswerService(AnswerRepository answerRepository, UserRepository userRepository, QuestionRepository questionRepository) {
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
    }


    public Optional<OutAnswerDto> getAnswer(String id) {
        // Validate the input ID
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("id is null or empty");
        }
        Optional<Answer> answer = answerRepository.findById(id);
        if (answer.isEmpty()) {
            throw new IllegalArgumentException("Answer not found");
        }
        OutAnswerDto result = AnswerMapper.INSTANCE.answerToOutAnswerDto(answer.get());
        result.setAuthor(UserMapper.INSTANCE.userToOutUseAnonymDto(answer.get().getAuthor()));
        result.setQuestionID(answer.get().getQuestion().getId());
        return Optional.of(validationCheck(result));

    }

    public Page<OutAnswerDto> getAllAnswers(String questionId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid page or size parameters.");
        }
        Optional<Question> question = questionRepository.findById(questionId);
        if (question.isEmpty()) {
            throw new IllegalArgumentException("Question not found");
        }

        PageRequest pageRequest = PageRequest.of(page, size);
        return answerRepository.findAllByQuestion(question.get(),pageRequest).map(answer -> {
            OutAnswerDto dto = AnswerMapper.INSTANCE.answerToOutAnswerDto(answer);
            dto.setQuestionID(question.get().getId());
            dto.setAuthor(UserMapper.INSTANCE.userToOutUseAnonymDto(answer.getAuthor()));
            return validationCheck(dto);
        });
    }

    public Optional<OutAnswerDto> addAnswer(@Valid InAnswerDto answerDto) {
        if (answerDto == null) {
            throw new ValidationException("Answer DTO cannot be null");
        }
        Answer answer = AnswerMapper.INSTANCE.inAnswerDtoToAnswer(answerDto);
        User author = userRepository.findById(answerDto.getAuthorID()).orElseThrow(() -> new ValidationException("Author not found"));
        Question question = questionRepository.findById(answerDto.getQuestionID()).orElseThrow(() -> new ValidationException("Question not found"));
        answer.setAuthor(author);
        answer.setQuestion(question);
        validationCheck(author);
        Answer authorDB = answerRepository.insert(answer);
        OutAnswerDto response = AnswerMapper.INSTANCE.answerToOutAnswerDto(authorDB);
        response.setAuthor(UserMapper.INSTANCE.userToOutUseAnonymDto(author));
        response.setQuestionID(question.getId());
        return Optional.of(validationCheck(response));

    }

    public void deleteAnswer(String id, String userMail) {
        if (id == null || id.isEmpty() || userMail == null || userMail.isEmpty()) {
            throw new IllegalArgumentException("ID or userMail cannot be null or empty");
        }

        User user = userRepository.findByEmail(userMail).orElseThrow(() -> new ValidationException("User not found"));
        Answer answer = answerRepository.findById(id).orElseThrow(() -> new ValidationException("Answer not found"));
        boolean isUserAllowedToDelete = (user.isVerified() && Objects.equals(answer.getAuthor().getId(), user.getId())) || user.getRole().equals("ADMIN");
        if (!isUserAllowedToDelete) {
            throw new AuthenticationException("User not allowed to delete this answer") {};
        }

        answerRepository.deleteById(id);
    }
}
