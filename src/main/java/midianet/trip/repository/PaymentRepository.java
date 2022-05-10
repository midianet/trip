package midianet.trip.repository;

import midianet.trip.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    public List<Payment> findByFamilyId(Integer familyId);
}
