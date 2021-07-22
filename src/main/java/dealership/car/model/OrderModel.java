package dealership.car.model;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@MappedSuperclass
public class OrderModel implements Serializable {

    private static final long serialVersionUID = 6549881889832859363L;

    protected String orderId;
    protected String processId;
    protected String taskId;
    @NotEmpty(message = "Wybór modelu jest wymagany")
    protected String model;
    @NotEmpty(message = "Wybór silnika jest wymagany")
    protected String engine;
    @NotEmpty(message = "Wybór nadwozia jest wymagany")
    protected String bodyType;
    @NotEmpty(message = "Wybór skrzyni biegów jest wymagany")
    protected String transmissionType;
    protected String client;
    protected String additionalEquipments;
    protected String lastStep;

    public OrderModel() {
    }

    public OrderModel(String model, String engine, String bodyType, String transmissionType, String client, List<String> additionalEquipments) {
        this.model = model;
        this.engine = engine;
        this.bodyType = bodyType;
        this.transmissionType = transmissionType;
        this.client = client;
        this.additionalEquipments = String.join(",", additionalEquipments);
    }

    public OrderModel(OrderModel copy) {
        this.orderId = copy.orderId;
        this.model = copy.model;
        this.engine = copy.engine;
        this.bodyType = copy.bodyType;
        this.transmissionType = copy.transmissionType;
        this.client = copy.client;
        this.additionalEquipments = copy.additionalEquipments;
        this.lastStep = copy.lastStep;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public String getTransmissionType() {
        return transmissionType;
    }

    public void setTransmissionType(String transmissionType) {
        this.transmissionType = transmissionType;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getLastStep() {
        return lastStep;
    }

    public void setLastStep(String lastStep) {
        this.lastStep = lastStep;
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

    @Transient
    public boolean isEmpty() {
        return !(StringUtils.isNotBlank(orderId)
                || StringUtils.isNotBlank(processId)
                || StringUtils.isNotBlank(taskId)
                || StringUtils.isNotBlank(model)
                || StringUtils.isNotBlank(engine)
                || StringUtils.isNotBlank(bodyType)
                || StringUtils.isNotBlank(transmissionType)
                || StringUtils.isNotBlank(client)
                || StringUtils.isNotBlank(lastStep)
                || !getAdditionalEquipments().isEmpty());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderModel that = (OrderModel) o;
        return Objects.equals(orderId, that.orderId) && Objects.equals(processId, that.processId) && Objects.equals(taskId, that.taskId) && Objects.equals(model, that.model) && Objects.equals(engine, that.engine) && Objects.equals(bodyType, that.bodyType) && Objects.equals(transmissionType, that.transmissionType) && Objects.equals(client, that.client) && Objects.equals(additionalEquipments, that.additionalEquipments) && Objects.equals(lastStep, that.lastStep);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, processId, taskId, model, engine, bodyType, transmissionType, client, additionalEquipments, lastStep);
    }

    @Override
    public String toString() {
        return "OrderModel{" +
                "orderId='" + orderId + '\'' +
                ", processId='" + processId + '\'' +
                ", taskId='" + taskId + '\'' +
                ", model='" + model + '\'' +
                ", engine='" + engine + '\'' +
                ", bodyType='" + bodyType + '\'' +
                ", transmissionType='" + transmissionType + '\'' +
                ", client='" + client + '\'' +
                ", additionalEquipments='" + additionalEquipments + '\'' +
                ", lastStep='" + lastStep + '\'' +
                '}';
    }
}
