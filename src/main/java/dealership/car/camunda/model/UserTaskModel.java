package dealership.car.camunda.model;

import javax.validation.constraints.NotEmpty;

public class UserTaskModel {

    @NotEmpty(message = "Text is required.")
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
