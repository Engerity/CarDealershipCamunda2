package dealership.car.camunda.service.dealership;

import dealership.car.camunda.service.CamundaProcessService;
import dealership.car.model.OrderStatusEnum;
import dealership.car.model.RoleEnum;
import dealership.car.camunda.service.BaseJavaDelegate;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("receiveFactoryMessageService")
public class ReceiveFactoryMessageService extends BaseJavaDelegate {

    @Autowired
    protected CamundaProcessService camundaProcessService;

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
        camundaProcessService.setVariable(delegateExecution.getProcessInstanceId(),"userGroup", RoleEnum.ROLE_DEALERSHIP.getValue());
        String dealeship = (String) delegateExecution.getVariable("dealeship");
        if (StringUtils.isNotBlank(dealeship))
            camundaProcessService.setVariable(delegateExecution.getProcessInstanceId(),"assignee", dealeship);
    }
}
