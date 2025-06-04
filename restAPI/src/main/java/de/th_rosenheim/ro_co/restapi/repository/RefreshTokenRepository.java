package de.th_rosenheim.ro_co.restapi.repository;

import de.th_rosenheim.ro_co.restapi.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RefreshTokenRepository  extends MongoRepository<RefreshToken, String> {


    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
