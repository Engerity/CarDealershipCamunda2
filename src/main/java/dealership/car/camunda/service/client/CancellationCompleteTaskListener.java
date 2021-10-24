package dealership.car.camunda.service.client;

import dealership.car.camunda.service.BaseTaskListener;
import dealership.car.model.OrderStatusEnum;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.springframework.stereotype.Component;

@Component("cancellationComplete")
public class CancellationCompleteTaskListener extends BaseTaskListener {
    @Override
    public OrderStatusEnum getNewOrderStatus() {
        return OrderStatusEnum.Rejected;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        super.notify(delegateTask);
    }
}
