package dealership.car.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Typ wyliczeniowy statusu zamówienia widoczny dla klienta
 */
public enum ClientOrderStatusEnum {

    Registration("Rejestracja"),

    SentToDealership("Wysłane do salonu"),

    Accepted("Przyjęte przez salon"),

    WaitingForAdvancePayment("Oczekiwanie na zapłatę zaliczki"),

    WaitingForPayment("Oczekiwanie na zapłatę zamówienia"),

    InProgress("W trakcie realizacji"),

    InProductionProgress("W trakcie produkcji"),

    Rejected("Odrzucone"),

    Cancelled("Anulowane"),

    WaitingForCollection("Gotowe do odbioru"),

    Completed("Zakończone"),

    ;

    /**
     * Słowny opis statusu
     */
    private final String description;

    /**
     * Konstruktor z opisem statusu
     * @param description opis statusu
     */
    ClientOrderStatusEnum(String description) {
        this.description = description;
    }

    /**
     * Zwraca opis statusu ClientOrderStatusEnum
     * @return opis statusu ClientOrderStatusEnum
     */
    public String getDescription() {
        return description;
    }


    /**
     * Zwraca listę nieaktywnych statusów zamówień
     * @return lista nieaktywnych statusów zamówień
     */
    public static List<ClientOrderStatusEnum> notActiveStatuses() {
        return new ArrayList<>(Arrays.asList(Registration, Rejected, Cancelled, Completed));
    }

    /**
     * Zwraca listę aktywnych statusów zamówień
     * @return lista aktywnych statusów zamówień
     */
    public static List<ClientOrderStatusEnum> activeStatuses() {
        List<ClientOrderStatusEnum> result = new ArrayList<>(Arrays.asList(values()));
        result.removeAll(notActiveStatuses());
        return result;
    }
}
