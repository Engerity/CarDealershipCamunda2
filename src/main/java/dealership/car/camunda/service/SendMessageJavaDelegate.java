package dealership.car.camunda.service;

import dealership.car.model.OrderStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.springframework.stereotype.Component;

/**
 * Serwis wykonujący zadanie wysłania wiadomości w procesie BPMN
 */
@Component("sendMessageDelegate")
public class SendMessageJavaDelegate extends BaseJavaDelegate {

    /**
     * Nazwa wiadomości
     */
    private FixedValue messageName;

    /**
     * Flaga czy wysłać proces (true/false)
     */
    private FixedValue startProcess;

    /**
     * Nowy status zamówienia
     */
    private FixedValue newOrderStatus;

    /**
     * Zeleżna nazwa zmiennej z processInstanceId
     */
    private FixedValue correlateProcessInstanceVar;


    @Override
    public OrderStatusEnum getNewOrderStatus() {
        if (newOrderStatus != null && StringUtils.isNotBlank(newOrderStatus.getExpressionText()))
            return OrderStatusEnum.valueOfString(newOrderStatus.getExpressionText());

        return OrderStatusEnum.SentToDealership;
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

        if ("true".equals(startProcess.getExpressionText())) {
            runtimeService.startProcessInstanceByMessage(messageName.getExpressionText(), delegateExecution.getVariables());

        } else {
            MessageCorrelationBuilder builder = runtimeService.createMessageCorrelation(messageName.getExpressionText());

            if (correlateProcessInstanceVar != null)
                builder.processInstanceId((String) delegateExecution.getVariable(correlateProcessInstanceVar.getExpressionText()));

            builder.setVariables(delegateExecution.getVariables())
                    .correlateWithResult();
        }
    }
}
