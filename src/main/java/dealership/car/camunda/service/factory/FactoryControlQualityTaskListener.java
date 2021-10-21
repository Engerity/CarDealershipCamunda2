package dealership.car.camunda.service.factory;

import dealership.car.model.OrderStatusEnum;
import dealership.car.camunda.service.BaseTaskListener;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.springframework.stereotype.Component;

@Component("factoryControlQuality")
public class FactoryControlQualityTaskListener extends BaseTaskListener {

    private boolean isOrderCancelled = false;

    @Override
    public OrderStatusEnum getNewOrderStatus() {
        if (isOrderCancelled)
            return null;
        return OrderStatusEnum.FactoryControlQuality;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        Boolean tmp = isOrderCancelled(delegateTask);
        isOrderCancelled = tmp != null && tmp;
        super.notify(delegateTask);
    }
}
