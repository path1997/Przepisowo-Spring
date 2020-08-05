package pl.Rzeznicki.Repo;

import pl.Rzeznicki.Entity.Recipes;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface RecipesRepo extends CrudRepository<Recipes, Long> {
    List<Recipes> findByOrderByUsersNicknameAsc();
    List<Recipes> findByOrderByUsersNicknameDesc();
    List<Recipes> findByOrderByLevelAsc();
    List<Recipes> findByOrderByLevelDesc();
    List<Recipes> findByOrderByCategoriesNameAsc();
    List<Recipes> findByOrderByCategoriesNameDesc();
    List<Recipes> findByCategoriesId(long categories_id);




}
