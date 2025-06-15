package de.th_rosenheim.ro_co.restapi.service;

import de.th_rosenheim.ro_co.restapi.dto.InAnswerDto;
import de.th_rosenheim.ro_co.restapi.dto.OutAnswerDto;
import de.th_rosenheim.ro_co.restapi.model.Answer;
import de.th_rosenheim.ro_co.restapi.model.Question;
import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.AnswerRepository;
import de.th_rosenheim.ro_co.restapi.repository.QuestionRepository;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.LockedException;

import java.lang.reflect.Field;
import java.util.*;

import static de.th_rosenheim.ro_co.restapi.model.User.instantiateUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnswerServiceTest {

    private AnswerRepository answerRepository;
    private UserRepository userRepository;
    private QuestionRepository questionRepository;
    private AnswerService answerService;

    @BeforeEach
    void setUp() {
        answerRepository = mock(AnswerRepository.class);
        userRepository = mock(UserRepository.class);
        questionRepository = mock(QuestionRepository.class);
        answerService = new AnswerService(answerRepository, userRepository, questionRepository);
    }

    @Test
    void testGetAnswer() throws Exception {
        // Normalfall
        String answerId = "answerId";
        User author = instantiateUser("test@example.com", "Pw123456!", "Author", "USER");
        author.setId("507f1f77bcf86cd799439011");
        author.setVerified(true);
        Question question = new Question();
        question.setId("questionId");
        Answer answer = new Answer();
        answer.setId(answerId);
        answer.setDescription("desc");
        answer.setAuthor(author);
        answer.setQuestion(question);

        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        Optional<OutAnswerDto> result = answerService.getAnswer(answerId);
        assertTrue(result.isPresent());
        assertEquals(answerId, result.get().getId());
        assertEquals("desc", result.get().getDescription());
        assertEquals("507f1f77bcf86cd799439011", result.get().getAuthor().getId());
        assertEquals("questionId", result.get().getQuestionID());

        // Invalid input
        assertThrows(IllegalArgumentException.class, () -> answerService.getAnswer(null));
        assertThrows(IllegalArgumentException.class, () -> answerService.getAnswer(""));

        // No answer found
        when(answerRepository.findById("notfound")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> answerService.getAnswer("notfound"));

        // Output validation: leere description
        answer.setDescription("");
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        assertThrows(ValidationException.class, () -> answerService.getAnswer(answerId));

        // Nested output validation: leerer username
        answer.setDescription("desc");
        Field displayNameField = User.class.getDeclaredField("displayName");
        displayNameField.setAccessible(true);
        displayNameField.set(author, "");
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        assertThrows(ValidationException.class, () -> answerService.getAnswer(answerId));
    }

    @Test
    void testGetAllAnswers() throws Exception {
        // Normalfall mit Pagination
        String questionId = "q1";
        Question question = new Question();
        question.setId(questionId);
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

        List<Answer> answers = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            User author = instantiateUser("user" + i + "@mail.com", "Pw123456!", "User" + i, "USER");
            author.setId("507f1f77bcf86cd799439011");
            author.setVerified(true);
            Answer answer = new Answer();
            answer.setId("a" + i);
            answer.setDescription("desc" + i);
            answer.setAuthor(author);
            answer.setQuestion(question);
            answers.add(answer);
        }
        when(answerRepository.findAllByQuestion(eq(question), any(PageRequest.class)))
                .thenAnswer(invocation -> {
                    PageRequest pr = invocation.getArgument(1);
                    int start = (int) pr.getOffset();
                    int end = Math.min(start + pr.getPageSize(), answers.size());
                    return new PageImpl<>(answers.subList(start, end), pr, answers.size());
                });

        Page<OutAnswerDto> page0 = answerService.getAllAnswers(questionId, 0, 10);
        assertEquals(10, page0.getContent().size());
        assertEquals("a0", page0.getContent().get(0).getId());

        Page<OutAnswerDto> page2 = answerService.getAllAnswers(questionId, 2, 10);
        assertEquals(1, page2.getContent().size());
        assertEquals("a20", page2.getContent().get(0).getId());

        // Invalid input
        assertThrows(IllegalArgumentException.class, () -> answerService.getAllAnswers(questionId, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> answerService.getAllAnswers(questionId, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> answerService.getAllAnswers(questionId, 0, 101));

        // No question found
        when(questionRepository.findById("notfound")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> answerService.getAllAnswers("notfound", 0, 10));

        // No answers found
        when(answerRepository.findAllByQuestion(eq(question), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));
        Page<OutAnswerDto> emptyPage = answerService.getAllAnswers(questionId, 0, 10);
        assertEquals(0, emptyPage.getTotalElements());

        // Output validation: leere description
        answers.get(0).setDescription("");
        when(answerRepository.findAllByQuestion(eq(question), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(answers.get(0)), PageRequest.of(0, 10), 1));
        assertThrows(ValidationException.class, () -> answerService.getAllAnswers(questionId, 0, 10));

        // Nested output validation: leerer username
        answers.get(0).setDescription("desc0");
        Field displayNameField = User.class.getDeclaredField("displayName");
        displayNameField.setAccessible(true);
        displayNameField.set(answers.get(0).getAuthor(), "");
        when(answerRepository.findAllByQuestion(eq(question), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(answers.get(0)), PageRequest.of(0, 10), 1));
        assertThrows(ValidationException.class, () -> answerService.getAllAnswers(questionId, 0, 10));
    }

    @Test
    void testAddAnswer() throws Exception {
        // Normalfall
        InAnswerDto inDto = new InAnswerDto("desc", "507f1f77bcf86cd799439011", "questionId");
        User author = instantiateUser("test@example.com", "Pw123456!", "Author", "USER");
        author.setId("507f1f77bcf86cd799439011");
        author.setVerified(true);
        Question question = new Question();
        question.setId("questionId");
        Answer answer = new Answer();
        answer.setId("answerId");
        answer.setDescription("desc");
        answer.setAuthor(author);
        answer.setQuestion(question);

        when(userRepository.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.of(author));
        when(questionRepository.findById("questionId")).thenReturn(Optional.of(question));
        when(answerRepository.insert(any(Answer.class))).thenReturn(answer);

        Optional<OutAnswerDto> result = answerService.addAnswer(inDto);
        assertTrue(result.isPresent());
        assertEquals("desc", result.get().getDescription());
        assertEquals("507f1f77bcf86cd799439011", result.get().getAuthor().getId());
        assertEquals("questionId", result.get().getQuestionID());
        verify(answerRepository, times(1)).insert(any(Answer.class));

        // Invalid input: null
        assertThrows(ValidationException.class, () -> answerService.addAnswer(null));

        // User not found
        when(userRepository.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () -> answerService.addAnswer(inDto));

        // Question not found
        when(userRepository.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.of(author));
        when(questionRepository.findById("questionId")).thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () -> answerService.addAnswer(inDto));

        // Insert wirft Exception
        when(questionRepository.findById("questionId")).thenReturn(Optional.of(question));
        when(answerRepository.insert(any(Answer.class))).thenThrow(new RuntimeException("DB-Fehler"));
        assertThrows(RuntimeException.class, () -> answerService.addAnswer(inDto));

        // Output validation: leere description
        when(answerRepository.insert(any(Answer.class))).thenReturn(answer);
        answer.setDescription("");
        assertThrows(ValidationException.class, () -> answerService.addAnswer(inDto));

        // Nested output validation: leerer username
        answer.setDescription("desc");
        Field displayNameField = User.class.getDeclaredField("displayName");
        displayNameField.setAccessible(true);
        displayNameField.set(author, "");
        assertThrows(ValidationException.class, () -> answerService.addAnswer(inDto));
    }

    @Test
    void testDeleteAnswer() {
        // Normalfall: User ist verifiziert und Autor
        String answerId = "answerId";
        String userMail = "test@example.com";
        User user = instantiateUser(userMail, "Pw123456!", "Author", "USER");
        user.setId("507f1f77bcf86cd799439011");
        user.setVerified(true);
        Answer answer = new Answer();
        answer.setId(answerId);
        answer.setDescription("desc");
        answer.setAuthor(user);
        answer.setQuestion(new Question());

        when(userRepository.findByEmail(userMail)).thenReturn(Optional.of(user));
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        assertDoesNotThrow(() -> answerService.deleteAnswer(answerId, userMail));
        verify(answerRepository, times(1)).deleteById(answerId);

        // Invalid input
        assertThrows(IllegalArgumentException.class, () -> answerService.deleteAnswer(null, userMail));
        assertThrows(IllegalArgumentException.class, () -> answerService.deleteAnswer("", userMail));
        assertThrows(IllegalArgumentException.class, () -> answerService.deleteAnswer(answerId, null));
        assertThrows(IllegalArgumentException.class, () -> answerService.deleteAnswer(answerId, ""));

        // User not found
        when(userRepository.findByEmail(userMail)).thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () -> answerService.deleteAnswer(answerId, userMail));

        // Answer not found
        when(userRepository.findByEmail(userMail)).thenReturn(Optional.of(user));
        when(answerRepository.findById(answerId)).thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () -> answerService.deleteAnswer(answerId, userMail));

        // User ist nicht Autor und kein Admin
        User otherUser = instantiateUser("other@mail.com", "Pw123456!", "Other", "USER");
        otherUser.setId("507f1f77bcf86cd799432222"); //set other user ID
        otherUser.setVerified(true);
        answer.setAuthor(otherUser);
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        assertThrows(LockedException.class, () -> answerService.deleteAnswer(answerId, userMail));
    }
}