package dealership.car.camunda.service;

import dealership.car.model.Order;
import dealership.car.model.OrderModel;
import dealership.car.model.OrderStatusEnum;
import dealership.car.repository.OrderRepository;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("baseTaskListener")
public abstract class BaseTaskListener implements TaskListener {

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected CamundaProcessService camundaProcessService;

    public abstract OrderStatusEnum getNewOrderStatus();

    @Override
    public void notify(DelegateTask delegateTask) {

        Object object = delegateTask.getVariable("orderData");
        OrderModel orderModel = object instanceof OrderModel ? (OrderModel) object : null;

        object = delegateTask.getVariable("orderId");
        String orderId = object instanceof String ? (String) object : null;

        if (StringUtils.isBlank(orderId) && orderModel != null)
            orderId = orderModel.getOrderId();

        // Aktualizacja w bazie danych: last step & order status
        if (StringUtils.isNotBlank(orderId)) {
            Order order = orderRepository.getOne(Long.valueOf(orderId));
            if (order != null) {
                order.getOrderInfo().setLastStep(delegateTask.getTaskDefinitionKey());
                if (getNewOrderStatus() != null && !getNewOrderStatus().equals(order.getOrderStatusEnum()))
                    order.setOrderStatusEnum(getNewOrderStatus());
                orderRepository.save(order);
            }
        }

        // Aktualizacja zmiennych procesu w Camunda: last step
        if (orderModel != null) {
            orderModel.setLastStep(delegateTask.getTaskDefinitionKey());
            delegateTask.setVariable("orderData", orderModel);
            camundaProcessService.setVariable(delegateTask.getProcessInstanceId(), "orderData", orderModel);
        }
    }
}
