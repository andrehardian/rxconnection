package connection.rxconnection.connection;

public interface ProgressDownloadListener {
    void progress(long progress);

    void error(String body);
}
