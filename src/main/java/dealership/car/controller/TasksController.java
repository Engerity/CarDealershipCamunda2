package dealership.car.controller;

import dealership.car.camunda.model.CompleteTasksModel;
import dealership.car.camunda.model.CustomFormField;
import dealership.car.camunda.service.CamundaProcessService;
import dealership.car.config.ProcessKey;
import dealership.car.model.*;
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

    /**
     * Obsługuje żądanie HTTP o ilosć tasków/zadań przypisanych do użytownika i do jego grupy
     *
     * @param userDetails dane zalogowanego użytkownika
     * @return odpowiedź HTTP z ilością tasków/zddań przypisanych do użytownika i do jego grupy
     */
    @GetMapping("/count")
    public ResponseEntity<?> getMyTasksNo(@AuthenticationPrincipal UserDetailsSecurity userDetails) {
        // Taski przypisane do mnie
        List<Task> assignedTasks = getTaskAssignedAndNotOrderConfiguration(userDetails.getUsername());

        // Taski dla Konfiguracji zamówień
        List<Task> forOrderConfig = getTaskForOrderConfigurationOnly(userDetails.getUsername(), camundaProcessService);

        // Taski w grupach
        Map<RoleEnum, List<Task>> taskInGroups = getTaskInGroups(userDetails.getAuthorities(), assignedTasks);

        int countAssigneeToMe = assignedTasks.size();
        int countInGroups = taskInGroups.values().stream().mapToInt(List::size).sum();
        int countInOrderConfig = forOrderConfig.size();

        return new ResponseEntity<>(new Integer[]{countAssigneeToMe, countInGroups, countInOrderConfig}, HttpStatus.OK);
    }


    /**
     * Obsługuje wyświetlenie listy tasków/zadań
     *
     * @param userDetails       dane zalogowanego użytkownika
     * @param completeTaskModel informacje o poszczególnych zadaniach na liście
     * @param model             model danych przesyłany do widoku
     * @return nazwa szablonu widoku
     */
    @GetMapping()
    public String tasks(@AuthenticationPrincipal UserDetailsSecurity userDetails,
                        @ModelAttribute CompleteTasksModel completeTaskModel, Model model) {

        // Taski przypisane do mnie
        List<Task> assignedTasks = getTaskAssignedAndNotOrderConfiguration(userDetails.getUsername());

        // Taski w grupach
        Map<RoleEnum, List<Task>> taskInGroups = getTaskInGroups(userDetails.getAuthorities(), assignedTasks);

        Set<Task> allTasks = new HashSet<>(assignedTasks);
        taskInGroups.forEach((key, value) -> allTasks.addAll(value));

        Map<String, Order> tasksOrders = new HashMap<>();
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

    /**
     * Obsługa otwarcia obsługi/wykonania konkretnego zadania
     *
     * @param taskId      id zadania Camunda
     * @param userDetails dane zalogowanego użytkownika
     * @param model       model danych przesyłany do widoku
     * @return nazwa szablonu widoku
     */
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

    /**
     * Obsługa otwarcia informacji o konkretnym zadaniu
     *
     * @param taskId      id zadania Camunda
     * @param userDetails dane zalogowanego użytkownika
     * @param model       model danych przesyłany do widoku
     * @return nazwa szablonu widoku
     */
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

    /**
     * Obsługa przypisania zadania na siebie
     *
     * @param taskId      id zadania Camunda
     * @param userDetails dane zalogowanego użytkownika
     * @param model       model danych przesyłany do widoku
     * @return przekierowanie na listę zadań
     */
    @GetMapping("/assigne/{taskId}")
    public String setTaskAssigne(@PathVariable("taskId") String taskId, @AuthenticationPrincipal UserDetailsSecurity userDetails, Model model) {
        camundaProcessService.setTaskAssignee(taskId, userDetails);
        return "redirect:/tasks";
    }

    /**
     * Obsługa zatwierdzenia konkretnego zadania na liście zadań
     *
     * @param completeTaskModel dane obsługiwanego zadania
     * @param redirect          obiekt informacji przekierowania
     * @return przekierowanie na listę zadań
     */
    @PostMapping()
    public String complete(@Valid CompleteTasksModel completeTaskModel, RedirectAttributes redirect) {
        List<CustomFormField> formValues = completeTaskModel.getFormValues();

        Map<String, Object> variables = new LinkedHashMap<>();
        for (CustomFormField customField : formValues) {
            String id = customField.getId();
            Object value = customField.getValue();
            if (value == null || value instanceof String) {

                if ("boolean".equals(customField.getTypeName())) {
                    variables.put(id, Boolean.valueOf((String) value));
                } else {
                    variables.put(id, value);
                }
            } else {
                variables.put(id, value);
            }
        }

        if (variables.isEmpty())
            camundaProcessService.completeTask(completeTaskModel.getId());
        else
            camundaProcessService.completeTask(completeTaskModel.getId(), variables);

        redirect.addFlashAttribute("globalMessage", "Pomyślnie ukończone zadanie  id " + completeTaskModel.getId() + ".");
        return "redirect:/tasks";
    }

    /**
     * Obsługa ządania HTTP o informacje nt. dostępności/konfiguracji przycisków na liscie zadań
     *
     * @param taskId     id zadania Camunda
     * @param userDetail dane zalogowanego użytkownika
     * @return odpowiedź HTTP z konfiguracją przycisków
     */
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

    /**
     * Pomocnicza metoda sprawdzająca, czy dane formularza BPMN zawierają jakieś pola
     *
     * @param formData dane formularza BPMN
     * @return true, jeśli formularz BPMN zawiera pola
     */
    private boolean isFormDataAvailable(TaskFormData formData) {
        return formData != null && formData.getFormKey() != null && formData.getFormFields() != null && !formData.getFormFields().isEmpty();
    }

    /**
     * Pomocnicza metoda zwracająca mapę ról/uprawnień z odpowiadajacymi im zadaniami oczekującymi
     *
     * @param authorities   kolekcja uprawnień
     * @param assignedTasks lista już przypisanych zadań do użytkownika (do wykluczenia w wyniku)
     * @return mapa ról/uprawnień z odpowiadajacymi im zadaniami oczekującymi
     */
    private Map<RoleEnum, List<Task>> getTaskInGroups(Collection<? extends GrantedAuthority> authorities, List<Task> assignedTasks) {
        List<? extends GrantedAuthority> roles = new ArrayList<>(authorities);

        // Taski w grupach
        Map<RoleEnum, List<Task>> taskInGroups = new LinkedHashMap<>();

        //Klient nie widzi taskó innych klientów
        if (roles.size() == 1 && roles.contains(RoleEnum.ROLE_CLIENT))
            return taskInGroups;

        roles.forEach(r -> taskInGroups.put((RoleEnum) r, camundaProcessService.getTasksForCandidateGroupIn(Collections.singletonList(r.getAuthority()))));
        // ... ale nie przypisane do mnie
        taskInGroups.values().forEach(list -> list.removeIf(assignedTasks::contains));

        return taskInGroups;
    }

    /**
     * Pomocnicza metoda zwracajaca listę zadań przypisanych do użytkownika (poza zadaniami z Konfiguratora zamówień)
     *
     * @param username nazwa zalogowanego użytkownika
     * @return listę zadań przypisanych do użytkownika (poza zadaniami z Konfiguratora zamówień)
     */
    private List<Task> getTaskAssignedAndNotOrderConfiguration(String username) {
        List<Task> assignedTasks = camundaProcessService.getTasksForAssignee(username);

        // Usuwam z listy zadań te dla "Konfiguratora zamówień"
        Iterator<Task> iterator = assignedTasks.iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            if (task.getProcessDefinitionId().contains(ProcessKey.CAR_DEALERSHIP_KLIENT.name())) {
                Object object = camundaProcessService.getVariable(task.getProcessInstanceId(), "orderData");
                if (object instanceof OrderModel && StringUtils.isBlank(((OrderModel) object).getOrderId())) {
                    iterator.remove();
                }
            }
        }

        return assignedTasks;
    }

    /**
     * Pomocnicza metoda zwracajaca listę zadań przypisanych do użytkownika ale tylko dla Konfiguratora zamówień
     *
     * @param username nazwa zalogowanego użytkownika
     * @return listę zadań przypisanych do użytkownika ale tylko dla Konfiguratora zamówień
     */
    public static List<Task> getTaskForOrderConfigurationOnly(String username, CamundaProcessService camundaProcessService) {
        List<Task> result = new ArrayList<>();

        for (Task task : camundaProcessService.getTasksForAssignee(username)) {
            if (task.getProcessDefinitionId().contains(ProcessKey.CAR_DEALERSHIP_KLIENT.name())) {
                Object object = camundaProcessService.getVariable(task.getProcessInstanceId(), "orderData");
                if (object instanceof OrderModel && StringUtils.isBlank(((OrderModel) object).getOrderId())) {
                    result.add(task);
                }
            }
        }

        return result;
    }
}
