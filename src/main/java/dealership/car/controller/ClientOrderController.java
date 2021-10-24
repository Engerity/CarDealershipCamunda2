package dealership.car.controller;

import dealership.car.config.ProcessKey;
import dealership.car.model.*;
import dealership.car.repository.OrderRepository;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

/**
 * Kontroler dostępny w ramach operacji Klienta
 */
@Controller
@RequestMapping("/client")
public class ClientOrderController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(ClientOrderController.class);

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Obsługuje wyświetlenie listy dla Konfiguracji zamówień
     *
     * @param userDetails informacje o zalogowanym użytkowniku
     * @param model       model danych przesyłany do widoku
     * @return nazwa szablonu widoku
     */
    @GetMapping
    public String showNotSendOrders(@AuthenticationPrincipal UserDetailsSecurity userDetails, Model model) {
        List<Task> taskList = camundaProcessService.getTasksForAssignee(userDetails.getUsername());
        Map<Task, OrderModel> taskOrderModelMap = new LinkedHashMap<>();
        for (Task task : taskList) {
            if (task.getProcessDefinitionId().contains(ProcessKey.CAR_DEALERSHIP_KLIENT.name())) {
                Object object = camundaProcessService.getVariable(task.getProcessInstanceId(), "orderData");
                if (object instanceof OrderModel && StringUtils.isBlank(((OrderModel) object).getOrderId())) {
                    taskOrderModelMap.put(task, (OrderModel) object);
                }
            }
        }

        model.addAttribute("assignedTasks", taskOrderModelMap.keySet());
        model.addAttribute("taskOrderModelMap", taskOrderModelMap);
        return "client_orders";
    }

    /**
     * Obsługuje kreator nowego zamówienia
     *
     * @param orderModel  obiekt DTO informacji o zamówieniu
     * @param userDetails informacje o zalogowanym użytkowniku
     * @param model       model danych przesyłany do widoku
     * @return nazwa szablonu widoku
     */
    @GetMapping("/new")
    public String newOrderConfigurationForm(@ModelAttribute OrderModel orderModel, @AuthenticationPrincipal UserDetailsSecurity userDetails, Model model) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("assignee", userDetails.getUsername());
        variables.put("orderData", orderModel);
        String processInstanceId = camundaProcessService.startProcess(ProcessKey.CAR_DEALERSHIP_KLIENT, variables);
        orderModel.setProcessId(processInstanceId);
        orderModel.setClient(userDetails.getUsername());

        List<Task> taskList = camundaProcessService.getTasksForProcessAndAssignee(processInstanceId, userDetails.getUsername());
        if (!taskList.isEmpty())
            orderModel.setTaskId(taskList.get(0).getId());

        camundaProcessService.setVariable(processInstanceId, "orderData", orderModel);

        fillModelWithAvailableResources(model);
        return "client_orderConfig";
    }

    /**
     * Obsługuje kreator edycji zamówienia (gdy jeszcze nie wysłane do salonu)
     *
     * @param processId  id procesu w camunda
     * @param userDetail informacje o zalogowanym użytkowniku
     * @param model      model danych przesyłany do widoku
     * @return nazwa szablonu widoku
     */
    @GetMapping("edit/{process_id}")
    public String showOrderConfigurationForm(@PathVariable("process_id") String processId,
                                             @AuthenticationPrincipal UserDetailsSecurity userDetail,
                                             RedirectAttributes redirect,
                                             Model model) {
        OrderModel orderModel = new OrderModel();
        String name = getKlientKonfiguracjaStepName(1);
        Object value = camundaProcessService.getVariable(processId, "orderData");
        if (value instanceof OrderModel)
            orderModel = (OrderModel) value;

        if (orderModel.getClient() != null
                && !userDetail.getUsername().equals(orderModel.getClient())) {
            redirect.addFlashAttribute("globalError", "Nie można uruchomić procesu (" + processId + ").");
            return "redirect:/home";
        }

        String newTaskID = camundaProcessService.restartProcessInstance(processId, userDetail.getUsername(), name);
        orderModel.setProcessId(processId);
        orderModel.setTaskId(newTaskID);
        orderModel.setClient(userDetail.getUsername());
        model.addAttribute("orderModel", orderModel);
        fillModelWithAvailableResources(model);
        return "client_orderConfig";
    }

    /**
     * Obsługuje usunięcie zamówienia (gdy jeszcze nie wysłane do salonu)
     *
     * @param processId  id procesu w camunda
     * @param redirect   obiekt informacji przekierowania
     * @param userDetail informacje o zalogowanym użytkownikuu
     * @return przekierowanie na stronę główną
     */
    @GetMapping("delete/{process_id}")
    public String deleteProcess(@PathVariable("process_id") String processId, RedirectAttributes redirect,
                                @AuthenticationPrincipal UserDetailsSecurity userDetail) {

        if (userDetail.getAuthorities().contains(RoleEnum.ROLE_ADMIN) ||
                camundaProcessService.getTasksForAssignee(userDetail.getUsername())
                        .stream()
                        .anyMatch(t -> t.getProcessInstanceId().equals(processId))) {

            String orderId = null;
            Object value = camundaProcessService.getVariable(processId, "orderData");
            if (value instanceof OrderModel && StringUtils.isNotBlank(((OrderModel) value).getOrderId())) {
                orderId = ((OrderModel) value).getOrderId();
            }
            if (StringUtils.isBlank(orderId))
                orderId = (String) camundaProcessService.getVariable(processId, "orderId");

            String reason = "Usunięte przez " + userDetail.getUsername();

            try {
                deleteProcessByVariableName(processId, "clientProcessId", reason);
                deleteProcessByVariableName(processId, "dealershipProcessId", reason);


                camundaProcessService.deleteProcess(processId, reason);
            } catch (Exception e) {
                redirect.addFlashAttribute("globalError", "Nie można usunąć procesu (" + processId + ").");
                return "redirect:/home";
            }

            if (StringUtils.isNotBlank(orderId))
                orderRepository.deleteById(Long.valueOf(orderId));

            redirect.addFlashAttribute("globalMessage", "Z powodzeniem usunięto proces (" + processId + ").");
        } else {
            redirect.addFlashAttribute("globalError", "Nie można usunąć procesu (" + processId + ").");
        }

        return "redirect:/home";
    }

    /**
     * Usuwanie procesu na podstawie zapisanych zmiennych Camunaa
     * @param processId id procesu Camunda
     * @param varName nazwa zmiennej Camunda
     * @param reason powód usunięcia procesu
     */
    private void deleteProcessByVariableName(String processId, String varName, String reason) {
        Object var = camundaProcessService.getVariable(processId, varName);
        if (var instanceof String
                && StringUtils.isNotBlank((String) var)) {

            camundaProcessService.deleteProcess((String) var, reason);
        }

    }

    /**
     * Obsługa złożenia zamówienia
     *
     * @param orderModel obiekt DTO informacji o zamówieniu
     * @param userDetail informacje o zalogowanym użytkowniku
     * @param redirect   obiekt informacji przekierowania
     * @return przekierowanie na listę zamówień
     */
    @PostMapping
    public String confirmOrderRegistration(@Valid OrderModel orderModel,
                                           @AuthenticationPrincipal UserDetailsSecurity userDetail,
                                           RedirectAttributes redirect) {
        Map<String, Object> variables = new HashMap<>();

        Order order = new Order();
        order.setOrderInfo(new OrderInfo(orderModel));
        order.setOrderStatusEnum(OrderStatusEnum.Registration);

        if (userDetail.getUsername().equals(orderModel.getClient())) {
            order.setOwner(userDetail.getUser());
        } else if (userDetail.getAuthorities().contains(RoleEnum.ROLE_ADMIN) || userDetail.getAuthorities().contains(RoleEnum.ROLE_DEALERSHIP)) {
            User user = userRepository.findByName(orderModel.getClient());
            order.setOwner(user);
        }

        if (order.getOwner() == null) {
            redirect.addFlashAttribute("globalError", "Nie można złożyć zamówienia. Niezgodność nazw lub uprawnień uzytkownika.");
            return "redirect:/client/orders";
        }

        order.setCreationDate(Instant.now().atZone(ZoneId.of("Europe/Warsaw")).toLocalDateTime());

        order.getOrderInfo().setProcessId(orderModel.getProcessId());

        order = orderRepository.save(order);

        order.setNumber(order.getId()
                + order.getOwner().getName().substring(0, 2)
                + order.getCreationDateAsString().substring(0, 10).replace("-", ""));
        orderRepository.save(order);

        orderRepository.flush();
        orderModel.setOrderId(String.valueOf(order.getId()));
        orderModel.setNumber(order.getNumber());

        variables.put("orderData", orderModel);
        variables.put("orderId", orderModel.getOrderId());

        camundaProcessService.setVariable(orderModel.getProcessId(), "orderData", orderModel);
        camundaProcessService.setVariable(orderModel.getProcessId(), "orderId", orderModel.getOrderId());
        camundaProcessService.setVariable(orderModel.getProcessId(), "clientProcessId", orderModel.getProcessId());
        camundaProcessService.setVariable(orderModel.getProcessId(), "client", orderModel.getClient());
        camundaProcessService.completeTask(orderModel.getTaskId(), variables);

        redirect.addFlashAttribute("globalMessage", "Pomyślnie ukończone zadanie  id " + orderModel.getTaskId() + ".");
        return "redirect:/client/orders";
    }

    /**
     * Obsługa kroku wstecz i zmian sekcji konfiguratora zamówień
     *
     * @param processId  id procesu w camunda
     * @param stepNum    numer kroku konfiguratora
     * @param orderModel obiekt DTO informacji o zamówieniu
     * @param userDetail informacje o zalogowanym użytkowniku
     * @return odpowiedź HTTP (200 lub 500)
     */
    @PostMapping("/change/{process_id}")
    public ResponseEntity<?> moveConfigLog(@PathVariable("process_id") String processId, @RequestParam("stepNum") long stepNum,
                                           @RequestBody OrderModel orderModel, @AuthenticationPrincipal UserDetailsSecurity userDetail) {
        String name = getKlientKonfiguracjaStepName(stepNum);
        String newTaskID;
        log.trace("[moveConfigLog] processId={}, stepNum={}, stepName={}", processId, stepNum, name);
        try {

            String username = null;
            if (userDetail.getUsername().equals(orderModel.getClient())) {
                username = userDetail.getUsername();
            } else if (userDetail.getAuthorities().contains(RoleEnum.ROLE_ADMIN) || userDetail.getAuthorities().contains(RoleEnum.ROLE_DEALERSHIP)) {
                User user = userRepository.findByName(orderModel.getClient());
                username = user != null ? user.getName() : null;
            }

            if (username == null) {
                return new ResponseEntity<>("Niezgodność użytkowników", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            newTaskID = camundaProcessService.restartProcessInstance(processId, username, name);

            orderModel.setTaskId(newTaskID);
            camundaProcessService.setVariable(processId, "orderData", orderModel);
        } catch (Exception ex) {
            log.error("[moveConfigLog] Exception:", ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(orderModel, HttpStatus.OK);
    }

    /**
     * Obsługa przycusku Dalej lub zatwierdzenia zadania
     *
     * @param taskId     id zadania Camunda
     * @param orderModel obiekt DTO informacji o zamówieniu
     * @param userDetail informacje o zalogowanym użytkowniku
     * @return odpowiedź HTTP (200 lub 500)
     */
    @PostMapping("/complete/{taskId}")
    public ResponseEntity<?> completeStep(@PathVariable("taskId") String taskId, @RequestBody OrderModel orderModel,
                                          @AuthenticationPrincipal UserDetailsSecurity userDetail) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("orderData", orderModel);
        camundaProcessService.completeTask(taskId, variables);

        String username = null;
        if (userDetail.getUsername().equals(orderModel.getClient())) {
            username = userDetail.getUsername();
        } else if (userDetail.getAuthorities().contains(RoleEnum.ROLE_ADMIN) || userDetail.getAuthorities().contains(RoleEnum.ROLE_DEALERSHIP)) {
            User user = userRepository.findByName(orderModel.getClient());
            username = user != null ? user.getName() : null;
        }

        if (username == null) {
            return new ResponseEntity<>("Niezgodność użytkowników", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        List<Task> taskList = camundaProcessService.getTasksForProcessAndAssignee(orderModel.getProcessId(), username);
        if (!taskList.isEmpty()) {
            orderModel.setTaskId(taskList.get(0).getId());
            camundaProcessService.setVariable(orderModel.getProcessId(), "orderData", orderModel);
        }
        return new ResponseEntity<>(orderModel, HttpStatus.OK);
    }

    /**
     * Obsługuje wyświetlanie listy zamówień dla klienta
     *
     * @param userDetail informacje o zalogowanym użytkowniku
     * @param active     czy tylko aktywne zamówienia (opcjonalnie)
     * @param model      model danych przesyłany do widoku
     * @return nazwa szablonu widoku
     */
    @GetMapping("/orders")
    public String viewOrdersList(@AuthenticationPrincipal UserDetailsSecurity userDetail,
                                 @RequestParam(required = false) Integer active,
                                 Model model) {

        List<Order> orders;
        if (active != null && active == 1) {
            orders = orderRepository.findAllByClientOrderStatusEnumInAndOwner_NameOrderByCreationDateDesc(ClientOrderStatusEnum.activeStatuses(), userDetail.getUsername());
        } else if (active != null && active == 0) {
            orders = orderRepository.findAllByClientOrderStatusEnumInAndOwner_NameOrderByCreationDateDesc(ClientOrderStatusEnum.notActiveStatuses(), userDetail.getUsername());
        } else {
            orders = orderRepository.findAllByOwner_NameOrderByCreationDateDesc(userDetail.getUsername());
        }

        model.addAttribute("urlPath", "/client/orders");
        model.addAttribute("urlActiveStatuses", "/client/orders?active=1");
        model.addAttribute("urlNotActiveStatuses", "/client/orders?active=0");
        model.addAttribute("orders", orders);
        model.addAttribute("viewLabel", "Złożone zamówienia");
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
        final String REDIRECT_URL = "redirect:/client/orders";
        Optional<Order> optional = orderRepository.findById(orderId);
        Order order = optional.orElse(null);
        if (order == null) {
            redirect.addFlashAttribute("globalError", "Nie udało się znaleźć zamówienia (" + orderId + ").");
            return REDIRECT_URL;
        }
        if (!userDetail.getUser().getId().equals(order.getOwner().getId())) {
            redirect.addFlashAttribute("globalError", "Brak uprawnień do wykonania operacji anulowania zamówienia.");
            return REDIRECT_URL;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("cancellationRequestCreator", "client");
        // Wyszukuje procesy po stronie salonu i wysyła wiadomość anulowania
        for (ProcessInstance processInstance : camundaProcessService.getProcessInstancesForOrderId(orderId+"", new String[]{"CAR_DEALERSHIP_SALON"}))
            camundaProcessService.createMessage("CancellationMessage", processInstance.getProcessInstanceId(), variables);

        // Wyszukuje procesy po stronie klienta i wysyła wiadomość anulowania
        // EDIT: Nie wyszukuje, bo subprocess po stronie klienta inicjuje wiadomość z salonu
        /*for (ProcessInstance processInstance : camundaProcessService.getProcessInstancesForOrderId(orderId+"", new String[]{"CAR_DEALERSHIP_KLIENT"}))
            camundaProcessService.createMessage("OrderRejectMessage", processInstance.getProcessInstanceId(), variables);*/

        //order.setClientOrderStatusEnum(ClientOrderStatusEnum.Cancelled);
        //orderRepository.save(order);

        return REDIRECT_URL;
    }

    /**
     * Pomocniczna metoda zwracajaca nazwę zadania w BPMN na podstaiwe nr kroku w Kreatorze konfiguracji zamówienia
     *
     * @param stepNum nr kroku
     * @return nazwa zadania BPMN
     */
    private String getKlientKonfiguracjaStepName(long stepNum) {
        String stepName = null;

        if (stepNum == 1) {
            stepName = "Klient_Konfiguracja_Model";
        } else if (stepNum == 2) {
            stepName = "Klient_Konfiguracja_Silnik";
        } else if (stepNum == 3) {
            stepName = "Klient_Konfiguracja_TypNadwozia";
        } else if (stepNum == 4) {
            stepName = "Klient_Konfiguracja_SkrzyniaBiegow";
        } else if (stepNum == 5) {
            stepName = "Klient_Konfiguracja_WyposazenieDodatkowe";
        } else if (stepNum == 6) {
            stepName = "Klient_ZlozenieZamowienia";
        }

        return stepName;
    }
}
