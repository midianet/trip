package midianet.trip.repository;

import midianet.trip.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassengerRepository extends JpaRepository<Passenger,String> {

}
