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
public abstract class BaseJavaDelegate extends BaseDelegate implements JavaDelegate {



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
        super.execute(delegateExecution);


    }
}
