package midianet.trip.service;

import lombok.RequiredArgsConstructor;
import midianet.trip.model.Family;
import midianet.trip.model.FamilyFilter;
import midianet.trip.repository.FamilyRepository;
import midianet.trip.util.MessageUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyRepository repository;

    @Transactional
    public void create(@NonNull final Family family) {
        family.setId(null);
        repository.save(family);
    }

    @Transactional
    public void update(@NonNull final Integer id, @NonNull final Family family) {
        final var persistent = findById(id);
        BeanUtils.copyProperties(family, persistent, "id");
        repository.save(persistent);
    }

    @Transactional
    public void deleteById(@NonNull final Integer id) {
        repository.delete(findById(id));
    }

    public Family findById(@NonNull final Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MessageUtil.getMessageNotFound("Família",id)));
    }

    public List<Family> list() {
        return repository.findAll();
    }

    public Page<Family> find(@NonNull FamilyFilter filter) {
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
            return repository.findAll(Example.of(Family.builder()
                    .name(filter.getName()).build(), matcher), pageRequest);
        }
    }

}