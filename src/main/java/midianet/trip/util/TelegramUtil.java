package midianet.trip.util;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Optional;

public class TelegramUtil {

    public static String buildFullName(@NonNull final String firstName,
                                       @Nullable final String lastName){
        final var name = new StringBuilder(firstName);
        Optional.ofNullable(lastName)
                .ifPresent(last -> name.append(" ").append(last));
        return name.toString();
    }

}
