package connection.rxconnection.connection;

import android.app.ProgressDialog;
import android.content.Context;

import lombok.Getter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by AndreHF on 4/12/2017.
 */

public class ConnectionManager implements CallBackSubscriber {
    @Getter
    private Context context;
    @Getter
    private boolean show = true;
    protected int requestSize = 0;

    public ConnectionManager setContext(Context context) {
        this.context = context;
        return this;
    }

    public ConnectionManager showDialog(boolean b) {
        show = b;
        return this;
    }

    public ConnectionManager setConnectionListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
        return this;
    }

    @Getter
    private ConnectionListener connectionListener;

    private ProgressDialog progressDialog;

    protected void subscribe(HttpRequest httpRequest) {
        try {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(context);
                progressDialog.setCancelable(false);
            }
            if (!progressDialog.isShowing() && show) {
                progressDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        requestSize += 1;
        Observable.create(httpRequest)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.newThread())
                .subscribe(new BaseServiceResponse(connectionListener).setCallBackSubscriber(this));
    }

    protected void subscribe(HttpRequest httpRequest, String message) {

        try {
            if (progressDialog == null && context != null) {
                progressDialog = new ProgressDialog(context);
                progressDialog.setCancelable(false);
                progressDialog.setMessage(message);
            }
            if (!progressDialog.isShowing() && show) {
                progressDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        requestSize += 1;
        Observable.create(httpRequest.setMessage(message))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.newThread())
                .subscribe(new BaseServiceResponse(connectionListener).setCallBackSubscriber(this));
    }

    @Override
    public void onServiceFinish() {
        if (progressDialog != null && progressDialog.isShowing() && requestSize == 1) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (requestSize > 0)
            requestSize -= 1;
    }
}
