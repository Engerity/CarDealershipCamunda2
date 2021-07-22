package dealership.car.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import java.util.Arrays;
import java.util.List;

@Embeddable
public class OrderInfo extends OrderModel {

    public OrderInfo() {
        super();
    }

    public OrderInfo(String model, String engine, String bodyType, String transmissionType, String client, List<String> additionalEquipments) {
        super(model, engine, bodyType, transmissionType, client, additionalEquipments);
    }

    public OrderInfo(OrderInfo copy) {
        this((OrderModel)copy);
    }

    public OrderInfo(OrderModel copy) {
        super(copy);
    }

    @Column(name = "process_id")
    public String getProcessId() {
        return processId;
    }

    @Column(name = "task_id")
    public String getTaskId() {
        return taskId;
    }

    public String getModel() {
        return model;
    }

    public String getEngine() {
        return engine;
    }

    @Column(name = "body_type")
    public String getBodyType() {
        return bodyType;
    }

    @Column(name = "transmission_type")
    public String getTransmissionType() {
        return transmissionType;
    }

    public String getClient() {
        return client;
    }

    @Column(name = "additional_equipments")
    public String getAdditionalEquipment() {
        return additionalEquipments;
    }

    public void setAdditionalEquipment(String additionalEquipment) {
        this.additionalEquipments = additionalEquipment;
    }

    @Transient
    public List<String> getAdditionalEquipments() {
        if (additionalEquipments == null)
            additionalEquipments = "";
        return Arrays.asList(additionalEquipments.split(","));
    }

    public void setAdditionalEquipments(List<String> additionalEquipments) {
        this.additionalEquipments = String.join(",", additionalEquipments);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
