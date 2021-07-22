package dealership.car.camunda.service.dealership;

import dealership.car.camunda.service.BaseJavaDelegate;
import dealership.car.model.OrderStatusEnum;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

@Component("preparationForCollection")
public class PreparationForCollectionJavaDelegate extends BaseJavaDelegate {
    @Override
    public OrderStatusEnum getNewOrderStatus() {
        return OrderStatusEnum.PreparationForCollection;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        super.execute(delegateExecution);
    }
}
