package de.th_rosenheim.ro_co.restapi.repository;

import de.th_rosenheim.ro_co.restapi.model.Skill;
import de.th_rosenheim.ro_co.restapi.model.User;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.HashSet;

public interface UserRepository extends PagingAndSortingRepository<User, String> {

    @Query("{skills: '?0'}")
    public User findBySkill(Skill skill);

    public long count();

    User findById(int id);
}
