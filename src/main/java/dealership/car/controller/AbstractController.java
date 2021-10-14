package dealership.car.controller;

import dealership.car.camunda.service.CamundaProcessService;
import dealership.car.repository.OrderRepository;
import dealership.car.repository.UserRepository;
import dealership.car.service.AvailableResourcesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

/**
 * Klasa bazowa dla wszystkich kontrolerów
 */
public abstract class AbstractController {

    @Autowired
    protected CamundaProcessService camundaProcessService;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected AvailableResourcesService availableResourcesService;

    /**
     * Pomocnicza metoda uzupełnia w modelu zasoby danych konfiguratora zamówień (lista modeli, silników, typów nadwozia itd.)
     *
     * @param model model danych
     */
    public void fillModelWithAvailableResources(Model model) {
        fillModelWithAvailableResources(model, availableResourcesService);
    }

    /**
     * Pomocnicza metoda uzupełnia w modelu zasoby danych konfiguratora zamówień (lista modeli, silników, typów nadwozia itd.)
     *
     * @param model                     model danych
     * @param availableResourcesService serwis dostępnych zasobów
     */
    public static void fillModelWithAvailableResources(Model model, AvailableResourcesService availableResourcesService) {
        model.addAttribute("carModels", availableResourcesService.getAvailableModels());
        model.addAttribute("carEngines", availableResourcesService.getAvailableEngines());
        model.addAttribute("carBodyTypes", availableResourcesService.getAvailableBodyTypes());
        model.addAttribute("carTransmissionTypes", availableResourcesService.getAvailableTransmissionTypes());
        model.addAttribute("carAdditionalEquipments", availableResourcesService.getAvailableAdditionalEquipments());
    }

}
