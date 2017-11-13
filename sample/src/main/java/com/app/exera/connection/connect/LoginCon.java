package com.app.exera.connection.connect;

import android.content.Context;

import com.app.exera.connection.model.LoginRequest;
import com.app.exera.connection.model.LoginResponse;

import connection.rxconnection.connection.HttpMethod;
import connection.rxconnection.connection.HttpRequest;

/**
 * Created by AndreHF on 11/14/2017.
 */

public class LoginCon extends HttpRequest<LoginRequest,LoginResponse> {
    public LoginCon(LoginRequest loginRequest, Context context) {
        super(loginRequest, context, LoginResponse.class, URL.LOGIN, HttpMethod.POST);
    }
}
