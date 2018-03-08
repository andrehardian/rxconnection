package connection.rxconnection.model;

import lombok.Data;

/**
 * Created by AndreHF on 3/1/2018.
 */

@Data
public class ModelLog {
    private String error;
    private String header;
    private String body;
    private String url;
    private long exp;
    private String name;
}
