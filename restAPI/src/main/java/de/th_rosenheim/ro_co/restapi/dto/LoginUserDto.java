package de.th_rosenheim.ro_co.restapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class LoginUserDto {

        @NotNull
        @Pattern(
                regexp = "^((?!\\.)[\\w\\-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])$",
                message = "Invalid email format"
        )
        private String email;

        @NotNull
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%]).{8,24}$",
                message = "Password must be at least 8 characters long and contain at least one letter and one number"
        )
        private String password;

        public LoginUserDto(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public LoginUserDto() {
            // Default constructor for deserialization
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }


}
