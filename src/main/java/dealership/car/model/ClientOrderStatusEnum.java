package dealership.car.model;

/**
 * Typ wyliczeniowy statusu zamówienia widoczny dla klienta
 */
public enum ClientOrderStatusEnum {

    Registration("Rejestracja"),

    SentToDealership("Wysłane do salonu"),

    Accepted("Przyjęte przez salon"),

    InProgress("W trakcie realizacji"),

    InProductionProgress("W trakcie produkcji"),

    Rejected("Odrzucone"),

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
}
