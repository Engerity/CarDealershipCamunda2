package dealership.car.camunda.service;

import dealership.car.model.OrderStatusEnum;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * Depozyt samochodu
 */
@Component("carDeposit")
public class CarDepositJavaDelegate extends BaseJavaDelegate {
    @Override
    public OrderStatusEnum getNewOrderStatus() {
        return null;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        super.execute(delegateExecution);
    }
}
