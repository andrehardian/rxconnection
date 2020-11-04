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
    private final Context context;
    private final String url;
    @Getter
    private REQUEST request;
    @Getter
    private OKHttpConnection<REQUEST, RESPONSE> teokHttpConnection;
    private MediaType mediaType;
    private Class<RESPONSE> eClass;
    private int httpMethod;
    private String userType;
    private Subscriber<? super BaseResponse<RESPONSE>> subscriber;
    private Map<String, String> customHeader;
    private boolean formData;
    private boolean boundary;
    private boolean logInfoRequestResponse;
    private boolean downloadFile;
    private File fileDownload;
    private int connectionTimeout = 1;
    private int readTimeout = 1;
    private int writeTimeout = 1;
    private String mediaTypeInfo = "charset=utf-8";
    private ProgressDownloadListener progressDownloadListener;
    private CallBackForLog callBackForLog;
    @Getter
    private String message;

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
                + "; " + mediaTypeInfo);
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
                + "; " + mediaTypeInfo);
    }

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

    public HttpRequest<REQUEST, RESPONSE> setCallBackForLog(CallBackForLog callBackForLog) {
        this.callBackForLog = callBackForLog;
        return this;
    }

    public HttpRequest<REQUEST, RESPONSE> setMessage(String message) {
        this.message = message;
        return this;
    }

    public HttpRequest<REQUEST, RESPONSE> setLogInfoRequestResponse(boolean logInfoRequestResponse) {
        this.logInfoRequestResponse = logInfoRequestResponse;
        return this;
    }

    public HttpRequest<REQUEST, RESPONSE> setMediaType(String mediaType) {
        this.mediaType = MediaType.parse(mediaType + "; " + mediaTypeInfo);
        return this;
    }

    public HttpRequest<REQUEST, RESPONSE> setMediaType(String mediaType, String mediaTypeInfo) {
        this.mediaTypeInfo = mediaTypeInfo;
        this.mediaType = MediaType.parse(mediaType + "; " + mediaTypeInfo);
        return this;
    }

    public HttpRequest<REQUEST, RESPONSE> setMediaTypeInfo(String mediaTypeInfo) {
        this.mediaTypeInfo = mediaTypeInfo;
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
    public HttpRequest<REQUEST, RESPONSE> setBoundary(boolean boundary) {
        this.boundary = boundary;
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
            teokHttpConnection.setBoundary(boundary);
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
