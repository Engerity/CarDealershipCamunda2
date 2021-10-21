package dealership.car.camunda.service.client;

import dealership.car.camunda.service.BaseJavaDelegate;
import dealership.car.model.OrderStatusEnum;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

@Component("sendAdvancePayment")
public class SendAdvancePaymentService extends BaseJavaDelegate {
    @Override
    public OrderStatusEnum getNewOrderStatus() {
        return null;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        super.execute(delegateExecution);

        camundaProcessService.setVariable(getProcessInstanceId(delegateExecution), "isAdvancePayment", true);
    }
}
