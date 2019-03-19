package connection.rxconnection.connection;

import android.content.Context;

import java.io.File;
import java.util.Map;

import connection.rxconnection.model.BaseResponse;
import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by AndreHF on 4/12/2017.
 */

public class HttpRequest<REQUEST, RESPONSE> implements CallBackOKHttp, Observable.OnSubscribe<BaseResponse<RESPONSE>> {
    @Getter
    private REQUEST request;
    @Getter
    private OKHttpConnection<REQUEST, RESPONSE> teokHttpConnection;
    @Getter
    private final Context context;
    private MediaType mediaType;
    private Class<RESPONSE> eClass;
    private final String url;
    private int httpMethod;
    private String userType;
    private Subscriber<? super BaseResponse<RESPONSE>> subscriber;
    private Map<String, String> customHeader;
    private boolean formData;
    private boolean logInfoRequestResponse;
    private boolean downloadFile;
    private File fileDownload;
    private int connectionTimeout = 1;
    private int readTimeout = 1;
    private int writeTimeout = 1;


    public HttpRequest<REQUEST, RESPONSE> setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public HttpRequest<REQUEST, RESPONSE> setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }


    public HttpRequest<REQUEST, RESPONSE> setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }
    private ProgressDownloadListener progressDownloadListener;

    public HttpRequest<REQUEST, RESPONSE> setCallBackForLog(CallBackForLog callBackForLog) {
        this.callBackForLog = callBackForLog;
        return this;
    }

    private CallBackForLog callBackForLog;

    public HttpRequest<REQUEST, RESPONSE> setMessage(String message) {
        this.message = message;
        return this;
    }

    @Getter
    private String message;

    public HttpRequest<REQUEST, RESPONSE> setLogInfoRequestResponse(boolean logInfoRequestResponse) {
        this.logInfoRequestResponse = logInfoRequestResponse;
        return this;
    }


    public HttpRequest(REQUEST request, Context context, Class<RESPONSE> resultClass, String url,
                       int httpMethod) {
//        super(f);
        this.request = request;
        this.context = context;
        this.eClass = resultClass;
        this.url = url;
        this.httpMethod = httpMethod;
        teokHttpConnection = new OKHttpConnection(this);
        this.mediaType = MediaType.parse(org.androidannotations.api.rest.MediaType.APPLICATION_JSON
                + "; charset=utf-8");
    }

    public HttpRequest(Context context, String url, File fileDownload, ProgressDownloadListener progressDownloadListener) {
//        super(f);
        this.progressDownloadListener = progressDownloadListener;
        this.fileDownload = fileDownload;
        downloadFile = true;
        this.context = context;
        this.url = url;
        teokHttpConnection = new OKHttpConnection(this);
    }

    public HttpRequest(Context context, Class<RESPONSE> resultClass, String url, int httpMethod) {
//        super(f);
        this.context = context;
        this.eClass = resultClass;
        this.url = url;
        this.httpMethod = httpMethod;
        teokHttpConnection = new OKHttpConnection(this);
        this.mediaType = MediaType.parse(org.androidannotations.api.rest.MediaType.APPLICATION_JSON
                + "; charset=utf-8");
    }

    public HttpRequest<REQUEST, RESPONSE> setMediaType(String mediaType) {
        this.mediaType = MediaType.parse(mediaType + "; charset=utf-8");
        return this;
    }

    public HttpRequest<REQUEST, RESPONSE> setUserType(String userType) {
        this.userType = userType;
        return this;
    }

    public HttpRequest<REQUEST, RESPONSE> setCustomHeader(Map<String, String> customHeader) {
        this.customHeader = customHeader;
        return this;
    }

    public HttpRequest<REQUEST, RESPONSE> setFormData(boolean formData) {
        this.formData = formData;
        return this;
    }


    @Override
    public void call(Subscriber<? super BaseResponse<RESPONSE>> subscriber) {
        this.subscriber = subscriber;
        teokHttpConnection.setCustomHeader(customHeader);
        if (downloadFile) {
            teokHttpConnection.download(url, fileDownload, progressDownloadListener, context);
        } else {
            teokHttpConnection.setConnectionTimeOut(connectionTimeout);
            teokHttpConnection.setReadTimeOut(readTimeout);
            teokHttpConnection.setWriteTimeOut(writeTimeout);
            teokHttpConnection.setFormData(formData);
            teokHttpConnection.setLogInfoRequestResponse(logInfoRequestResponse);
            teokHttpConnection.setCallBackForLog(callBackForLog);
            teokHttpConnection.data(request, url, eClass, httpMethod, formData ?
                    MediaType.parse(org.androidannotations.api.rest.MediaType.MULTIPART_FORM_DATA) :
                    mediaType, context);
        }
    }

    @Override
    public void error(ExceptionHttpRequest exceptionHttpRequest) {
        exceptionHttpRequest.setHttpRequest(this);
        subscriber.onError(exceptionHttpRequest);
    }

    @Override
    public <T> void success(T t) {
        BaseResponse<RESPONSE> response = new BaseResponse<>();
        try {
            response = (BaseResponse<RESPONSE>) t;
        } catch (Exception e) {
            e.printStackTrace();
            response = new BaseResponse<>();
            response.setError(e.getMessage());
        }

        subscriber.onNext(response);
    }

    @Override
    public void doneDownload() {
        subscriber.onCompleted();
    }

    public OkHttpClient getOkhttpClient() {
        return teokHttpConnection.getOkHttpClient();
    }

}
