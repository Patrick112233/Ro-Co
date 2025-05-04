package de.th_rosenheim.ro_co.restapi.repository;

import de.th_rosenheim.ro_co.restapi.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RefreshTokenRepository  extends MongoRepository<RefreshToken, String> {

    RefreshToken findByToken(String token);

    void deleteByToken(String token);

    void deleteByUserId(String userId);


}
