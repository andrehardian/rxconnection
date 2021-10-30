package com.app.exera.connection;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.exera.connection.connect.ServiceManager;
import com.app.exera.connection.model.RequestLogin;

import connection.rxconnection.connection.ConnectionListener;
import connection.rxconnection.connection.HttpRequest;

public class MainActivity extends AppCompatActivity implements ConnectionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        download("https://s3.ap-southeast-1.amazonaws.com/dev.pawoon.com/backup/3003-1544528282.db");
        login();
    }

    private void download(String s) {
        ((ServiceManager) new ServiceManager().setContext(this).setConnectionListener(this))
                .download(s);
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
    public void onError(Object o, HttpRequest httpRequest) {
        Toast.makeText(this, (String) o, Toast.LENGTH_LONG);
//error 400,403,500,etc
    }

    @Override
    public void unAuthorized(HttpRequest httpRequest, String messageError) {
//error 401 auto logout unauthorized
    }
}
