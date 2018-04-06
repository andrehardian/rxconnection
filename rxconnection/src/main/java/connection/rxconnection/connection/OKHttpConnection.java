package connection.rxconnection.connection;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import connection.rxconnection.model.BaseResponse;
import connection.rxconnection.model.ModelLog;
import lombok.Getter;
import lombok.Setter;
import okhttp3.FormBody;
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
    @Setter
    private CallBackForLog callBackForLog;
    @Getter
    private OkHttpClient okHttpClient = new OkHttpClient();
    @Getter
    private ModelLog modelLog;

    public OKHttpConnection(HandleErrorConnection handleErrorConnection) {
        this.handleErrorConnection = handleErrorConnection;
    }


    public BaseResponse data(T t, String url, Class<E> eClass, int httpMethod, MediaType mediaType,
                             Context context) {
        okHttpClient = getUnsafeOkHttpClient();
        OkHttpClient.Builder builder = okHttpClient.newBuilder();
        builder.connectTimeout(1, TimeUnit.MINUTES);
        builder.readTimeout(1, TimeUnit.MINUTES);
        builder.writeTimeout(1, TimeUnit.MINUTES);
        return execute(t, url, eClass, httpMethod, mediaType, context);
    }


    private BaseResponse execute(T t, String url, Class<E> eClass,
                                 int httpMethod, MediaType mediaType, Context context) {
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
            String code = String.valueOf(response.code());
            printLog(t,request, log);
            try {
                if (code.startsWith("2")) {
                    E json = null;
                    try {
                        json = new GsonBuilder().setLenient().create().fromJson(log, eClass);
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    }
                    baseResponse = new BaseResponse();
                    baseResponse.setCode(response.code());
                    if (json != null) {
                        baseResponse.setData(json);
                    }else {
                        catchSuccessNull(response, log, null);
                    }
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

    private void printLog(T t, Request request, String response) {
        try {
            modelLog = new ModelLog();
            modelLog.setBody(new Gson().toJson(t));
            modelLog.setUrl(request.url().toString());
            modelLog.setHeader(request.headers().toString());
            modelLog.setError(response);
            if (callBackForLog!=null){
                callBackForLog.log(modelLog);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (logInfoRequestResponse) {
            try {
                final String s = "Info\n" + "url : " + request.url() + "\nbody request : " + request.body().toString()
                        + "\nrequest header : " + request.headers() +
                        "\nresponse body : " + response;
                Log.i("rxconnection_log", s);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private BaseResponse catchSuccessNull(Response response, String error, Throwable throwable) {
        if (response != null && String.valueOf(response.code()).startsWith("2"))
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
        }else if (mediaType.toString().contains("form")){
            return bodyForm(t);
        }
        else {
            return RequestBody.create(mediaType, new Gson().toJson(t));
        }
    }

    private RequestBody bodyForm(T t) {
        Map<String,Object> objectMap = pojo2Map(t);
        FormBody.Builder formBody = new FormBody.Builder();
        for (String key :objectMap.keySet()) {
        formBody.add(key, String.valueOf(objectMap.get(key)));
        }
        return formBody.build();
    }

    public final static Map<String, Object> pojo2Map(Object obj) {
        Map<String, Object> hashMap = new HashMap<String, Object>();
        try {
            Class<? extends Object> c = obj.getClass();
            Method m[] = c.getMethods();
            for (int i = 0; i < m.length; i++) {
                if (m[i].getName().indexOf("get") == 0) {
                    String name = m[i].getName().toLowerCase().substring(3, 4) + m[i].getName().substring(4);
                    hashMap.put(name, m[i].invoke(obj, new Object[0])!=null?m[i].invoke(obj,
                            new Object[0]):new Object());
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return hashMap;
    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}