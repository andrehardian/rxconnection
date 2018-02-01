package connection.rxconnection.connection;

/**
 * Created by AndreHF on 1/27/2017.
 */

public interface ConnectionListener {
    void onSuccessWithData(Object o);
    void onSuccessNull();
    void onMessageSuccess(String s);
    void onError(Object o, HttpRequest httpRequest);
    void unAuthorized(HttpRequest httpRequest);
}
