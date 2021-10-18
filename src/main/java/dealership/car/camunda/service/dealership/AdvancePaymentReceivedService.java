package dealership.car.camunda.service.dealership;

import dealership.car.camunda.service.BaseJavaDelegate;
import dealership.car.model.OrderStatusEnum;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

@Component("advancePaymentReceivedService")
public class AdvancePaymentReceivedService extends BaseJavaDelegate {
    @Override
    public OrderStatusEnum getNewOrderStatus() {
        return OrderStatusEnum.InProgress;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        super.execute(delegateExecution);

        camundaProcessService.setVariable(getProcessInstanceId(delegateExecution), "isAdvancePayment", true);
    }
}
