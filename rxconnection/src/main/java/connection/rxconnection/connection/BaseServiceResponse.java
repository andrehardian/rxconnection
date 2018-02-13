package connection.rxconnection.connection;

import connection.rxconnection.model.BaseResponse;
import okhttp3.Response;
import rx.Subscriber;

/**
 * Created by AndreHF on 4/12/2017.
 */

public class BaseServiceResponse<RESPONSE> extends Subscriber<BaseResponse<RESPONSE>> {
    private final ConnectionListener connectionListener;

    public BaseServiceResponse<RESPONSE> setCallBackSubscriber(CallBackSubscriber callBackSubscriber) {
        this.callBackSubscriber = callBackSubscriber;
        return this;
    }

    private CallBackSubscriber callBackSubscriber;

    public BaseServiceResponse(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }


    @Override
    public void onCompleted() {
        unsubscribe();
    }

    @Override
    public void onError(Throwable e) {
        callBackSubscriber.onServiceFinish();
        if (e instanceof ExceptionHttpRequest) {
            ExceptionHttpRequest exceptionHttpRequest = (ExceptionHttpRequest) e;
            Response response = exceptionHttpRequest.getResponse();
            if (response != null && response.code() == 401) {
                connectionListener.unAuthorized(exceptionHttpRequest.getHttpRequest());
            } else {
                connectionListener.onError(exceptionHttpRequest.getMessage(),
                        exceptionHttpRequest.getHttpRequest());
            }
        } else {
            connectionListener.onError(e.getMessage(), null);
        }
    }

    @Override
    public void onNext(BaseResponse<RESPONSE> responseBaseResponse) {
        callBackSubscriber.onServiceFinish();
        if (responseBaseResponse != null) {
            if (String.valueOf(responseBaseResponse.getCode()).startsWith("2")) {
                if (responseBaseResponse.getData() != null)
                    connectionListener.onSuccessWithData(responseBaseResponse.getData());
                else if (responseBaseResponse.getError() != null)
                    connectionListener.onMessageSuccess(responseBaseResponse.getError());
                else
                    connectionListener.onSuccessNull();
            }
        }
    }

}
