package pl.Rzeznicki.Repo;

import pl.Rzeznicki.Entity.Categories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface CategoriesRepo extends CrudRepository<Categories, Long> {
}
