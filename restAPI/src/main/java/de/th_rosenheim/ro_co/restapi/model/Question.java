package de.th_rosenheim.ro_co.restapi.model;

import de.th_rosenheim.ro_co.restapi.mapper.QuestionMapper;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Document(collection="Question")
public class Question {

    @Id
    private String id;
    @NotNull
    @Size(min=1, max = 255)
    private String title;
    @Size(min=1, max = 10000)
    private String description;
    @NotNull
    private Date createdAt = new Date(System.currentTimeMillis());

    private boolean answered = false;
    private boolean deleted = false;

    @NotNull
    @DocumentReference(lazy = true)
    private User author;

    @DocumentReference(lazy = true)
    private transient List<Answer> answers = new ArrayList<>();



}
