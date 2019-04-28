package ml.slack.queue.bot.organizer.repository;

import ml.slack.queue.bot.organizer.db.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUserId(String userId);
}
