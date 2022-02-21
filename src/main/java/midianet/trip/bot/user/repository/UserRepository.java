package midianet.trip.bot.user.repository;

import midianet.trip.bot.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {

}
