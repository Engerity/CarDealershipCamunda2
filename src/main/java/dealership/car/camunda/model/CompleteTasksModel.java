package dealership.car.camunda.model;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class CompleteTasksModel {

    @NotEmpty(message = "Id is required.")
    private String id;

    private String name;

    private List<CustomFormField> formValues;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CustomFormField> getFormValues() {
        if (formValues == null)
            formValues = new ArrayList<>();

        return formValues;
    }
}
