package pl.Rzeznicki.Repo;

import pl.Rzeznicki.Entity.Ratings;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface RatingsRepo extends CrudRepository<Ratings, Long> {
    List<Ratings> findByRecipesId(long recipes_Id);
    Ratings findByUsersId(long users_id);

}
