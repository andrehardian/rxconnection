package com.app.exera.connection;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

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
                .login(new RequestLogin().setUsername("kanibal@me.com").setPassword("kanibal"));
    }

    @Override
    public void onSuccessWithData(Object o) {
    }

    @Override
    public void onSuccessNull() {
        Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMessageSuccess(String s) {
    }

    @Override
    public void onError(Object o) {
        Toast.makeText(this,(String)o,Toast.LENGTH_LONG);
//error 400,403,500,etc
    }

    @Override
    public void unAuthorized() {
//error 401 auto logout unauthorized
    }
}
