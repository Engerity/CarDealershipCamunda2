package dealership.car.controller;

import dealership.car.camunda.model.CompleteTasksModel;
import dealership.car.camunda.model.CustomFormField;
import dealership.car.model.Order;
import dealership.car.model.OrderModel;
import dealership.car.model.RoleEnum;
import dealership.car.model.UserDetailsSecurity;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.*;

@Controller
@RequestMapping("/tasks")
public class TasksController extends AbstractController {

    @GetMapping()
    public String tasks(@AuthenticationPrincipal UserDetailsSecurity userDetails,
                        @ModelAttribute CompleteTasksModel completeTaskModel, Model model) {
        List<Task> assignedTasks = camundaProcessService.getTasksForAssignee(userDetails.getUsername());

        List<? extends GrantedAuthority> roles = new ArrayList<>(userDetails.getAuthorities());

        List<String> groups = new ArrayList<>();
        roles.forEach(r -> groups.add(r.getAuthority()));
        Map<RoleEnum, List<Task>> taskInGroups = new LinkedHashMap<>();
        roles.forEach(r -> taskInGroups.put((RoleEnum) r, camundaProcessService.getTasksForCandidateGroupIn(Collections.singletonList(r.getAuthority()))));

        taskInGroups.values().forEach(list -> list.removeIf(assignedTasks::contains));

        Map<String, Order> tasksOrders = new HashMap<>();
        Set<Task> allTasks = new HashSet<>(assignedTasks);
        taskInGroups.forEach((key, value) -> allTasks.addAll(value));

        for (Task t : allTasks) {
            if (!tasksOrders.containsKey(t.getId())) {
                String orderId = (String) camundaProcessService.getVariable(t.getProcessInstanceId(), "orderId");
                if (StringUtils.isNotBlank(orderId)) {
                    Order order = orderRepository.findById(Long.valueOf(orderId)).orElse(null);
                    tasksOrders.put(t.getId(), order);
                }
            }
        }

        model.addAttribute("assignedTasks", assignedTasks);
        model.addAttribute("taskInGroups", taskInGroups);
        model.addAttribute("tasksOrders", tasksOrders);
        return "tasks";
    }

    @GetMapping("/open/{taskId}")
    public String openTask(@PathVariable("taskId") String taskId, @AuthenticationPrincipal UserDetailsSecurity userDetails, Model model) {
        TaskFormData formData = camundaProcessService.getTaskFormData(taskId);
        Task task = camundaProcessService.getTaskForId(taskId);

        String orderId = (String) camundaProcessService.getVariable(task.getProcessInstanceId(), "orderId");
        if (StringUtils.isBlank(orderId)) {
            Object orderData = camundaProcessService.getVariable(task.getProcessInstanceId(), "orderData");
            if (orderData instanceof OrderModel)
                orderId = ((OrderModel) orderData).getOrderId();
        }

        Order order = null;
        if (StringUtils.isNotBlank(orderId))
            order = orderRepository.findById(Long.valueOf(orderId)).orElse(null);

        VariableMap variableFormMap = camundaProcessService.getTaskFormVariables(taskId);
        CompleteTasksModel completeTaskModel = new CompleteTasksModel();
        completeTaskModel.setId(taskId);
        completeTaskModel.setName(task.getName());

        for (FormField ff : formData.getFormFields()) {
            completeTaskModel.getFormValues().add(new CustomFormField(ff.getId(), ff.getTypeName(), ff.getValue().getValue(), ff.getLabel(), ff.getProperties()));
        }

        model.addAttribute("order", order);
        model.addAttribute("viewLabel", task.getName());
        model.addAttribute("variableFormMap", variableFormMap);
        model.addAttribute("completeTaskModel", completeTaskModel);

        return formData.getFormKey();
    }

