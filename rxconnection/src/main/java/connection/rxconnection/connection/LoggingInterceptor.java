package connection.rxconnection.connection;

import android.util.Log;

import java.io.IOException;

import connection.rxconnection.BuildConfig;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by AndreHF on 1/18/2018.
 */

public class LoggingInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        ;

        long t1 = System.nanoTime();
        if (BuildConfig.DEBUG) {
            Log.d("rxCon request", String.format("Sending request %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));
            long t2 = System.nanoTime();
            Log.d("rxCon response", String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers()));

        }
        return response;
    }
}
