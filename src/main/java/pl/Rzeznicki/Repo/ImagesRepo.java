package pl.Rzeznicki.Repo;

import pl.Rzeznicki.Entity.Images;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface ImagesRepo extends CrudRepository<Images, Long> {

    Images findByRecipesId(long recipes_Id);

}
