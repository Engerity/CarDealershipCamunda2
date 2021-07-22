package dealership.car.camunda.service.client;

import dealership.car.camunda.service.BaseJavaDelegate;
import dealership.car.model.OrderModel;
import dealership.car.model.OrderStatusEnum;
import dealership.car.model.RoleEnum;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

@Component("receiveMessageClientService")
public class ReceiveMessageClientService extends BaseJavaDelegate {

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
        camundaProcessService.setVariable(delegateExecution.getProcessInstanceId(),"userGroup", RoleEnum.ROLE_CLIENT.getValue());
        OrderModel orderModel = (OrderModel) delegateExecution.getVariable("orderData");
        if (orderModel != null)
            camundaProcessService.setVariable(delegateExecution.getProcessInstanceId(),"assignee", orderModel.getClient());
    }
}
