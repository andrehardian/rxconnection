package connection.rxconnection.connection;

import android.content.Context;

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

public class HttpRequest<REQUEST, RESPONSE> implements HandleErrorConnection, Observable.OnSubscribe<BaseResponse<RESPONSE>> {
    private REQUEST request;
    private OKHttpConnection<REQUEST, RESPONSE> teokHttpConnection;
    @Getter
    private final Context context;
    private MediaType mediaType;
    private final Class<RESPONSE> eClass;
    private final String url;
    private final int httpMethod;
    private String userType;
    private Subscriber<? super BaseResponse<RESPONSE>> subscriber;
    private Map<String, String> customHeader;
    private String multipartFileName;
    private boolean logInfoRequestResponse;

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

    public HttpRequest<REQUEST, RESPONSE> setMultipartFileName(String multipartFileName) {
        this.multipartFileName = multipartFileName;
        return this;
    }


    @Override
    public void call(Subscriber<? super BaseResponse<RESPONSE>> subscriber) {
        this.subscriber = subscriber;
        BaseResponse<RESPONSE> response = null;
        teokHttpConnection.setCustomHeader(customHeader);
        teokHttpConnection.setMultipartFileName(multipartFileName);
        teokHttpConnection.setLogInfoRequestResponse(logInfoRequestResponse);
        try {
            response =
                    teokHttpConnection.data(request, url, eClass, httpMethod, mediaType, context);
        } catch (Exception e) {
            e.printStackTrace();
            response = new BaseResponse<>();
            response.setError(e.getMessage());
        }
        response.setHttpRequest(this);
        subscriber.onNext(response);
    }

    @Override
    public void error(ExceptionHttpRequest exceptionHttpRequest) {
        exceptionHttpRequest.setHttpRequest(this);
        subscriber.onError(exceptionHttpRequest);
    }

    public OkHttpClient getOkhttpClient() {
        return teokHttpConnection.getOkHttpClient();
    }

}
