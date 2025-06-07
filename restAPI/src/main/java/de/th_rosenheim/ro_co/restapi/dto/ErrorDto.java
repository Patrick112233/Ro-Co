package de.th_rosenheim.ro_co.restapi.dto;

import lombok.Data;

@Data
public class ErrorDto {

    private String errorMessage;

    public ErrorDto(String errorMessage) {
        this.errorMessage = errorMessage;
    }


}
