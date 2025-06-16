package de.th_rosenheim.ro_co.restapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class InQuestionDto {
    @NotNull
    @Size(min=1, max = 255)
    private String title;
    @Size(min=1, max = 10000)
    private String description;
    @NotNull
    private String authorId;
}
