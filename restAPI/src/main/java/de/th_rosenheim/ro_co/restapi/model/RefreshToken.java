package de.th_rosenheim.ro_co.restapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection="token")
public class RefreshToken {

    @Id
    private String id;
    private String tokenHash; //should be always encrypted
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date tokenExpires;

}
