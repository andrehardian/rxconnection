package connection.rxconnection.connection;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.Reader;

import connection.rxconnection.model.BaseResponse;
import connection.rxconnection.model.ModelLog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class UtilsQueueOKHttp<T, E> implements Callback {
    private ModelLog modelLog;
    private boolean logInfoRequestResponse;
    private final Class<E> eClass;
    private final CallBackOKHttp callBackOKHttp;
    private final CallBackForLog callBackForLog;
    private final T requestData;

    public UtilsQueueOKHttp(ModelLog modelLog, boolean logInfoRequestResponse, Class<E> eClass,
                            CallBackOKHttp callBackOKHttp, CallBackForLog callBackForLog, T request) {
        this.modelLog = modelLog;
        this.logInfoRequestResponse = logInfoRequestResponse;
        this.eClass = eClass;
        this.callBackOKHttp = callBackOKHttp;
        this.callBackForLog = callBackForLog;
        this.requestData = request;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        printLog(call.request(), e.getMessage(), "0");
        catchSuccessNull(null, e.getMessage(), null);
    }

    @Override
    public void onResponse(Call call, Response response) {
        BaseResponse<E> baseResponse = null;
        String log = getBodyString(response);
        String code = String.valueOf(response.code());
        printLog(call.request(), log, code);
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
                    callBackOKHttp.success(baseResponse);
                } else {
                    catchSuccessNull(response, log, null);
                }
            } else {
                catchSuccessNull(response, log, null);
            }
        } catch (ExceptionHttpRequest e) {
            e.printStackTrace();
            catchSuccessNull(response, log, e);
        }

    }

    private void catchSuccessNull(Response response, String error, Throwable throwable) {
        if (response != null && String.valueOf(response.code()).startsWith("2"))
            callBackOKHttp.success(new BaseResponse<E>().setCode(response.code()).setError(error));
        else {
            ExceptionHttpRequest exceptionHttpRequest = new ExceptionHttpRequest(error, response, throwable);
            callBackOKHttp.error(exceptionHttpRequest);
        }
    }

    private void printLog(Request request, String response, String httpCode) {
        try {
            modelLog = new ModelLog();
            modelLog.setBody(new Gson().toJson(requestData));
            modelLog.setUrl(request.url().toString());
            modelLog.setHeader(request.headers().toString());
            modelLog.setError(response);
            if (httpCode != null && httpCode.length() > 0)
                modelLog.setHttpCode(Integer.parseInt(httpCode));
            if (callBackForLog != null) {
                callBackForLog.log(modelLog);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (logInfoRequestResponse) {
            try {
                final String s = "Info\n" + "url : " + request.url() + "\nbody requestData : " + request.body().toString()
                        + "\nrequestData header : " + request.headers() +
                        "\nresponse body : " + response;
                Log.i("rxconnection_log", s);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public String getBodyString(Response response) {
        String error = "";

        try {
            error = response.body().string();
        } catch (IOException|OutOfMemoryError e) {
            e.printStackTrace();
            int value = 0;
            Reader reader = null;
            try {
                reader = response.body().charStream();
                while ((value = reader.read()) != -1) {
                    error += (char) value;
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException|IllegalStateException e1) {
                        e1.printStackTrace();
                    }
                }
            }

        }
        return error;
    }


}
