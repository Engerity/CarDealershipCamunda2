package dealership.car.controller;

import dealership.car.camunda.service.CamundaProcessService;
import dealership.car.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Klasa bazowa dla wszystkich kontrolerów
 */
public abstract class AbstractController {

    @Autowired
    protected CamundaProcessService camundaProcessService;

    @Autowired
    protected OrderRepository orderRepository;

}
