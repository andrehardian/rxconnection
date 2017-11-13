package connection.rxconnection.connection;

import java.io.IOException;

import lombok.Getter;
import okhttp3.Response;

/**
 * Created by AndreHF on 4/12/2017.
 */

public class ExceptionHttpRequest extends RuntimeException {
    public enum Kind {
        NETWORK, HTTP, UNEXPECTED
    }

    @Getter
    private String url;
    @Getter
    private final Response response;
    @Getter
    private Kind kind;

    public ExceptionHttpRequest(
            String message, String url, Response response,
            Kind kind, Throwable exception) {

        super(message, exception);
        this.url = url;
        this.response = response;
        this.kind = kind;
    }

    public ExceptionHttpRequest(
            String message, Response response,
            Throwable exception) {

        super(message, exception);
        this.response = response;
    }

    public static ExceptionHttpRequest httpError(String url, Response response) {
        String message = response.code() + " " + response.message();
        return new ExceptionHttpRequest(message, url, response, Kind.HTTP, null);
    }

    public static ExceptionHttpRequest networkError(IOException exception) {
        return new ExceptionHttpRequest(exception.getMessage(), null, null, Kind.NETWORK, exception);
    }

    public static ExceptionHttpRequest unexpectedError(Throwable exception) {
        return new ExceptionHttpRequest(exception.getMessage(), null, null, Kind.UNEXPECTED,
                exception);
    }

}
