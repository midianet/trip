package midianet.trip.util;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Sort;

@Data
@SuperBuilder
public abstract class Filter {
    public static final String DEFAULT_PAGE = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT = "id";
    public static final String DEFAULT_SORT_DIRECTION = "ASC";

    private Integer page;
    private Integer size;
    private Sort.Direction sortDirection;
    private String[] sort;

}