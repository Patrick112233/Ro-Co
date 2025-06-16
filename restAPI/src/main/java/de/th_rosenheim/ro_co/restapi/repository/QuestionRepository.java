package de.th_rosenheim.ro_co.restapi.repository;

import de.th_rosenheim.ro_co.restapi.model.Question;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.Optional;

public interface QuestionRepository extends MongoRepository<Question, String> {
    @Query(value = "{id: ?0}")
    Optional<Question> findById(@NotNull String id);
}
