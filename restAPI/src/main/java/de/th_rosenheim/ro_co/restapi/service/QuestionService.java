package de.th_rosenheim.ro_co.restapi.service;

import de.th_rosenheim.ro_co.restapi.dto.InQuestionDto;
import de.th_rosenheim.ro_co.restapi.dto.InStatusQuestionDto;
import de.th_rosenheim.ro_co.restapi.dto.OutQuestionDto;
import de.th_rosenheim.ro_co.restapi.mapper.QuestionMapper;
import de.th_rosenheim.ro_co.restapi.mapper.UserMapper;
import de.th_rosenheim.ro_co.restapi.model.Question;
import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.QuestionRepository;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import jakarta.validation.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import static de.th_rosenheim.ro_co.restapi.mapper.Validator.validationCheck;

@Service
public class QuestionService {

    private UserRepository userRepository;
    private QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository, UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    public Optional<OutQuestionDto> getQuestion(String id) {
        // Validate the input ID
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("id is null or empty");
        }
        Optional<Question> question = questionRepository.findById(id);
        if (question.isEmpty()) {
            throw new IllegalArgumentException("Question not found");
        }
        OutQuestionDto result = QuestionMapper.INSTANCE.questionToOutQuestionDto(question.get());
        result.setAuthor(validationCheck(UserMapper.INSTANCE.userToOutUseAnonymDto(question.get().getAuthor())));
        return Optional.of(validationCheck(result));
    }

    public Page<OutQuestionDto> getAllQuestions(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid page or size parameters.");
        }
        PageRequest pageRequest = PageRequest.of(page, size);
        return questionRepository.findAll(pageRequest).map(question -> {
            OutQuestionDto dto = QuestionMapper.INSTANCE.questionToOutQuestionDto(question);
            dto.setAuthor(validationCheck(UserMapper.INSTANCE.userToOutUseAnonymDto(question.getAuthor())));
            return validationCheck(dto);
        });
    }

    public Optional<OutQuestionDto> addQuestion(InQuestionDto questionDto) throws ValidationException {
        if (questionDto == null) {
            throw new ValidationException("Question DTO cannot be null");
        }
        Question question = QuestionMapper.INSTANCE.inQuestionDtoToQuestion(questionDto);
        User author = userRepository.findById(questionDto.getAuthorId()).orElseThrow(() -> new ValidationException("Author not found"));
        question.setAuthor(author);
        validationCheck(question);
        Question questionDB = questionRepository.insert(question);

        OutQuestionDto response = QuestionMapper.INSTANCE.questionToOutQuestionDto(questionDB);
        response.setAuthor(validationCheck(UserMapper.INSTANCE.userToOutUseAnonymDto(author)));
        return Optional.of(validationCheck(response));
    }

    public void deleteQuestion(String id, String userMail) throws IllegalArgumentException {
        if (id == null || id.isEmpty() || userMail == null || userMail.isEmpty()) {
            throw new IllegalArgumentException("ID or userMail cannot be null or empty");
        }

        User user = userRepository.findByEmail(userMail).orElseThrow(() -> new ValidationException("User not found"));
        Question question = questionRepository.findById(id).orElseThrow(() -> new ValidationException("Question not found"));
        boolean isUserAllowedToDelete = (user.isVerified() && Objects.equals(question.getAuthor().getId(), user.getId())) || user.getRole().equals("ADMIN");
        if (!isUserAllowedToDelete) {

            throw new LockedException("User not allowed to delete this question");
        }

        questionRepository.deleteById(id);
    }

    public Optional<OutQuestionDto> updateStatusQuestion(String id, InStatusQuestionDto statusQuestionDto, String userMail) {
        if (id == null || id.isEmpty() || statusQuestionDto == null || userMail == null || userMail.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }

        Optional<User> user = userRepository.findByEmail(userMail);
        Optional<Question> question = questionRepository.findById(id);
        if (user.isEmpty()|| question.isEmpty()) {
            throw new IllegalArgumentException("Eather user or question not found");
        }
        if (!user.get().getId().equals(question.get().getAuthor().getId())) {
            throw new IllegalArgumentException("User is not the author of the question");
        }

        Question questionDB = question.get();
        questionDB.setAnswered(statusQuestionDto.isAnswered());
        questionDB = questionRepository.save(questionDB);
        OutQuestionDto response = QuestionMapper.INSTANCE.questionToOutQuestionDto(questionDB);
        response.setAuthor(validationCheck(UserMapper.INSTANCE.userToOutUseAnonymDto(user.get())));
        return Optional.of(validationCheck(response));
    }
}
