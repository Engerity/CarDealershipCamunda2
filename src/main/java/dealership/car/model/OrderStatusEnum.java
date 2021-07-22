package dealership.car.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public enum OrderStatusEnum {

    Registration(ClientOrderStatusEnum.Registration),

    SentToDealership(ClientOrderStatusEnum.SentToDealership),

    Accepted(ClientOrderStatusEnum.Accepted),

    InProgress(ClientOrderStatusEnum.InProgress),

    SentToFactory("Wysłane do fabryki"),

    InProductionProgress(ClientOrderStatusEnum.InProductionProgress),

    FactoryControlQuality("Kontrola jakości (fabryka)"),

    DealershipControlQuality(ClientOrderStatusEnum.InProgress, "Kontrola jakości (salon)"),

    InTransport("W transporcie do salonu"),

    Rejected(ClientOrderStatusEnum.Rejected),

    PreparationForCollection("W przygotowaniu do wydania"),

    WaitingForCollection(ClientOrderStatusEnum.WaitingForCollection),

    Completed(ClientOrderStatusEnum.Completed),
    ;

    private final ClientOrderStatusEnum clientOrderStatusEnum;

    private final String description;

    OrderStatusEnum(ClientOrderStatusEnum clientOrderStatusEnum) {
        this.clientOrderStatusEnum = clientOrderStatusEnum;
        this.description = clientOrderStatusEnum.getDescription();
    }

    OrderStatusEnum(ClientOrderStatusEnum clientOrderStatusEnum, String description) {
        this.clientOrderStatusEnum = clientOrderStatusEnum;
        this.description = description;
    }

    OrderStatusEnum(String description) {
        this.clientOrderStatusEnum = null;
        this.description = description;
    }

    public ClientOrderStatusEnum getClientOrderStatusEnum() {
        return clientOrderStatusEnum;
    }

    public String getDescription() {
        return description;
    }

    public static OrderStatusEnum valueOfString(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException | NullPointerException ignored) {

        }
        return null;
    }

    public static List<OrderStatusEnum> notActiveStatuses() {
        return new ArrayList<>(Arrays.asList(Registration, Rejected, Completed));
    }

    public static List<OrderStatusEnum> activeStatuses() {
        List<OrderStatusEnum> result = new ArrayList<>(Arrays.asList(values()));
        result.removeAll(notActiveStatuses());
        return result;
    }

    public static String notActiveStatusesAsString() {
        List<String> list = new ArrayList<>();
        for (OrderStatusEnum s : notActiveStatuses()) {
            if (s != null)
                list.add(s.name());
        }
        return String.join(",", list);
    }

    public static String activeStatusesAsString() {
        List<String> list = new ArrayList<>();
        for (OrderStatusEnum s : activeStatuses()) {
            if (s != null)
                list.add(s.name());
        }
        return String.join(",", list);
    }
}
