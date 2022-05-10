package midianet.trip.util;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    public static final DateTimeFormatter DATE_DDMMYYYY = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
}
