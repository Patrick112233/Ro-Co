package de.th_rosenheim.ro_co.restapi.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.Date;

@Data
@Document(collection="Answer")
public class Answer {

    @Id
    private String id;
    @NotNull
    @Size(min=1, max = 10000)
    private String description;
    @NotNull
    private Date createdAt = new Date(System.currentTimeMillis());
    private boolean deleted = false;

    @NotNull
    @DocumentReference(lazy = true)
    private User author;

    @NotNull
    @DocumentReference(lazy = true)
    private Question question;
}
