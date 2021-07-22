package dealership.car.camunda.service;

import dealership.car.model.Order;
import dealership.car.model.OrderModel;
import dealership.car.model.OrderStatusEnum;
import dealership.car.repository.OrderRepository;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("baseJavaDelegate")
public abstract class BaseJavaDelegate implements JavaDelegate {

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

    /**
     * Metoda aktualizujące dane zamówienia (w bazie danych oraz zmiennych camunda)
     * @param delegateExecution dane delegowanego zadania
     */
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Object object = delegateExecution.getVariable("orderData");
        OrderModel orderModel = object instanceof OrderModel ? (OrderModel) object : null;

        object = delegateExecution.getVariable("orderId");
        String orderId = object instanceof String ? (String) object : null;

        if (StringUtils.isBlank(orderId) && orderModel != null)
            orderId = orderModel.getOrderId();

        // Aktualizacja w bazie danych: last step & order status
        if (StringUtils.isNotBlank(orderId)) {
            Order order = orderRepository.findById(Long.valueOf(orderId)).orElse(null);
            if (order != null) {
                order.getOrderInfo().setLastStep(delegateExecution.getCurrentActivityId());
                if (getNewOrderStatus() != null && !getNewOrderStatus().equals(order.getOrderStatusEnum()))
                    order.setOrderStatusEnum(getNewOrderStatus());
                orderRepository.save(order);
            }
        }

        // Aktualizacja zmiennych procesu w Camunda: last step 
        if (orderModel != null) {
            orderModel.setLastStep(delegateExecution.getCurrentActivityId());
            delegateExecution.setVariable("orderData", orderModel);
            camundaProcessService.setVariable(delegateExecution.getProcessInstanceId(), "orderData", orderModel);
        }
    }
}
