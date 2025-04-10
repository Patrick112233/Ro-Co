package de.th_rosenheim.ro_co.restapi.repository;
import de.th_rosenheim.ro_co.restapi.model.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.Optional;


public interface UserRepository extends MongoRepository<User, String> {


    @Query("{email: ?0}")
    Optional<User> findByEmail(String email);

    void deleteById(@NotNull String id);

    @Query("{displayName: ?0}")
    Long countByDisplayName(String displayName);
}
