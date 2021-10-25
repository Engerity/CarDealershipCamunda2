package dealership.car.camunda.service.dealership;

import dealership.car.camunda.service.BaseTaskListener;
import dealership.car.model.OrderStatusEnum;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.springframework.stereotype.Component;

@Component("dealershipControlQuality")
public class DealershipControlQualityTaskListener extends BaseTaskListener {

    private boolean orderCancelled = false;

    @Override
    public OrderStatusEnum getNewOrderStatus() {
        if (orderCancelled)
            return null;
        return OrderStatusEnum.DealershipControlQuality;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        Object tmpVar = delegateTask.getVariable("orderCancelled");
        if (tmpVar instanceof Boolean)
            orderCancelled = (Boolean) tmpVar;
        super.notify(delegateTask);
    }
}
