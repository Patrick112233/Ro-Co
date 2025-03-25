package de.th_rosenheim.ro_co.restapi.repository;

import de.th_rosenheim.ro_co.restapi.model.Skill;
import de.th_rosenheim.ro_co.restapi.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.HashSet;

/**
 * @see https://docs.spring.io/spring-data/data-mongo/docs/1.5.0.RELEASE/reference/html/mongo.repositories.html
 */
public interface UserRepository extends MongoRepository<User, Long> {

    @Query("{skills: {'$in': '?0'}}")
    Page<User> findBySkills(HashSet<Skill> skills, Pageable pageable);

    Long deleteById(long id);

    public long count();

    User findById(long id);
}
