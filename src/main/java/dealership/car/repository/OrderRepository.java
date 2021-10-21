package dealership.car.repository;

import dealership.car.model.ClientOrderStatusEnum;
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

    @Query("SELECT o FROM Order o ORDER BY o.creationDate DESC")
    List<Order> findAll();

    List<Order> findAllByOwnerOrderByCreationDateDesc(User owner);

    List<Order> findAllByOwner_NameOrderByCreationDateDesc(String name);

    List<Order> findAllByOrderStatusEnumInOrderByCreationDateDesc(List<OrderStatusEnum> enums);

    List<Order> findAllByClientOrderStatusEnumInOrderByCreationDateDesc(List<ClientOrderStatusEnum> enums);

    List<Order> findAllByOrderStatusEnumInAndOwner_NameOrderByCreationDateDesc(List<OrderStatusEnum> enums, String name);

    List<Order> findAllByClientOrderStatusEnumInAndOwner_NameOrderByCreationDateDesc(List<ClientOrderStatusEnum> enums, String name);

}
