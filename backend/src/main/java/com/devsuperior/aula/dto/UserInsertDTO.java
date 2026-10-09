package com.devsuperior.aula.dto;

import com.devsuperior.aula.services.validation.UserInsertValid;

import java.io.Serial;

@UserInsertValid
public class UserInsertDTO extends UserDTO {
    @Serial
    private static final long serialVersionUID = 1L;

    private String password;

    public UserInsertDTO() {
        super();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
