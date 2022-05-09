package midianet.trip.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import midianet.trip.util.Filter;

import java.time.LocalDate;

@Data
@SuperBuilder
public class PaymentFilter extends Filter {
    final LocalDate inicio;
    final LocalDate fim;
}
