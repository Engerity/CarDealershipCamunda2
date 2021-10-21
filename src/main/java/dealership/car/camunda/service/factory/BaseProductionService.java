package dealership.car.camunda.service.factory;

import dealership.car.model.OrderStatusEnum;
import dealership.car.camunda.service.BaseJavaDelegate;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

@Component("baseProductionService")
public class BaseProductionService extends BaseJavaDelegate {

    private boolean isOrderCancelled = false;

    @Override
    public OrderStatusEnum getNewOrderStatus() {
        if (isOrderCancelled)
            return null;
        return OrderStatusEnum.InProductionProgress;
    }

    /**
     * Metoda wywoływana dla delegowanego zadania
     *
     * @param delegateExecution delegowane wykonanie
     * @throws Exception błędy wykonania
     */
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Boolean tmp = isOrderCancelled(delegateExecution);
        isOrderCancelled = tmp != null && tmp;
        super.execute(delegateExecution);
    }
}
