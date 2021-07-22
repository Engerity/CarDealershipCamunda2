package dealership.car.camunda.service.factory;

import dealership.car.model.OrderStatusEnum;
import dealership.car.camunda.service.BaseTaskListener;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.springframework.stereotype.Component;

@Component("factoryControlQuality")
public class FactoryControlQualityTaskListener extends BaseTaskListener {
    @Override
    public OrderStatusEnum getNewOrderStatus() {
        return OrderStatusEnum.FactoryControlQuality;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        super.notify(delegateTask);
    }
}
