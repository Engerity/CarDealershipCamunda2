package dealership.car.camunda.service;

import dealership.car.model.Order;
import dealership.car.model.OrderModel;
import dealership.car.model.OrderStatusEnum;
import dealership.car.repository.OrderRepository;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseDelegate {

    protected OrderModel orderModel;

    protected String orderId;


    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected RuntimeService runtimeService;

    @Autowired
    protected CamundaProcessService camundaProcessService;

    /**
     * Abstrakcyjna metoda zwracajaca nowy status zamówienia
     * @return nowy status zamówienia
     */
    public abstract OrderStatusEnum getNewOrderStatus();

    public String getStepDefinitionId(VariableScope delegate) {
        if (delegate instanceof DelegateExecution)
            return ((DelegateExecution) delegate).getCurrentActivityId();
        else if (delegate instanceof DelegateTask)
            return ((DelegateTask) delegate).getTaskDefinitionKey();

        return null;
    }

    public String getProcessInstanceId(VariableScope delegate) {
        if (delegate instanceof DelegateExecution)
            return ((DelegateExecution) delegate).getProcessInstanceId();
        else if (delegate instanceof DelegateTask)
            return ((DelegateTask) delegate).getProcessInstanceId();

        return null;
    }

    /**
     * Metoda aktualizujące dane zamówienia (w bazie danych oraz zmiennych camunda)
     * @param delegateExecution dane delegowanego zadania
     */
    public void execute(VariableScope delegateExecution) {
        Object object = delegateExecution.getVariable("orderData");
        orderModel = object instanceof OrderModel ? (OrderModel) object : null;

        object = delegateExecution.getVariable("orderId");
        orderId = object instanceof String ? (String) object : null;

        if (StringUtils.isBlank(orderId) && orderModel != null)
            orderId = orderModel.getOrderId();

        // Aktualizacja w bazie danych: last step & order status
        if (StringUtils.isNotBlank(orderId)) {
            Order order = orderRepository.findById(Long.valueOf(orderId)).orElse(null);
            if (order != null) {
                order.getOrderInfo().setLastStep(getStepDefinitionId(delegateExecution));
                if (getNewOrderStatus() != null && !getNewOrderStatus().equals(order.getOrderStatusEnum()))
                    order.setOrderStatusEnum(getNewOrderStatus());
                orderRepository.save(order);
            }
        }

        // Aktualizacja zmiennych procesu w Camunda: last step
        if (orderModel != null) {
            orderModel.setLastStep(getStepDefinitionId(delegateExecution));
            delegateExecution.setVariable("orderData", orderModel);
            camundaProcessService.setVariable(getProcessInstanceId(delegateExecution), "orderData", orderModel);
        }
    }
}
