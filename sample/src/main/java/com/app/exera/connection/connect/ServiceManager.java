package com.app.exera.connection.connect;

import android.os.Environment;
import android.util.Log;

import com.app.exera.connection.BuildConfig;
import com.app.exera.connection.model.RequestLogin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import connection.rxconnection.connection.ConnectionManager;
import connection.rxconnection.connection.HttpRequest;
import connection.rxconnection.connection.ProgressDownloadListener;

/**
 * Created by AndreHF on 11/14/2017.
 */

public class ServiceManager extends ConnectionManager implements ProgressDownloadListener {
    public void login(RequestLogin loginRequest) {
        subscribe(new LoginCon(loginRequest, getContext()).setLogInfoRequestResponse(true));
    }

    public void download(String s) {
        HttpRequest httpRequest = new ConDownloadDB(getContext(), s,
                new File(Environment.getExternalStorageDirectory().getPath()),
                this);
        Map<String, String> header = new HashMap<String, String>();
        header.put("version", BuildConfig.VERSION_NAME);
        httpRequest.setCustomHeader(header);
        subscribe(httpRequest);
    }

    @Override
    public void progress(long progress) {
        Log.d("test", progress + "");
    }

    @Override
    public void error(String body) {
        Log.d("test", body);
    }
}
