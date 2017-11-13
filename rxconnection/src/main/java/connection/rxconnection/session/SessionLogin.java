package connection.rxconnection.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionLogin {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private final String sessionName = "login";
    private final String key = "token";

    public SessionLogin(Context context) {
        sharedPreferences = context.getSharedPreferences(sessionName, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setToken(String token) {
        editor.putString(key, token);
        editor.commit();
    }

    public String getToken() {
        return sharedPreferences.getString(key, null);
    }

    public void clearToken() {
        editor.clear();
        editor.commit();
    }
}
