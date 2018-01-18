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
    private final boolean show;

    public LoggingInterceptor(boolean show) {
        this.show = show;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        ;

        long t1 = System.nanoTime();
        if (show) {
            System.out.println(String.format("Sending request %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));
            long t2 = System.nanoTime();
            System.out.println(String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers()));

        }
        return response;
    }
}
