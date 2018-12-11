package connection.rxconnection.connection;

import okhttp3.ResponseBody;

public interface ProgressDownloadListener {
    void progress(long progress);

    void error(String body);
}
