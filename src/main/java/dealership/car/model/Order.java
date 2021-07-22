package dealership.car.model;

import dealership.car.converter.CustomDateTimeFormatter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "client_order")
public class Order {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_client")
    private ClientOrderStatusEnum clientOrderStatusEnum;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_dealership")
    private OrderStatusEnum orderStatusEnum;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Embedded
    private OrderInfo orderInfo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    @Transient
    public String getCreationDateAsString() {
        if (creationDate == null)
            return null;
        return creationDate.format(CustomDateTimeFormatter.DATE_AND_TIME);
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public ClientOrderStatusEnum getClientOrderStatusEnum() {
        return clientOrderStatusEnum;
    }

    public void setClientOrderStatusEnum(ClientOrderStatusEnum clientOrderStatusEnum) {
        this.clientOrderStatusEnum = clientOrderStatusEnum;
    }

    public OrderStatusEnum getOrderStatusEnum() {
        return orderStatusEnum;
    }

    public void setOrderStatusEnum(OrderStatusEnum orderStatusEnum) {
        this.orderStatusEnum = orderStatusEnum;
        if (orderStatusEnum.getClientOrderStatusEnum() != null)
            setClientOrderStatusEnum(orderStatusEnum.getClientOrderStatusEnum());
    }

    public OrderInfo getOrderInfo() {
        if (orderInfo == null)
            orderInfo = new OrderInfo();
        return orderInfo;
    }

    public void setOrderInfo(OrderInfo orderInfo) {
        this.orderInfo = orderInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
                //&& Objects.equals(owner, order.owner) && clientOrderStatusEnum == order.clientOrderStatusEnum && orderStatusEnum == order.orderStatusEnum && Objects.equals(orderInfo, order.orderInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, owner, clientOrderStatusEnum, orderStatusEnum, orderInfo, creationDate);
    }
}
