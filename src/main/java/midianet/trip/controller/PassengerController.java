package midianet.trip.controller;

import lombok.RequiredArgsConstructor;
import midianet.trip.model.Passenger;
import midianet.trip.model.PassengerFilter;
import midianet.trip.service.PassengerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import static midianet.trip.util.Filter.*;

@Validated
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/passenger")
public class PassengerController {

    private final PassengerService service;

    @GetMapping("/{id}")
    public Passenger get(@PathVariable final Long id) {
        return service.findById(id);
    }

    @GetMapping
    public Page<Passenger> find(@RequestParam(required = false) final String name,
                                @RequestParam(required = false, defaultValue = DEFAULT_PAGE ) final Integer page,
                                @RequestParam(required = false, defaultValue = DEFAULT_PAGE_SIZE) final Integer size,
                                @RequestParam(required = false, defaultValue = DEFAULT_SORT_DIRECTION) final Sort.Direction sortDirection,
                                @RequestParam(required = false, defaultValue = "name") final String... sort){
        return service.find(PassengerFilter.builder()
                .name(name)
                .page(page)
                .size(size)
                .sortDirection(sortDirection)
                .sort(sort).build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void post(@Valid @RequestBody final Passenger passenger, final HttpServletResponse response) {
        service.create(passenger);
        response.setHeader(HttpHeaders.LOCATION, ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(passenger.getId()).toUri().toString());
    }

    @PutMapping("/{id}")
    public void put(@PathVariable final Long id, @Valid @RequestBody final Passenger passenger) {
        service.update(id, passenger);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final Long id) {
        service.deleteById(id);
    }

}
