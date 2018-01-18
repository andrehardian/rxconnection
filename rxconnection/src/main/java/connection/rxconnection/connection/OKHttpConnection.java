package connection.rxconnection.connection;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import connection.rxconnection.model.BaseResponse;
import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by AndreHF on 1/27/2017.
 */

public class OKHttpConnection<T, E> extends Header {
    private final HandleErrorConnection handleErrorConnection;
    @Setter
    private String multipartFileName;
    @Setter
    private boolean logInfoRequestResponse;

    public OKHttpConnection(HandleErrorConnection handleErrorConnection) {
        this.handleErrorConnection = handleErrorConnection;
    }


    public BaseResponse data(T t, String url, Class<E> eClass, int httpMethod, MediaType mediaType,
                             Context context) {
        OkHttpClient okHttpClient = new OkHttpClient();
        OkHttpClient.Builder builder = okHttpClient.newBuilder();
        builder.connectTimeout(1, TimeUnit.MINUTES);
        builder.readTimeout(1, TimeUnit.MINUTES);
        builder.writeTimeout(1, TimeUnit.MINUTES);
        Request request = null;
        switch (httpMethod) {
            case HttpMethod.POST:
                RequestBody requestBody = createBody(mediaType, t);
                request = new Request.Builder().headers(headers(context)).post(requestBody).url(url).build();
                break;
            case HttpMethod.GET:
                request = new Request.Builder().headers(headers(context)).url(url).build();
                break;
            case HttpMethod.PUT:
                requestBody = createBody(mediaType, t);
                request = new Request.Builder().headers(headers(context)).put(requestBody).url(url).build();
                break;
            case HttpMethod.DELETE:
                request = new Request.Builder().headers(headers(context)).delete().url(url).build();
                break;
        }
        Response response = null;
        try {
            BaseResponse<E> baseResponse = null;
            response = okHttpClient.newCall(request).execute();
            String log = response.body().string();
            try {
                if (response.code() == 200) {
                    E json = null;
                    try {
                        json = new GsonBuilder().setLenient().create().fromJson(log, eClass);
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                        json = (E) log;
                    }
                    if (json != null) {
                        baseResponse = new BaseResponse();
                        baseResponse.setCode(response.code());
                        baseResponse.setData(json);
                    }
                    printLog(request, log);
                    return baseResponse;
                } else {
                    return catchSuccessNull(response, log, null);
                }
            } catch (ExceptionHttpRequest e) {
                e.printStackTrace();
                return catchSuccessNull(response, log, e);
            }

        } catch (IOException e) {
            return catchSuccessNull(response, e.getMessage(), e);
        }
    }

    private void printLog(Request request, String response) {
        if (logInfoRequestResponse) {
            final String s = "Info\n" + "url : " + request.url() + "\nbody request : " + request.body().toString()
                    + "\nrequest header : " + request.headers() +
                    "\nresponse body : " + response;
            try {
                Log.i("rxconnection_log",s);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private BaseResponse catchSuccessNull(Response response, String error, Throwable throwable) {
        if (response.code() == 203)
            return new BaseResponse<E>().setCode(response.code()).setError(error);
        else {
            ExceptionHttpRequest exceptionHttpRequest = new ExceptionHttpRequest(error, response, throwable);
            handleErrorConnection.error(exceptionHttpRequest);
        }
        return null;
    }

    private RequestBody createBody(MediaType mediaType, T t) {
        if (t instanceof File) {
            File file = (File) t;
            return new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart(multipartFileName, file.getName(),
                    RequestBody.create(mediaType, file)).build();
        } else {
            return RequestBody.create(mediaType, new Gson().toJson(t));
        }
    }


}

