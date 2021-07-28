package dealership.car.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Typ wyliczeniowy statusu roboczego zamówienia w systemie (widoczny dla pracowników salonu i fabryki)
 */
public enum OrderStatusEnum {

    Registration(ClientOrderStatusEnum.Registration),

    SentToDealership(ClientOrderStatusEnum.SentToDealership),

    Accepted(ClientOrderStatusEnum.Accepted),

    InProgress(ClientOrderStatusEnum.InProgress),

    SentToFactory("Wysłane do fabryki"),

    InProductionProgress(ClientOrderStatusEnum.InProductionProgress),

    FactoryControlQuality("Kontrola jakości (fabryka)"),

    RejectedInFactory("Odrzucone w fabryce"),

    DealershipControlQuality(ClientOrderStatusEnum.InProgress, "Kontrola jakości (salon)"),

    InTransport("W transporcie do salonu"),

    Rejected(ClientOrderStatusEnum.Rejected),

    PreparationForCollection("W przygotowaniu do wydania"),

    WaitingForCollection(ClientOrderStatusEnum.WaitingForCollection),

    Completed(ClientOrderStatusEnum.Completed),
    ;

    /**
     * Status widoczny dla klienta
     */
    private final ClientOrderStatusEnum clientOrderStatusEnum;

    /**
     * Słowny opis statusu
     */
    private final String description;

    /**
     * Konstruktor z odpowiadajacym statusem widocznym klienta ClientOrderStatusEnum
     * @param clientOrderStatusEnum status widoczny dla klienta
     */
    OrderStatusEnum(ClientOrderStatusEnum clientOrderStatusEnum) {
        this.clientOrderStatusEnum = clientOrderStatusEnum;
        this.description = clientOrderStatusEnum.getDescription();
    }

    /**
     * Konstruktor z odpowiadajacym statusem widocznym klienta ClientOrderStatusEnum
     * oraz opisem statusu
     * @param clientOrderStatusEnum status widoczny dla klienta
     * @param description opis statusu
     */
    OrderStatusEnum(ClientOrderStatusEnum clientOrderStatusEnum, String description) {
        this.clientOrderStatusEnum = clientOrderStatusEnum;
        this.description = description;
    }

    /**
     * Konstruktor z opisem statusu
     * @param description opis statusu
     */
    OrderStatusEnum(String description) {
        this.clientOrderStatusEnum = null;
        this.description = description;
    }

    /**
     * Zwraca odpowiadajacy status widoczny dla klienta ClientOrderStatusEnum
     * @return odpowiadajacy status widoczny dla klienta ClientOrderStatusEnum
     */
    public ClientOrderStatusEnum getClientOrderStatusEnum() {
        return clientOrderStatusEnum;
    }

    /**
     * Zwraca opis statusu OrderStatusEnum
     * @return opis statusu OrderStatusEnum
     */
    public String getDescription() {
        return description;
    }

    /**
     * Zamienia tekst na OrderStatusEnum
     * @param name tekst
     * @return OrderStatusEnum lub null, gdy błąd
     */
    public static OrderStatusEnum valueOfString(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException | NullPointerException ignored) {

        }
        return null;
    }

    /**
     * Zwraca listę nieaktywnych statusów zamówień
     * @return lista nieaktywnych statusów zamówień
     */
    public static List<OrderStatusEnum> notActiveStatuses() {
        return new ArrayList<>(Arrays.asList(Registration, Rejected, Completed));
    }

    /**
     * Zwraca listę aktywnych statusów zamówień
     * @return lista aktywnych statusów zamówień
     */
    public static List<OrderStatusEnum> activeStatuses() {
        List<OrderStatusEnum> result = new ArrayList<>(Arrays.asList(values()));
        result.removeAll(notActiveStatuses());
        return result;
    }

    /**
     * Zwraca nieaktywne statusy zamówień jako tekst (statusy oddzelone przecinkami)
     * @return nieaktywne statusy zamówień
     */
    public static String notActiveStatusesAsString() {
        List<String> list = new ArrayList<>();
        for (OrderStatusEnum s : notActiveStatuses()) {
            if (s != null)
                list.add(s.name());
        }
        return String.join(",", list);
    }

    /**
     * Zwraca aktywne statusy zamówień jako tekst (statusy oddzelone przecinkami)
     * @return laktywne statusy zamówień
     */
    public static String activeStatusesAsString() {
        List<String> list = new ArrayList<>();
        for (OrderStatusEnum s : activeStatuses()) {
            if (s != null)
                list.add(s.name());
        }
        return String.join(",", list);
    }

    /**
     * Zamienia listę String na listę OrderStatusEnum
     * @param orderStatus lista String
     * @return lista OrderStatusEnum
     */
    public static List<OrderStatusEnum> getOrderStatusEnumList(List<String> orderStatus) {
        List<OrderStatusEnum> orderStatusEnum = null;
        if (orderStatus != null && !orderStatus.isEmpty()) {
            orderStatusEnum = new ArrayList<>();
            for (String val : orderStatus) {
                OrderStatusEnum tmp = OrderStatusEnum.valueOfString(val);
                if (tmp != null)
                    orderStatusEnum.add(tmp);
            }
        }
        return orderStatusEnum;
    }
}
