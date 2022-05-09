package midianet.trip.controller;

import lombok.RequiredArgsConstructor;
import midianet.trip.model.Payment;
import midianet.trip.model.PaymentFilter;
import midianet.trip.service.PaymentService;
import midianet.trip.service.PaymentService;
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
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService service;

    @GetMapping("/{id}")
    public Payment get(@PathVariable final Long id) {
        return service.findById(id);
    }

    @GetMapping
    public Page<Payment> find(@RequestParam(required = false, defaultValue = DEFAULT_PAGE ) final Integer page,
                              @RequestParam(required = false, defaultValue = DEFAULT_PAGE_SIZE) final Integer size,
                              @RequestParam(required = false, defaultValue = DEFAULT_SORT_DIRECTION) final Sort.Direction sortDirection,
                              @RequestParam(required = false, defaultValue = "date") final String... sort){
        return service.find(PaymentFilter.builder()
                .page(page)
                .size(size)
                .sortDirection(sortDirection)
                .sort(sort).build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void post(@Valid @RequestBody final Payment payment, final HttpServletResponse response) {
        service.create(payment);
        response.setHeader(HttpHeaders.LOCATION, ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(payment.getId()).toUri().toString());
    }

    @PutMapping("/{id}")
    public void put(@PathVariable final Long id, @Valid @RequestBody final Payment payment) {
        service.update(id, payment);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final Long id) {
        service.deleteById(id);
    }

}
