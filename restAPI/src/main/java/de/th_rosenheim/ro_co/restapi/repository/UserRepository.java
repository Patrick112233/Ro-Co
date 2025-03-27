package de.th_rosenheim.ro_co.restapi.repository;

import de.th_rosenheim.ro_co.restapi.model.Skill;
import de.th_rosenheim.ro_co.restapi.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.HashSet;


public interface UserRepository extends MongoRepository<User, String> {

    /*
    @Query("{skills: {'$in': '?0'}}")
    Page<User> findBySkills(HashSet<Skill> skills, Pageable pageable);
    */

    void deleteById(String id);

    public long count();

}
