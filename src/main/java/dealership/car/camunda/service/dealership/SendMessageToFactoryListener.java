package dealership.car.camunda.service.dealership;

import dealership.car.camunda.service.CamundaProcessService;
import dealership.car.repository.OrderRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("sendMessageToFactory")
public class SendMessageToFactoryListener implements ExecutionListener {

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected CamundaProcessService camundaProcessService;

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        camundaProcessService.setVariable(delegateExecution.getProcessInstanceId(), "dealershipProcessId", delegateExecution.getProcessInstanceId());
        camundaProcessService.setVariable(delegateExecution.getProcessInstanceId(), "dealeship", delegateExecution.getVariable("assignee"));
    }
}
