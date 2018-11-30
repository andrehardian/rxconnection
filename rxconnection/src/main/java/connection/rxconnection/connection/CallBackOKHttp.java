package connection.rxconnection.connection;

/**
 * Created by AndreHF on 1/27/2017.
 */

public interface CallBackOKHttp {
    void error(ExceptionHttpRequest exceptionHttpRequest);
    <T> void success(T t);
    void doneDownload();
}
