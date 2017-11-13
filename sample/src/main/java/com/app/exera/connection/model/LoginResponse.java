package com.app.exera.connection.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by AndreHF on 11/14/2017.
 */

public class LoginResponse {
    @SerializedName("full_name")
    private String fullName;
    private String address;
    private String phone;
}
