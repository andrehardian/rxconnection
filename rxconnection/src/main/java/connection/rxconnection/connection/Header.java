package connection.rxconnection.connection;

import android.content.Context;

import connection.rxconnection.session.SessionLogin;
import okhttp3.Headers;

/**
 * Created by AndreHF on 4/12/2017.
 */

public class Header {
    protected Headers headers(Context context) {
        SessionLogin sessionLogin = new SessionLogin(context);
        Headers.Builder builder = new Headers.Builder();
        if (sessionLogin.getToken() != null) {
            builder.add("token", sessionLogin.getToken());
        }
        return builder.build();
    }

}
