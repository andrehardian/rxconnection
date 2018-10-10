package connection.rxconnection.model;

import java.util.ArrayList;

import lombok.Data;

@Data
public class BaseModelRequestFormData {
    public BaseModelRequestFormData setModelFormData(ArrayList<ModelFormData> modelFormData) {
        this.modelFormData = modelFormData;
        return this;
    }

    private ArrayList<ModelFormData> modelFormData;
}
