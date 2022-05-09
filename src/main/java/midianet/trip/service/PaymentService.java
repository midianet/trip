package midianet.trip.service;

import lombok.RequiredArgsConstructor;
import midianet.trip.model.Payment;
import midianet.trip.model.PaymentFilter;
import midianet.trip.repository.PaymentRepository;
import midianet.trip.repository.PaymentRepository;
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
public class PaymentService {

    private final PaymentRepository repository;

    public Payment findById(@NonNull final Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MessageUtil.getMessageNotFound("Passageiro",id)));
    }

    public List<Payment> list() {
        return repository.findAll();
    }

    public Page<Payment> find(@NonNull PaymentFilter filter) {
        final var size = filter.getSize() == -1 ? Integer.MAX_VALUE : filter.getSize();
        final var pageRequest = PageRequest.of(filter.getPage(),
                size,
                Sort.by(filter.getSortDirection(),
                        filter.getSort()));
        return repository.findAll(pageRequest);
    }

    @Transactional
    public void create(@NonNull final Payment payment) {
        payment.setId(null);
        repository.save(payment);
    }

    @Transactional
    public void update(@NonNull final Long id, @NonNull final Payment payment) {
        final var persistent = findById(id);
        BeanUtils.copyProperties(payment, persistent, "id");
        repository.save(persistent);
    }

    @Transactional
    public void deleteById(@NonNull final Long id) {
        repository.delete(findById(id));
    }

}
