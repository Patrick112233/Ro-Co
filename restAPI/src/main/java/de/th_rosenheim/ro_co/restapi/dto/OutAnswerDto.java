package de.th_rosenheim.ro_co.restapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutAnswerDto {
    @Id
    private String id;
    @NotNull
    @Size(min=1, max = 10000)
    private String description;
    @NotNull
    private Date createdAt;
    @NotNull
    private OutUseAnonymDto author;
    @NotNull
    private String questionID;
}
