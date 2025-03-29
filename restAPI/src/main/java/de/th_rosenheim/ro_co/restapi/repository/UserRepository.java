package de.th_rosenheim.ro_co.restapi.repository;

import com.mongodb.lang.NonNull;
import com.mongodb.lang.NonNullApi;
import de.th_rosenheim.ro_co.restapi.model.Skill;
import de.th_rosenheim.ro_co.restapi.model.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.HashSet;
import java.util.UUID;


public interface UserRepository extends MongoRepository<User, String> {

    /*
    @Query("{skills: {'$in': '?0'}}")
    Page<User> findBySkills(HashSet<Skill> skills, Pageable pageable);
    */
    void deleteById(@NotNull String id);

    public long count();

}
