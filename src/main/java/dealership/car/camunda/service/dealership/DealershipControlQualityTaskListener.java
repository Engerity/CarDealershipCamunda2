package dealership.car.camunda.service.dealership;

import dealership.car.camunda.service.BaseTaskListener;
import dealership.car.model.OrderStatusEnum;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.springframework.stereotype.Component;

@Component("dealershipControlQuality")
public class DealershipControlQualityTaskListener extends BaseTaskListener {
    @Override
    public OrderStatusEnum getNewOrderStatus() {
        return OrderStatusEnum.DealershipControlQuality;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        super.notify(delegateTask);
    }
}
