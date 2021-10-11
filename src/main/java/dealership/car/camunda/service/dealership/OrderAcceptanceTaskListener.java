package dealership.car.camunda.service.dealership;

import dealership.car.camunda.service.BaseTaskListener;
import dealership.car.model.OrderStatusEnum;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.springframework.stereotype.Component;

@Component("orderAcceptance")
public class OrderAcceptanceTaskListener extends BaseTaskListener {

    @Override
    public OrderStatusEnum getNewOrderStatus() {
        return OrderStatusEnum.Accepted;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        super.notify(delegateTask);
        delegateTask.setVariable("dealershipProcessId", delegateTask.getProcessInstanceId());
    }
}
