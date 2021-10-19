package dealership.car.controller;

import dealership.car.config.ProcessKey;
import dealership.car.model.*;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.function.Predicate;

/**
 * Kontroler dostępny w ramach operacji Salonu i Fabryki
 */
@Controller
@RequestMapping("/orders")
public class DealershipOrderController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(DealershipOrderController.class);

    /**
     * Lista zamówień w systemie
     * @param userDetail informacje o zalogowanym użytkowniku
     * @param active czy tylko aktywne zamówienia (opcjonalnie)
     * @param model model danych przesyłany do widoku
     * @return nazwa szablonu widoku
     */
    @GetMapping()
    public String viewOrdersList(@AuthenticationPrincipal UserDetailsSecurity userDetail,
                                 @RequestParam(required = false) Integer active,
                                 Model model) {

        List<Order> orders;
        if (active != null && active == 1) {
            orders = orderRepository.findAllByOrderStatusEnumInOrderByCreationDateDesc(OrderStatusEnum.activeStatuses());
        } else if (active != null && active == 0) {
            orders = orderRepository.findAllByOrderStatusEnumInOrderByCreationDateDesc(OrderStatusEnum.notActiveStatuses());
        } else {
           orders = orderRepository.findAll();
        }

        model.addAttribute("urlPath", "/orders");
        model.addAttribute("urlActiveStatuses", "/orders?active=1");
        model.addAttribute("urlNotActiveStatuses", "/orders?active=0");
        model.addAttribute("orders", orders);
        model.addAttribute("viewLabel", "Zamówienia");
        return "ordersList";
    }

    /**
     * Obsługuje anulowanie zamówienia przez klienta.
     *
     * @param orderId    id zamówienia
     * @param userDetail informacje o zalogowanym użytkowniku
     * @param redirect   obiekt informacji przekierowania
     * @return przekierowanie na listę zamówień
     */
    @GetMapping("/cancel/{orderId}")
    public String cancelOrder(@PathVariable("orderId") Long orderId, @AuthenticationPrincipal UserDetailsSecurity userDetail, RedirectAttributes redirect) {
        final String REDIRECT_URL = "redirect:/orders";
        Optional<Order> optional = orderRepository.findById(orderId);
        Order order = optional.orElse(null);
        if (order == null) {
            redirect.addFlashAttribute("globalError", "Nie udało się znaleźć zamówienia (" + orderId + ").");
            return REDIRECT_URL;
        }

        if (!userDetail.getAuthorities().contains(RoleEnum.ROLE_ADMIN) && !userDetail.getAuthorities().contains(RoleEnum.ROLE_DEALERSHIP)) {
            redirect.addFlashAttribute("globalError", "Brak uprawnień do wykonania operacji anulowania zamówienia.");
            return REDIRECT_URL;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("cancellationRequestCreator", "dealership");
        // Wyszukuje procesy po stronie salonu i wysyła wiadomość anulowania
        for (ProcessInstance processInstance : camundaProcessService.getProcessInstancesForOrderId(orderId+"", new String[]{"CAR_DEALERSHIP_SALON"})) {
            camundaProcessService.createMessage("CancellationMessage", processInstance.getProcessInstanceId(), variables);
        }

        order.setClientOrderStatusEnum(ClientOrderStatusEnum.Cancelled);
        orderRepository.save(order);

        return REDIRECT_URL;
    }

    /**
     * Wybór klienta do kreatora zamówienia uruchomionego przez pracowanika salonu.
     *
     * @param model model danych przesyłany do widoku
     * @return nazwa szablonu widoku
     */
    @GetMapping("/newOrderForClient")
    public String createNewOrderForClient(Model model) {
        List<User> clients = userRepository.findAllByRolesIsIn(Collections.singletonList(RoleEnum.ROLE_CLIENT));
        clients.removeIf(user -> "system".equalsIgnoreCase(user.getName()) || "admin".equalsIgnoreCase(user.getName()));
        model.addAttribute("clients", clients);
        model.addAttribute("selectedClient", "");
        return "dealership_orderClient";
    }

    /**
     * Rozpoczęcie zamówienia dla wybranego klienta.
     *
     * @param selectedClient parametr z wybranym użytkownikiem
     * @param model          model danych przesyłany do widoku
     * @return nazwa szablonu widoku
     */
    @PostMapping("/newOrderForClient")
    public String startNewOrderForClient(Model model, @RequestParam String selectedClient) {
        OrderModel orderModel = new OrderModel();
        orderModel.setClient(selectedClient);

        Map<String, Object> variables = new HashMap<>();
        variables.put("assignee", selectedClient);
        variables.put("orderData", orderModel);
        String processInstanceId = camundaProcessService.startProcess(ProcessKey.CAR_DEALERSHIP_KLIENT, variables);
        orderModel.setProcessId(processInstanceId);

        List<Task> taskList = camundaProcessService.getTasksForProcessAndAssignee(processInstanceId, selectedClient);
        if (!taskList.isEmpty())
            orderModel.setTaskId(taskList.get(0).getId());

        camundaProcessService.setVariable(processInstanceId, "orderData", orderModel);

        fillModelWithAvailableResources(model, availableResourcesService);
        model.addAttribute("orderModel", orderModel);
        return "client_orderConfig";
    }
}
