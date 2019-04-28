package ml.slack.queue.bot.organizer.repository;

import ml.slack.queue.bot.organizer.db.model.Next;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NextRepository extends JpaRepository<Next, Long> {

    Next findByUser_userId(String userId);

}
