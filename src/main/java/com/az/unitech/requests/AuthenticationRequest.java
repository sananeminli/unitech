package com.az.unitech.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthenticationRequest {
    private String pin;
    private String password;
    public AuthenticationRequest(String pin, String password) {
        this.pin = pin;
        this.password = password;
    }
}
