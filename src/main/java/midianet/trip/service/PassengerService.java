package midianet.trip.service;

import lombok.RequiredArgsConstructor;
import midianet.trip.model.Passenger;
import midianet.trip.model.PassengerFilter;
import midianet.trip.repository.PassengerRepository;
import midianet.trip.util.MessageUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PassengerService {

    private final PassengerRepository repository;

    public Passenger findById(@NonNull final String id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MessageUtil.getMessageNotFound("Passageiro",id)));
    }

    public List<Passenger> list() {
        return repository.findAll();
    }

    public Page<Passenger> find(@NonNull PassengerFilter filter) {
        final var size = filter.getSize() == -1 ? Integer.MAX_VALUE : filter.getSize();
        final var pageRequest = PageRequest.of(filter.getPage(),
                size,
                Sort.by(filter.getSortDirection(),
                        filter.getSort()));
        if (Objects.isNull(filter.getName())) {
            return repository.findAll(pageRequest);
        } else {
            final var matcher = ExampleMatcher
                    .matchingAll()
                    .withMatcher("name", ExampleMatcher
                            .GenericPropertyMatchers.startsWith()
                            .ignoreCase());
            return repository.findAll(Example.of(Passenger.builder()
                    .name(filter.getName()).build(), matcher), pageRequest);
        }
    }

    @Transactional
    public void create(@NonNull final Passenger passenger) {
        passenger.setId(Optional.ofNullable(passenger.getId())
            .orElse(String.valueOf(UUID.randomUUID().getLeastSignificantBits())).replace("-",""));
        passenger.setStatus(Passenger.Status.INTERESTED);
        repository.save(passenger);
    }

    @Transactional
    public void update(@NonNull final String id, @NonNull final Passenger passenger) {
        final var persistent = findById(id);
        BeanUtils.copyProperties(passenger, persistent, "id");
        repository.save(persistent);
    }

    @Transactional
    public void deleteById(@NonNull final String id) {
        repository.delete(findById(id));
    }

}
