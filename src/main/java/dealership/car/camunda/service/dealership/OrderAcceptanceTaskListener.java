package dealership.car.camunda.service.dealership;

import dealership.car.camunda.service.BaseTaskListener;
import dealership.car.model.OrderStatusEnum;
import org.springframework.stereotype.Component;

@Component("orderAcceptance")
public class OrderAcceptanceTaskListener extends BaseTaskListener {

    @Override
    public OrderStatusEnum getNewOrderStatus() {
        return OrderStatusEnum.Accepted;
    }

}
