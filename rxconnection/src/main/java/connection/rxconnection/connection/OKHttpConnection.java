package connection.rxconnection.connection;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.security.cert.CertificateException;
import java.util.Collections;
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
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.TlsVersion;

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

    public void download(String url, File fileDownload, ProgressDownloadListener
            progressDownloadListener,Context context) {
        okHttpClient = getUnsafeOkHttpClient();
        Request request = new Request.Builder().headers(this.headers(context)).url(url).build();
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response != null) {
            String code = String.valueOf(response.code());
            if (code.startsWith("2")) {
                InputStream inputStream = response.body().byteStream();

                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                try {
                    OutputStream outputStream = new FileOutputStream(fileDownload);
                    long total = 0;
                    byte[] dataFile = new byte[1024];
                    int count = 0;
                    while ((count = bufferedInputStream.read(dataFile)) != -1) {
                        total += count;
                        outputStream.write(dataFile, 0, count);
                        progressDownloadListener.progress(total/count*100);
                    }

                    if (outputStream != null) {
                        outputStream.flush();
                        outputStream.close();
                    }

                    if (bufferedInputStream != null) {
                        bufferedInputStream.close();
                    }

                    callBackOKHttp.doneDownload();
                } catch (FileNotFoundException e) {
                    callBackOKHttp.error(new ExceptionHttpRequest(e.getMessage(), response, e));
                } catch (IOException e) {
                    callBackOKHttp.error(new ExceptionHttpRequest(e.getMessage(), response, e));
                }
            }else {
                try {
                    progressDownloadListener.error(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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
                request = new Request.Builder().headers(headers(context)).url(url).get().build();
                break;
            case HttpMethod.PUT:
                requestBody = createBody(mediaType, t);
                request = new Request.Builder().headers(headers(context)).put(requestBody).url(url).build();
                break;
            case HttpMethod.DELETE:
                requestBody = createBody(mediaType, t);
                request = new Request.Builder().headers(headers(context)).delete(requestBody).url(url).build();
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
/*
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
*/

            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                    .supportsTlsExtensions(true)
                    .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
                    .cipherSuites(
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
                            CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA)
                    .build();

/*
            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
*/

            SSLContext sslContext;
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            sslContext.createSSLEngine();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectionSpecs(Collections.singletonList(spec));
//            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.sslSocketFactory(sslContext.getSocketFactory());
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
