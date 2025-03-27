package de.th_rosenheim.ro_co.restapi.model;

import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User{

    /**
     * The ID of the User.
     * This is an oid determined by MongoDB.
     * e.g. "5ff1e194b4f39b6e52a8314f".
     */
    @Id
    private String id;
    private String firstName;
    private String lastName;


}

