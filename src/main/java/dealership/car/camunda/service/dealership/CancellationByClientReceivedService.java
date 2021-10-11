package dealership.car.camunda.service.dealership;

import dealership.car.camunda.service.BaseJavaDelegate;
import dealership.car.model.OrderStatusEnum;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

@Component("cancellationByClientReceivedService")
public class CancellationByClientReceivedService extends BaseJavaDelegate {
    @Override
    public OrderStatusEnum getNewOrderStatus() {
        return null;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        super.execute(delegateExecution);

        delegateExecution.setVariable("cancellationRequestCreator", "client");
    }
}
