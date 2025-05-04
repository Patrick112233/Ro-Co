package de.th_rosenheim.ro_co.restapi.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection="token")
public class RefreshToken {

    @Id
    private String id;
    private String token_hash; //should be always encrypted

}
