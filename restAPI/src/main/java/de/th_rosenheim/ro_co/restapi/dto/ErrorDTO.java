package de.th_rosenheim.ro_co.restapi.dto;

import lombok.Data;

@Data
public class ErrorDTO {

    private String errorMessage;

    public ErrorDTO(String errorMessage) {
        this.errorMessage = errorMessage;
    }


}
