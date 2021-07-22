package dealership.car.converter;

import java.time.format.DateTimeFormatter;

public class CustomDateTimeFormatter {

    public static final DateTimeFormatter DATE_AND_TIME;

    static {
        DATE_AND_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    }
}
