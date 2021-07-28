package dealership.car.camunda.service.factory;

import dealership.car.camunda.service.BaseDelegate;
import dealership.car.camunda.service.CamundaProcessService;
import dealership.car.model.OrderStatusEnum;
import dealership.car.repository.OrderRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("factoryOrderReject")
public class FactoryOrderRejectListener extends BaseDelegate implements ExecutionListener {

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected CamundaProcessService camundaProcessService;

    @Override
    public OrderStatusEnum getNewOrderStatus() {
        return OrderStatusEnum.RejectedInFactory;
    }

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        super.execute(delegateExecution);
    }

}
