package dealership.car.controller;

import dealership.car.config.ProcessKey;
import dealership.car.model.*;
import dealership.car.repository.OrderRepository;
import dealership.car.service.AvailableResourcesService;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/client")
public class ClientOrderController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(ClientOrderController.class);

    @Autowired
    private AvailableResourcesService availableResourcesService;

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping
    public String showExistingOrders(@AuthenticationPrincipal UserDetailsSecurity userDetails, Model model) {
        List<Task> taskList = camundaProcessService.getTasksForAssignee(userDetails.getUsername());
        Map<Task, OrderModel> taskOrderModelMap =new LinkedHashMap<>();
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

    @GetMapping("edit/{process_id}")
    public String showOrderConfigurationForm(@PathVariable("process_id") String processId,
                                             @AuthenticationPrincipal UserDetailsSecurity userDetail,
                                             Model model) {
        OrderModel orderModel = new OrderModel();
        String name = getKlientKonfiguracjaStepName(1);
        Object value = camundaProcessService.getVariable(processId, "orderData");
        if (value instanceof OrderModel)
            orderModel = (OrderModel) value;
        String newTaskID = camundaProcessService.restartProcessInstance(processId, userDetail.getUsername(), name);
        orderModel.setProcessId(processId);
        orderModel.setTaskId(newTaskID);
        orderModel.setClient(userDetail.getUsername());
        model.addAttribute("orderModel", orderModel);
        fillModelWithAvailableResources(model);
        return "client_orderConfig";
    }

    @GetMapping("delete/{process_id}")
    public ModelAndView deleteProcess(@PathVariable("process_id") String processId, RedirectAttributes redirect,
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

            camundaProcessService.deleteProcess(processId, "Usunięte przez " + userDetail.getUsername());
            if (StringUtils.isNotBlank(orderId))
                orderRepository.delete(Long.valueOf(orderId));

            redirect.addFlashAttribute("globalMessage", "Successfully deleted process id " + processId + ".");
        } else {
            redirect.addFlashAttribute("globalMessage", "Failed to delete process id " + processId + ".");
        }

        return new ModelAndView("redirect:/home");
    }

    @PostMapping
    public ModelAndView confirmOrderRegistration(@Valid OrderModel orderModel,
                                                 @AuthenticationPrincipal UserDetailsSecurity userDetail,
                                                 RedirectAttributes redirect) {
        Map<String, Object> variables = new HashMap<>();

        Order order = new Order();
        order.setOrderInfo(new OrderInfo(orderModel));
        order.setOrderStatusEnum(OrderStatusEnum.Registration);
        order.setOwner(userDetail.getUser());
        order.setCreationDate(LocalDateTime.now());
        order.getOrderInfo().setProcessId(orderModel.getProcessId());

        order = orderRepository.save(order);
        orderRepository.flush();
        orderModel.setOrderId(String.valueOf(order.getId()));

        variables.put("orderData", orderModel);
        variables.put("orderId", orderModel.getOrderId());

        camundaProcessService.setVariable(orderModel.getProcessId(), "orderData", orderModel);
        camundaProcessService.setVariable(orderModel.getProcessId(), "orderId", orderModel.getOrderId());
        camundaProcessService.setVariable(orderModel.getProcessId(), "clientProcessId", orderModel.getProcessId());
        camundaProcessService.completeTask(orderModel.getTaskId(), variables);

        redirect.addFlashAttribute("globalMessage", "Successfully completed task id " + orderModel.getTaskId() + ".");
        return new ModelAndView("redirect:/client");
    }

    @PostMapping("/change/{process_id}")
    public ResponseEntity<?> moveConfigLog(@PathVariable("process_id") String processId, @RequestParam("stepNum") long stepNum,
                                           @RequestBody OrderModel orderModel, @AuthenticationPrincipal UserDetailsSecurity userDetail) {
        String name = getKlientKonfiguracjaStepName(stepNum);
        String newTaskID;
        log.trace("[moveConfigLog] processId={}, stepNum={}, stepName={}", processId, stepNum, name);
        try {
            newTaskID = camundaProcessService.restartProcessInstance(processId, userDetail.getUsername(), name);
            orderModel.setTaskId(newTaskID);
            camundaProcessService.setVariable(processId, "orderData", orderModel);
        } catch (Exception ex) {
            log.error("[moveConfigLog] Exception:", ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(orderModel, HttpStatus.OK);
    }

    @PostMapping("/complete/{taskId}")
    public ResponseEntity<?> completeStep(@PathVariable("taskId") String taskId, @RequestBody OrderModel orderModel,
                                      @AuthenticationPrincipal UserDetailsSecurity userDetail) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("orderData", orderModel);
        camundaProcessService.completeTask(taskId, variables);
        List<Task> taskList = camundaProcessService.getTasksForProcessAndAssignee(orderModel.getProcessId(), userDetail.getUsername());
        if (!taskList.isEmpty()) {
            orderModel.setTaskId(taskList.get(0).getId());
            camundaProcessService.setVariable(orderModel.getProcessId(), "orderData", orderModel);
        }
        return new ResponseEntity<>(orderModel,HttpStatus.OK);
    }

    @GetMapping("/orders")
    public String viewOrdersList(@AuthenticationPrincipal UserDetailsSecurity userDetail,
                                 @RequestParam(required = false) List<String> orderStatus,
                                 Model model) {

        List<OrderStatusEnum> orderStatusEnum = null;
        if (orderStatus != null && !orderStatus.isEmpty()) {
            orderStatusEnum = new ArrayList<>();

            for (String val : orderStatus) {
                OrderStatusEnum tmp = OrderStatusEnum.valueOfString(val);
                if (tmp != null)
                    orderStatusEnum.add(tmp);
            }
        }

        List<Order> orders;
        if (orderStatusEnum != null) {
            orders = orderRepository.findAllByOrderStatusEnumInAndOwner_Name(orderStatusEnum, userDetail.getUsername());
        } else {
            orders = orderRepository.findAllByOwner_Name(userDetail.getUsername());
        }

        model.addAttribute("urlPath", "/client/orders");
        model.addAttribute("urlActiveStatuses", "/client/orders?orderStatus=" + OrderStatusEnum.activeStatusesAsString());
        model.addAttribute("urlNotActiveStatuses", "/client/orders?orderStatus=" + OrderStatusEnum.notActiveStatusesAsString());
        model.addAttribute("orders", orders);
        model.addAttribute("viewLabel", "Złożone zamówienia");
        return "ordersList";
    }

    private void fillModelWithAvailableResources(Model model) {
        model.addAttribute("carModels", availableResourcesService.getAvailableModels());
        model.addAttribute("carEngines", availableResourcesService.getAvailableEngines());
        model.addAttribute("carBodyTypes", availableResourcesService.getAvailableBodyTypes());
        model.addAttribute("carTransmissionTypes", availableResourcesService.getAvailableTransmissionTypes());
        model.addAttribute("carAdditionalEquipments", availableResourcesService.getAvailableAdditionalEquipments());
    }

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
