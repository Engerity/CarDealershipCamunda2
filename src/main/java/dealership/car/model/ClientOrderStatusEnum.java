package dealership.car.model;

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

    private final String description;

    ClientOrderStatusEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
