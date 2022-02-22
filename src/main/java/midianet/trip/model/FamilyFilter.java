package midianet.trip.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import midianet.trip.util.Filter;

@Data
@SuperBuilder
public class FamilyFilter extends Filter {
    final String name;
}
