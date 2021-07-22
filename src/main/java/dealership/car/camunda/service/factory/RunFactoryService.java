package dealership.car.camunda.service.factory;

import dealership.car.model.OrderStatusEnum;
import dealership.car.model.RoleEnum;
import dealership.car.camunda.service.BaseJavaDelegate;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

@Component("runFactoryService")
public class RunFactoryService extends BaseJavaDelegate {

    @Override
    public OrderStatusEnum getNewOrderStatus() {
        return null;
    }

    /**
     * Metoda wywoływana dla delegowanego zadania
     *
     * @param delegateExecution delegowane wykonanie
     * @throws Exception błędy wykonania
     */
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        super.execute(delegateExecution);
        delegateExecution.setVariable("userGroup", RoleEnum.ROLE_FACTORY_WORKER.getValue());
        delegateExecution.setVariable("assignee", "system");
    }
}
