package pl.Rzeznicki.Repo;

import pl.Rzeznicki.Entity.Commentaries;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface CommentariesRepo extends CrudRepository<Commentaries, Long> {
    List<Commentaries> findByRecipesId(long recipes_id);
}