    @GetMapping("/info/{taskId}")
    public String infoForTask(@PathVariable("taskId") String taskId, @AuthenticationPrincipal UserDetailsSecurity userDetails, Model model) {
        TaskFormData formData = camundaProcessService.getTaskFormData(taskId);
        Task task = camundaProcessService.getTaskForId(taskId);

        String orderId = (String) camundaProcessService.getVariable(task.getProcessInstanceId(), "orderId");
        if (StringUtils.isBlank(orderId)) {
            Object orderData = camundaProcessService.getVariable(task.getProcessInstanceId(), "orderData");
            if (orderData instanceof OrderModel)
                orderId = ((OrderModel) orderData).getOrderId();
        }

        Order order = null;
        if (StringUtils.isNotBlank(orderId))
            order = orderRepository.findById(Long.valueOf(orderId)).orElse(null);

        VariableMap variableFormMap = camundaProcessService.getTaskFormVariables(taskId);
        List<CustomFormField> variablesInfo = new ArrayList<>();
        for (Map.Entry<String, Object> entry : variableFormMap.entrySet()) {
            CustomFormField tmp = new CustomFormField();
            tmp.setLabel(entry.getKey());
            tmp.setValue(entry.getValue());

            TypedValue typeOfVal = variableFormMap.asVariableContext().resolve(entry.getKey());
            if (typeOfVal instanceof ObjectValue)
                tmp.setTypeName(((ObjectValue) typeOfVal).getObjectTypeName());
            else
                tmp.setTypeName(typeOfVal.getType().getName());

            variablesInfo.add(tmp);
        }

        CompleteTasksModel completeTaskModel = new CompleteTasksModel();
        completeTaskModel.setId(taskId);
        completeTaskModel.setName(task.getName());

        for (FormField ff : formData.getFormFields()) {
            completeTaskModel.getFormValues().add(new CustomFormField(ff.getId(), ff.getTypeName(), ff.getValue().getValue(), ff.getLabel(), ff.getProperties()));
        }

        model.addAttribute("order", order);
        model.addAttribute("variablesInfo", variablesInfo);

        return "taskInfo";
    }

    @GetMapping("/assigne/{taskId}")
    public ModelAndView setTaskAssigne(@PathVariable("taskId") String taskId, @AuthenticationPrincipal UserDetailsSecurity userDetails, Model model) {
        camundaProcessService.setTaskAssignee(taskId, userDetails);
        return new ModelAndView("redirect:/tasks");
    }

    @PostMapping()
    public ModelAndView complete(@Valid CompleteTasksModel completeTaskModel, RedirectAttributes redirect) {
        List<CustomFormField> formValues = completeTaskModel.getFormValues();

        Map<String, Object> variables = new LinkedHashMap<>();
        for (CustomFormField customField : formValues) {
            String id = customField.getId();
            Object value = customField.getValue();
            if (value == null || value instanceof String) {

                if ("boolean".equals(customField.getTypeName())) {
                    variables.put(id, Boolean.valueOf((String)value));
                } else {
                    variables.put(id, value);
                }
            } else  {
                variables.put(id, value);
            }
        }

        if (variables.isEmpty())
            camundaProcessService.completeTask(completeTaskModel.getId());
        else
            camundaProcessService.completeTask(completeTaskModel.getId(), variables);

        redirect.addFlashAttribute("globalMessage", "Pomyślnie ukończone zadanie  id " + completeTaskModel.getId() + ".");
        return new ModelAndView("redirect:/tasks");
    }


    @GetMapping("/config/{taskId}")
    public ResponseEntity<?> getTaskConfig(@PathVariable("taskId") String taskId, @AuthenticationPrincipal UserDetailsSecurity userDetail) {
        Map<String, String> responseConfig = new HashMap<>();
        Task task;
        try {
            TaskFormData formData = camundaProcessService.getTaskFormData(taskId);
            task = camundaProcessService.getTaskForId(taskId);
            if (task != null) {
                responseConfig.put("isOpenAvailable", String.valueOf(isFormDataAvailable(formData)));
                responseConfig.put("isCompleteAvailable", String.valueOf(!isFormDataAvailable(formData)));
                responseConfig.put("isRemoveAvailable", String.valueOf(userDetail.getAuthorities().contains(RoleEnum.ROLE_ADMIN)));
                responseConfig.put("isInfoAvailable", "true");
            }
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(responseConfig, HttpStatus.OK);
    }

    private boolean isFormDataAvailable(TaskFormData formData) {
        return formData != null && formData.getFormKey() != null && formData.getFormFields() != null && !formData.getFormFields().isEmpty();
    }

}
