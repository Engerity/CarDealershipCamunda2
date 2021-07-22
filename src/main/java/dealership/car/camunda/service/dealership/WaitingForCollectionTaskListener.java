package dealership.car.camunda.service.dealership;

import dealership.car.model.OrderStatusEnum;
import dealership.car.camunda.service.BaseTaskListener;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.springframework.stereotype.Component;

@Component("waitingForCollection")
public class WaitingForCollectionTaskListener extends BaseTaskListener {
    @Override
    public OrderStatusEnum getNewOrderStatus() {
        return OrderStatusEnum.WaitingForCollection;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        super.notify(delegateTask);
    }
}
