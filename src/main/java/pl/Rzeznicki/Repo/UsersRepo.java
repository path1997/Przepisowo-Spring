package pl.Rzeznicki.Repo;

import pl.Rzeznicki.Entity.Users;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface UsersRepo extends CrudRepository<Users, Long> {
    Users findByLogin(String login);

}
