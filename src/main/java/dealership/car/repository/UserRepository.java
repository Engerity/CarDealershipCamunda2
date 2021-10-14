package dealership.car.repository;

import dealership.car.model.RoleEnum;
import dealership.car.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByName(String name);

    List<User> findAllByNameIsNotIn(List<String> names);

    List<User> findAllByRolesIsIn(List<RoleEnum> names);
}
