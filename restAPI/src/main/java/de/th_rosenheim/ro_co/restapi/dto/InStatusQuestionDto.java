package de.th_rosenheim.ro_co.restapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InStatusQuestionDto {
    @NotNull
    private boolean answered = false;

}
