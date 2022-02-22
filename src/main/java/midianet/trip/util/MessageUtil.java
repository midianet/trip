package midianet.trip.util;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Locale;

public abstract class MessageUtil {

    private MessageUtil(){}

    private static MessageSource messageSource;

    private static final String DEFAULT_INVALID_VALUE = "midianet.trip.message.default.invalid.value";
    private static final String DEFAULT_REQUIRED      = "midianet.trip.message.default.Required";
    private static final String DEFAULT_FOUND         = "midianet.trip.message.default.found";
    private static final String DEFAULT_NOTFOUND      = "midianet.trip.message.default.notfound";
    private static final Locale DEFAULT_LOCALE        = new Locale("pt","BR");

    static {
        final var loader = new ReloadableResourceBundleMessageSource();
        loader.addBasenames("classpath:ValidationMessages");
        loader.setDefaultEncoding("UTF-8");
        loader.setDefaultLocale(DEFAULT_LOCALE);
        messageSource = loader;
    }

    public static String getMessage(@NonNull final String id, @Nullable final Object... params){
        return getMessageDefault(id, params);
    }

    public static String getMessageRequired(@NonNull final String field){
        return getMessageDefault(DEFAULT_REQUIRED, field);
    }

    public static String messageInvalidValue(@NonNull final String field, @NonNull final Object value){
        return getMessageDefault(DEFAULT_INVALID_VALUE, field,value);
    }

    private static String getMessageDefault(@NonNull final String id, @Nullable final Object... params){
        try {
            return messageSource.getMessage(id, params, DEFAULT_LOCALE);
        }catch (NoSuchMessageException e){
            return "Sem mensagem definida para a chave :" + id;
        }
    }

    public static String getMessageFound(@NonNull final String field){
        return getMessageDefault(DEFAULT_FOUND, field);
    }

    public static String getMessageNotFound(@NonNull final String field, final Object key){
        return getMessageDefault(DEFAULT_NOTFOUND, field, key);
    }

}