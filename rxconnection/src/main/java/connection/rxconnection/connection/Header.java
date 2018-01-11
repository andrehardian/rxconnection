package connection.rxconnection.connection;

import android.content.Context;

import java.util.Map;

import connection.rxconnection.session.SessionLogin;
import lombok.Setter;
import okhttp3.Headers;

/**
 * Created by AndreHF on 4/12/2017.
 */

public class Header {
    @Setter
    private Map<String, String> customHeader;

    protected Headers headers(Context context) {
        SessionLogin sessionLogin = new SessionLogin(context);
        Headers.Builder builder = new Headers.Builder();
        if (customHeader != null && customHeader.size() > 0) {
            for (String key : customHeader.keySet()) {
                builder.add(key,customHeader.get(key));
            }
        }else {
            if (sessionLogin.getToken() != null) {
                builder.add("Authorization", sessionLogin.getToken());
            }
        }
        return builder.build();
    }

}
