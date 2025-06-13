package de.th_rosenheim.ro_co.restapi.service;

import de.th_rosenheim.ro_co.restapi.dto.InQuestionDto;
import de.th_rosenheim.ro_co.restapi.dto.InStatusQuestionDto;
import de.th_rosenheim.ro_co.restapi.model.Question;
import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.QuestionRepository;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.LockedException;

import java.lang.reflect.Field;
import java.util.*;

import static de.th_rosenheim.ro_co.restapi.model.User.instantiateUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuestionServiceTest {

    private QuestionRepository questionRepository;
    private UserRepository userRepository;
    private QuestionService questionService;

    @BeforeEach
    void setUp() {
        questionRepository = mock(QuestionRepository.class);
        userRepository = mock(UserRepository.class);
        questionService = new QuestionService(questionRepository, userRepository);
    }

    @Test
    void getQuestion() throws Exception {
        String validId = "q1";
        User author = instantiateUser("mail@test.com", "Pw123456!", "Author", "USER");
        author.setId("507f1f77bcf86cd799439011");
        author.setVerified(true);

        Question validQuestion = new Question();
        validQuestion.setId(validId);
        validQuestion.setTitle("Title");
        validQuestion.setDescription("Description");
        validQuestion.setCreatedAt(new Date());
        validQuestion.setAuthor(author);

        // Normalfall
        when(questionRepository.findById(validId)).thenReturn(Optional.of(validQuestion));
        assertTrue(questionService.getQuestion(validId).isPresent());

        // Invalid input
        assertThrows(IllegalArgumentException.class, () -> questionService.getQuestion(null));
        assertThrows(IllegalArgumentException.class, () -> questionService.getQuestion(""));

        // Not found
        when(questionRepository.findById("notfound")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> questionService.getQuestion("notfound"));

        // Output-Validation: createdAt null
        Question invalidQ = cloneQuestion(validQuestion);
        Field createdAt = Question.class.getDeclaredField("createdAt");
        createdAt.setAccessible(true);
        createdAt.set(invalidQ, null);
        when(questionRepository.findById("invalid1")).thenReturn(Optional.of(invalidQ));
        assertThrows(ValidationException.class, () -> questionService.getQuestion("invalid1"));

        // Output-Validation: title leer
        invalidQ = cloneQuestion(validQuestion);
        Field title = Question.class.getDeclaredField("title");
        title.setAccessible(true);
        title.set(invalidQ, "");
        when(questionRepository.findById("invalid2")).thenReturn(Optional.of(invalidQ));
        assertThrows(ValidationException.class, () -> questionService.getQuestion("invalid2"));

        // Output-Validation: description leer
        invalidQ = cloneQuestion(validQuestion);
        Field desc = Question.class.getDeclaredField("description");
        desc.setAccessible(true);
        desc.set(invalidQ, "");
        when(questionRepository.findById("invalid3")).thenReturn(Optional.of(invalidQ));
        assertThrows(ValidationException.class, () -> questionService.getQuestion("invalid3"));

        // Output-Validation: title overflow
        invalidQ = cloneQuestion(validQuestion);
        title = Question.class.getDeclaredField("title");
        title.setAccessible(true);
        title.set(invalidQ, "a".repeat(256));
        when(questionRepository.findById("invalid4")).thenReturn(Optional.of(invalidQ));
        assertThrows(ValidationException.class, () -> questionService.getQuestion("invalid4"));

        // Output-Validation: description overflow
        invalidQ = cloneQuestion(validQuestion);
        desc = Question.class.getDeclaredField("description");
        desc.setAccessible(true);
        desc.set(invalidQ, "a".repeat(10001));
        when(questionRepository.findById("invalid5")).thenReturn(Optional.of(invalidQ));
        assertThrows(ValidationException.class, () -> questionService.getQuestion("invalid5"));
    }

    @Test
    void getAllQuestions() throws Exception {
        User author = instantiateUser("mail@test.com", "Pw123456!", "Author", "USER");
        author.setId("507f1f77bcf86cd799439011");
        author.setVerified(true);

        Question q1 = new Question();
        q1.setId("q1");
        q1.setTitle("Title1");
        q1.setDescription("Desc1");
        q1.setCreatedAt(new Date());
        q1.setAuthor(author);

        // Normalfall
        when(questionRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(q1)));
        assertEquals(1, questionService.getAllQuestions(0, 10).getTotalElements());

        // Invalid page/size
        assertThrows(IllegalArgumentException.class, () -> questionService.getAllQuestions(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> questionService.getAllQuestions(0, 0));
        assertThrows(IllegalArgumentException.class, () -> questionService.getAllQuestions(0, 101));

        // findAll gibt null zurück
        when(questionRepository.findAll(any(PageRequest.class))).thenReturn(null);
        assertThrows(NullPointerException.class, () -> questionService.getAllQuestions(0, 10));

        // Invalid Question-Objekt (title leer)
        Question invalidQ = cloneQuestion(q1);
        Field title = Question.class.getDeclaredField("title");
        title.setAccessible(true);
        title.set(invalidQ, "");
        when(questionRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(invalidQ)));
        assertThrows(ValidationException.class, () -> questionService.getAllQuestions(0, 10));

        // Invalid Author-Objekt (displayName null)
        User invalidAuthor = instantiateUser("mail@test.com", "Pw123456!", "displayname", "USER");
        Field field = null;
        try {
            field = User.class.getDeclaredField("displayName");
            field.setAccessible(true);
            field.set(invalidAuthor, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        invalidAuthor.setId("507f1f77bcf86cd799439011");
        invalidAuthor.setVerified(true);
        Question qWithInvalidAuthor = cloneQuestion(q1);
        Field authorField = Question.class.getDeclaredField("author");
        authorField.setAccessible(true);
        authorField.set(qWithInvalidAuthor, invalidAuthor);
        when(questionRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(qWithInvalidAuthor)));
        assertThrows(ValidationException.class, () -> questionService.getAllQuestions(0, 10));

        // UserId darf im Autor nicht enthalten sein (OutUseAnonymDto)
        when(questionRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(q1)));
        var result = questionService.getAllQuestions(0, 10);
        assertNull(result.getContent().get(0).getAuthor().getId());
    }

    @Test
    void addQuestion() throws Exception {
        String validUserId = "507f1f77bcf86cd799439011";
        User author = instantiateUser("mail@test.com", "Pw123456!", "Author", "USER");
        author.setId(validUserId);
        author.setVerified(true);

        InQuestionDto inDto = new InQuestionDto("Title", "Description", "507f1f77bcf86cd799439011");
        Question question = new Question();
        question.setTitle("Title");
        question.setDescription("Description");
        question.setAuthor(author);
        question.setCreatedAt(new Date());

        when(userRepository.findById(validUserId)).thenReturn(Optional.of(author));
        when(questionRepository.insert(any(Question.class))).thenReturn(question);

        // Normalfall
        assertTrue(questionService.addQuestion(inDto).isPresent());
        verify(questionRepository, times(1)).insert(any(Question.class));

        // Invalid input
        assertThrows(ValidationException.class, () -> questionService.addQuestion(null));

        // User not found
        when(userRepository.findById(validUserId)).thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () -> questionService.addQuestion(inDto));

        // Invalid Question (title leer)
        InQuestionDto invalidDto = new InQuestionDto("", "Description", validUserId);
        when(userRepository.findById(validUserId)).thenReturn(Optional.of(author));
        assertThrows(ValidationException.class, () -> questionService.addQuestion(invalidDto));

        // Invalid Author (displayName null)
        User invalidAuthor = instantiateUser("mail@test.com", "Pw123456!", "username", "USER");
        Field field = null;
        try {
            field = User.class.getDeclaredField("displayName");
            field.setAccessible(true);
            field.set(invalidAuthor, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        invalidAuthor.setId(validUserId);
        invalidAuthor.setVerified(true);
        when(userRepository.findById(validUserId)).thenReturn(Optional.of(invalidAuthor));
        assertThrows(ValidationException.class, () -> questionService.addQuestion(inDto));
    }

    @Test
    void deleteQuestion() {
        String qId = "q1";
        String userMail = "mail@test.com";
        User user = instantiateUser(userMail, "Pw123456!", "Author", "USER");
        user.setId("507f1f77bcf86cd799439011");
        user.setVerified(true);

        Question question = new Question();
        question.setId(qId);
        question.setTitle("Title");
        question.setDescription("Description");
        question.setCreatedAt(new Date());
        question.setAuthor(user);

        // Normalfall
        when(userRepository.findByEmail(userMail)).thenReturn(Optional.of(user));
        when(questionRepository.findById(qId)).thenReturn(Optional.of(question));
        assertDoesNotThrow(() -> questionService.deleteQuestion(qId, userMail));
        verify(questionRepository, times(1)).deleteById(qId);

        // Invalid input
        assertThrows(IllegalArgumentException.class, () -> questionService.deleteQuestion(null, userMail));
        assertThrows(IllegalArgumentException.class, () -> questionService.deleteQuestion("", userMail));
        assertThrows(IllegalArgumentException.class, () -> questionService.deleteQuestion(qId, null));
        assertThrows(IllegalArgumentException.class, () -> questionService.deleteQuestion(qId, ""));

        // User not found
        when(userRepository.findByEmail(userMail)).thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () -> questionService.deleteQuestion(qId, userMail));

        // Question not found
        when(userRepository.findByEmail(userMail)).thenReturn(Optional.of(user));
        when(questionRepository.findById(qId)).thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () -> questionService.deleteQuestion(qId, userMail));

        // User != Author
        User otherUser = instantiateUser("other@mail.com", "Pw123456!", "Other", "USER");
        otherUser.setId("507f1f77bcf86cd799439012");
        otherUser.setVerified(true);
        question.setAuthor(otherUser);
        when(userRepository.findByEmail(userMail)).thenReturn(Optional.of(user));
        when(questionRepository.findById(qId)).thenReturn(Optional.of(question));
        assertThrows(LockedException.class, () -> questionService.deleteQuestion(qId, userMail));
    }

    @Test
    void updateStatusQuestion() throws Exception {
        String qId = "q1";
        String userMail = "mail@test.com";
        User user = instantiateUser(userMail, "Pw123456!", "Author", "USER");
        user.setId("507f1f77bcf86cd799439011");
        user.setVerified(true);

        Question question = new Question();
        question.setId(qId);
        question.setTitle("Title");
        question.setDescription("Description");
        question.setCreatedAt(new Date());
        question.setAuthor(user);
        question.setAnswered(false);

        InStatusQuestionDto statusDto = new InStatusQuestionDto(true);

        // Normalfall
        when(userRepository.findByEmail(userMail)).thenReturn(Optional.of(user));
        when(questionRepository.findById(qId)).thenReturn(Optional.of(question));
        when(questionRepository.save(any(Question.class))).thenReturn(question);
        var result = questionService.updateStatusQuestion(qId, statusDto, userMail);
        assertTrue(result.isPresent());
        assertTrue(result.get().isAnswered());

        // Invalid input
        assertThrows(IllegalArgumentException.class, () -> questionService.updateStatusQuestion(null, statusDto, userMail));
        assertThrows(IllegalArgumentException.class, () -> questionService.updateStatusQuestion("", statusDto, userMail));
        assertThrows(IllegalArgumentException.class, () -> questionService.updateStatusQuestion(qId, null, userMail));
        assertThrows(IllegalArgumentException.class, () -> questionService.updateStatusQuestion(qId, statusDto, null));
        assertThrows(IllegalArgumentException.class, () -> questionService.updateStatusQuestion(qId, statusDto, ""));

        // User not found
        when(userRepository.findByEmail(userMail)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> questionService.updateStatusQuestion(qId, statusDto, userMail));

        // Question not found
        when(userRepository.findByEmail(userMail)).thenReturn(Optional.of(user));
        when(questionRepository.findById(qId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> questionService.updateStatusQuestion(qId, statusDto, userMail));

        // User != Author
        User otherUser = instantiateUser("other@mail.com", "Pw123456!", "Other", "USER");
        otherUser.setId("507f1f77bcf86cd799439012");
        otherUser.setVerified(true);
        question.setAuthor(otherUser);
        when(userRepository.findByEmail(userMail)).thenReturn(Optional.of(user));
        when(questionRepository.findById(qId)).thenReturn(Optional.of(question));
        assertThrows(IllegalArgumentException.class, () -> questionService.updateStatusQuestion(qId, statusDto, userMail));

        // Invalid Question (title leer)
        Question invalidQ = cloneQuestion(question);
        Field title = Question.class.getDeclaredField("title");
        title.setAccessible(true);
        title.set(invalidQ, "");
        when(questionRepository.findById(qId)).thenReturn(Optional.of(invalidQ));
        assertThrows(IllegalArgumentException.class, () -> questionService.updateStatusQuestion(qId, statusDto, userMail));

        // Invalid Author (displayName null)
        User invalidAuthor = instantiateUser("mail@test.com", "Pw123456!", "displayName", "USER");
        Field field = null;
        try {
            field = User.class.getDeclaredField("displayName");
            field.setAccessible(true);
            field.set(invalidAuthor, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        invalidAuthor.setId("507f1f77bcf86cd799439011");
        invalidAuthor.setVerified(true);
        question.setAuthor(invalidAuthor);
        when(questionRepository.findById(qId)).thenReturn(Optional.of(question));
        assertThrows(IllegalArgumentException.class, () -> questionService.updateStatusQuestion(qId, statusDto, userMail));
    }

    // Hilfsmethode zum Klonen eines Question-Objekts (flach)
    private static Question cloneQuestion(Question q) {
        Question copy = new Question();
        copy.setId(q.getId());
        copy.setTitle(q.getTitle());
        copy.setDescription(q.getDescription());
        copy.setCreatedAt(q.getCreatedAt());
        copy.setAuthor(q.getAuthor());
        copy.setAnswered(q.isAnswered());
        return copy;
    }
}