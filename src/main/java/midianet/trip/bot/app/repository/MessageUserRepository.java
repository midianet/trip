package midianet.trip.bot.app.repository;

import midianet.trip.bot.app.model.MessageUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageUserRepository extends JpaRepository<MessageUser,Long> {

}
