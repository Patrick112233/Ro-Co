package de.th_rosenheim.ro_co.restapi.repository;

import de.th_rosenheim.ro_co.restapi.model.Answer;
import de.th_rosenheim.ro_co.restapi.model.Question;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AnswerRepository  extends MongoRepository<Answer, String> {

    Optional<Answer> findById(String id);


}
