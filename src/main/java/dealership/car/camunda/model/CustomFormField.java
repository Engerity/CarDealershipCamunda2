package dealership.car.camunda.model;

import java.util.Map;

public class CustomFormField {

    private String id;

    private String typeName;

    private Object value;

    private String label;

    private Map<String, String> properties;

    public CustomFormField() {
    }

    public CustomFormField(String id, String typeName, Object value, String label, Map<String, String> properties) {
        this.id = id;
        this.typeName = typeName;
        this.value = value;
        this.label = label;
        this.properties = properties;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
