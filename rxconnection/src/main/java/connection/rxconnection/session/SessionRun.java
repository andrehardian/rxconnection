package connection.rxconnection.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionRun {
    private final String sessionName = "run";
    private final String key = "run";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SessionRun(Context context) {
        sharedPreferences = context.getSharedPreferences(sessionName, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public boolean isRun() {
        return sharedPreferences.getBoolean(key, true);
    }

    public void setRun(boolean run) {
        editor.putBoolean(key, run);
        editor.commit();
    }

    public void clearRun() {
        editor.clear();
        editor.commit();
    }
}
