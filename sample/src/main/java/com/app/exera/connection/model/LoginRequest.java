package com.app.exera.connection.model;

/**
 * Created by AndreHF on 11/14/2017.
 */

public class LoginRequest {
    public LoginRequest setUsername(String username) {
        this.username = username;
        return this;
    }

    public LoginRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    private String username;
    private String password;
}
