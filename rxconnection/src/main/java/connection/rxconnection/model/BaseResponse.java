package connection.rxconnection.model;

import connection.rxconnection.connection.HttpRequest;
import lombok.Data;

/**
 * Created by AndreHF on 1/27/2017.
 */
@Data
public class BaseResponse<E> {
    private String error;
    private int code;
    private E data;

    public BaseResponse<E> setCode(int code) {
        this.code = code;
        return this;
    }
    public BaseResponse<E> setError(String error) {
        this.error = error;
        return this;
    }
}
