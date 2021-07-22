package dealership.car.repository;

import dealership.car.model.Order;
import dealership.car.model.OrderStatusEnum;
import dealership.car.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByOwner(User owner);

    List<Order> findAllByOwner_Name(String name);

    List<Order> findAllByOrderStatusEnumIn(List<OrderStatusEnum> enums);

    List<Order> findAllByOrderStatusEnumInAndOwner_Name(List<OrderStatusEnum> enums, String name);

    @Modifying
    @Query("delete from Order o where o.id = ?1")
    void delete(Long id);

    @Modifying
    @Query("delete from Order o where o = ?1")
    void delete(Order order);

}
