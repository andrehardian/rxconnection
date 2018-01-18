package com.app.exera.connection;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.app.exera.connection.connect.ServiceManager;
import com.app.exera.connection.model.RequestLogin;

import connection.rxconnection.connection.ConnectionListener;

public class MainActivity extends AppCompatActivity implements ConnectionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login();
    }

    private void login() {
        ((ServiceManager) new ServiceManager().setContext(this).setConnectionListener(this))
                .login(new RequestLogin().setUsername("test@gmail.com").setPassword("12345"));
    }

    @Override
    public void onSuccessWithData(Object o) {
//        success 200
    }

    @Override
    public void onSuccessNull() {
//success 204
    }

    @Override
    public void onMessageSuccess(String s) {
//success 203
    }

    @Override
    public void onError(Object o) {
//error 400,403,500,etc
    }

    @Override
    public void unAuthorized() {
//error 401 auto logout unauthorized
    }
}
