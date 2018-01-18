package com.app.exera.connection.model;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class RequestLogin implements java.io.Serializable {

    public RequestLogin setPassword(String password) {
        this.password = password;
        return this;
    }

    public RequestLogin setUsername(String username) {
        this.username = username;
        return this;
    }

    private String password;
    @SerializedName("grant_type")
    private String grantType = "password";
    @SerializedName("client_secret")
    private String clientSecret = "iBAhimye9KtrTP9tYsGHXW6XyTMczDhGDmaraudy";
    @SerializedName("client_id")
    private String clientId = "3";
    private String username;

}
