package connection.rxconnection.connection;

import android.app.Activity;
import android.app.ProgressDialog;

import lombok.Getter;
import lombok.Setter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by AndreHF on 4/12/2017.
 */

public class ConnectionManager implements CallBackSubscriber {
    @Getter
    private Activity activity;

    public ConnectionManager setActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    public ConnectionManager setConnectionListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
        return this;
    }

    private ConnectionListener connectionListener;

    private ProgressDialog progressDialog;

    protected void subscribe(HttpRequest httpRequest) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(activity);
            progressDialog.setCancelable(false);
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
        Observable.create(httpRequest)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.newThread())
                .subscribe(new BaseServiceResponse(connectionListener).setCallBackSubscriber(this));
    }
    protected void subscribe(HttpRequest httpRequest,String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(activity);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(message);
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
        Observable.create(httpRequest)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.newThread())
                .subscribe(new BaseServiceResponse(connectionListener).setCallBackSubscriber(this));
    }

    @Override
    public void onServiceFinish() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
