package com.app.exera.connection.connect;

import android.content.Context;

import com.app.exera.connection.model.LoginResponse;
import com.app.exera.connection.model.RequestLogin;

import connection.rxconnection.connection.HttpMethod;
import connection.rxconnection.connection.HttpRequest;
import okhttp3.MediaType;

/**
 * Created by AndreHF on 11/14/2017.
 */

public class LoginCon extends HttpRequest<RequestLogin, LoginResponse[]> {
    public LoginCon(RequestLogin loginRequest, Context context) {
        super(loginRequest, context, LoginResponse[].class, URL.LOGIN, HttpMethod.POST);
    }
}
