package com.app.exera.connection.connect;

import com.app.exera.connection.model.RequestLogin;

import connection.rxconnection.connection.ConnectionManager;

/**
 * Created by AndreHF on 11/14/2017.
 */

public class ServiceManager extends ConnectionManager {
    public void login(RequestLogin loginRequest) {
        subscribe(new LoginCon(loginRequest, getContext()).setLogInfoRequestResponse(true));
    }
}
