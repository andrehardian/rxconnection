package connection.rxconnection.connection;

import android.content.Context;

import com.google.gson.Gson;

import java.io.File;
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

import connection.rxconnection.model.BaseModelRequestFormData;
import connection.rxconnection.model.ModelFormData;
import connection.rxconnection.model.ModelLog;
import lombok.Getter;
import lombok.Setter;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by AndreHF on 1/27/2017.
 */

public class OKHttpConnection<T, E> extends Header {
    private final CallBackOKHttp callBackOKHttp;
    @Setter
    private boolean formData;
    @Setter
    private boolean logInfoRequestResponse;
    @Setter
    private CallBackForLog callBackForLog;
    @Getter
    private OkHttpClient okHttpClient = new OkHttpClient();
    @Getter
    private ModelLog modelLog;

    public OKHttpConnection(CallBackOKHttp handleErrorConnection) {
        this.callBackOKHttp = handleErrorConnection;
    }


    public void data(T t, String url, Class<E> eClass, int httpMethod, MediaType mediaType,
                     Context context) {
        okHttpClient = getUnsafeOkHttpClient();
        execute(t, url, eClass, httpMethod, mediaType, context);
    }


    private void execute(T t, String url, Class<E> eClass,
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
        okHttpClient.newCall(request).enqueue(new UtilsQueueOKHttp(modelLog,
                logInfoRequestResponse, eClass, callBackOKHttp, callBackForLog, t));

    }

    private RequestBody createBody(MediaType mediaType, T t) {
        if (formData) {
            MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
            if (t instanceof BaseModelRequestFormData) {
                BaseModelRequestFormData baseModelRequestFormData = (BaseModelRequestFormData) t;
                multipartBodyBuilder.setType(MultipartBody.FORM);
                if (baseModelRequestFormData.getModelFormData() != null) {
                    for (ModelFormData modelFormData : baseModelRequestFormData.getModelFormData()) {
                        if (modelFormData.getValue() instanceof File) {
                            multipartBodyBuilder.addFormDataPart(modelFormData.getKey(), ((File) modelFormData.getValue())
                                            .getName(),
                                    RequestBody.create(mediaType, (File) modelFormData.getValue()));
                        } else {
                            multipartBodyBuilder.addFormDataPart(modelFormData.getKey(),
                                    (String) modelFormData.getValue());
                        }
                    }
                }
            }
            return multipartBodyBuilder.build();
        } else if (mediaType.toString().contains("form")) {
            return bodyForm(t);
        } else {
            return RequestBody.create(mediaType, new Gson().toJson(t));
        }
    }


    private RequestBody bodyForm(T t) {
        Map<String, Object> objectMap = pojo2Map(t);
        FormBody.Builder formBody = new FormBody.Builder();
        for (String key : objectMap.keySet()) {
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
                    hashMap.put(name, m[i].invoke(obj, new Object[0]) != null ? m[i].invoke(obj,
                            new Object[0]) : new Object());
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
            builder.connectTimeout(1, TimeUnit.MINUTES);
            builder.readTimeout(1, TimeUnit.MINUTES);
            builder.writeTimeout(1, TimeUnit.MINUTES);

            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
