package midianet.trip.bot.user.repository;

import midianet.trip.bot.user.domain.MessageUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageUserRepository extends JpaRepository<MessageUser,Long> {

}
