package midianet.trip.controller;

import lombok.RequiredArgsConstructor;
import midianet.trip.model.Family;
import midianet.trip.model.FamilyFilter;
import midianet.trip.service.FamilyService;
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
@RequestMapping("/family")
public class FamilyController {

    private final FamilyService service;

    @GetMapping("/{id}")
    public Family get(@PathVariable final Integer id) {
        return service.findById(id);
    }


    @GetMapping
    public Page<Family> find(@RequestParam(required = false) final String name,
                                @RequestParam(required = false, defaultValue = DEFAULT_PAGE ) final Integer page,
                                @RequestParam(required = false, defaultValue = DEFAULT_PAGE_SIZE) final Integer size,
                                @RequestParam(required = false, defaultValue = DEFAULT_SORT_DIRECTION) final Sort.Direction sortDirection,
                                @RequestParam(required = false, defaultValue = "name") final String... sort){
        return service.find(FamilyFilter.builder()
                .name(name)
                .page(page)
                .size(size)
                .sortDirection(sortDirection)
                .sort(sort).build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void post(@Valid @RequestBody final Family family, final HttpServletResponse response) {
        service.create(family);
        response.setHeader(HttpHeaders.LOCATION, ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(family.getId()).toUri().toString());
    }

    @PutMapping("/{id}")
    public void put(@PathVariable final Integer id, @Valid @RequestBody final Family family) {
        service.update(id, family);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final Integer id) {
        service.deleteById(id);
    }

}
