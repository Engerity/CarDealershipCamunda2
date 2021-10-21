package dealership.car.camunda.service.factory;

import dealership.car.camunda.service.BaseJavaDelegate;
import dealership.car.model.OrderStatusEnum;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

@Component("equipmentProductionService")
public class EquipmentProductionService extends BaseJavaDelegate {

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
