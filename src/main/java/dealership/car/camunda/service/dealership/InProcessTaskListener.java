package dealership.car.camunda.service.dealership;

import dealership.car.camunda.service.BaseTaskListener;
import dealership.car.model.OrderStatusEnum;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.springframework.stereotype.Component;

@Component("inProcess")
public class InProcessTaskListener extends BaseTaskListener {
    @Override
    public OrderStatusEnum getNewOrderStatus() {
        return OrderStatusEnum.InProgress;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        super.notify(delegateTask);
    }
}
