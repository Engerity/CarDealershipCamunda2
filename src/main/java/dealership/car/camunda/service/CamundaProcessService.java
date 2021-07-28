package dealership.car.camunda.service;

import dealership.car.config.ProcessKey;
import dealership.car.model.IUser;
import dealership.car.model.RoleEnum;
import dealership.car.model.UserDetailsSecurity;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.runtime.*;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.variable.VariableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Serwis obsługujący procesy i zadania w silniku Camunda.
 */
@Service
public class CamundaProcessService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private FormService formService;


    public void initialGroupConfig(Iterable<? extends IUser> iterable) {
        List<IUser> users = new ArrayList<>();
        iterable.forEach(users::add);

        for (RoleEnum roleEnum : RoleEnum.values()) {
            String name = roleEnum.getValue();
            List<Group> list = identityService.createGroupQuery().groupName(name).list();
            Group group;
            if (list == null || list.isEmpty()) {
                list = identityService.createGroupQuery().groupId(name).list();
                if (list == null || list.isEmpty()) {
                    group = identityService.newGroup(StringUtils.removeStart(name, "ROLE_"));
                    if (group.getId().equalsIgnoreCase("FACTORY_WORKER"))
                        group.setId("FACTORYWORKER");

                    group.setName(name);
                    group.setType("WEBAPP");
                    identityService.saveGroup(group);
                } else {
                    group = list.get(0);
                }
            } else {
                group = list.get(0);
            }

            Predicate<? super IUser> hasSameRoleName =
                    u -> u.getRoles().stream().anyMatch(r -> r.getValue().equals(roleEnum.getValue()));

            List<? extends IUser> candidateUsers = users.stream()
                    .filter(hasSameRoleName)
                    .collect(Collectors.toList());

            for (IUser user : candidateUsers) {
                List<User> tmpUsr = identityService.createUserQuery().userId(user.getName()).list();
                User cmdUser;
                if (tmpUsr == null || tmpUsr.isEmpty()) {
                    cmdUser = identityService.newUser(user.getName());
                    identityService.saveUser(cmdUser);
                } else {
                    cmdUser = tmpUsr.get(0);
                }

                identityService.deleteMembership(cmdUser.getId(), group.getId());
                identityService.createMembership(cmdUser.getId(), group.getId());
            }
        }

    }

    /**
     * Rozpoczyna proces w systemie.
     *
     * @param processKey klucz/ID procesu
     * @return ID instancji procesu
     */
    public String startProcess(ProcessKey processKey) {
        return startProcess(processKey, null);
    }

    /**
     * Rozpoczyna proces w systemie.
     *
     * @param processKey klucz/ID procesu
     * @param variables  mapa dodatkowych zmiennych
     * @return ID instancji procesu
     */
    public String startProcess(ProcessKey processKey, Map<String, Object> variables) {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processKey.name(), variables);
        return processInstance.getId();
    }

    /**
     * Usuwa proces w systemie
     *
     * @param processId ID instancji procesu
     * @param reason    powód usunięcia
     */
    public void deleteProcess(String processId, String reason) {
        runtimeService.deleteProcessInstance(processId, reason);
    }

    /**
     * Zwraca listę zadań w systemie.
     *
     * @return lista zadań
     */
    public List<Task> getTasks() {
        return taskService.createTaskQuery().list();
    }

    /**
     * Zwraca zadanie w systemie na podstawie ID.
     *
     * @param taskId ID zadania
     * @return zadanie w systemie
     */
    public Task getTaskForId(String taskId) {
        return taskService.createTaskQuery().taskId(taskId).singleResult();
    }

    /**
     * Zwraca listę zadań w systemie przypisanych do użytkownika.
     *
     * @param assignee nazwa użytkownika
     * @return lista zadań w systemie przypisanych do użytkownika
     */
    public List<Task> getTasksForAssignee(String assignee) {
        return taskService.createTaskQuery().taskAssignee(assignee).list();
    }

    /**
     * Zwraca listę zadań w systemie przypisanych do docelowej grupy.
     *
     * @param candidateGroup lista grup
     * @return lista zadań w systemie przypisanych do docelowej grupy
     */
    public List<Task> getTasksForCandidateGroupIn(List<String> candidateGroup) {
        return taskService.createTaskQuery().taskCandidateGroupIn(candidateGroup).includeAssignedTasks().list();
    }

    /**
     * Zwraca listę zadań w systemie przypisanych do użytkownika i procesu.
     *
     * @param processId ID instancji procesu
     * @param assignee  nazwa użytkownika
     * @return lista zadań w systemie przypisanych do użytkownika
     */
    public List<Task> getTasksForProcessAndAssignee(String processId, String assignee) {
        return taskService.createTaskQuery().processInstanceId(processId).taskAssignee(assignee).list();
    }

    /**
     * Zwraca listę zadań o podanej nazwie.
     *
     * @param name nazwa zadania
     * @return listę zadań o podanej nazwie
     */
    public List<Task> getTasksNameLike(String name) {
        return getTasksNameLike(name, null);
    }

    /**
     * Zwraca listę zadań o podanej nazwie w konkretnej instancji procesu.
     *
     * @param name      nazwa zadania
     * @param processId ID instancji procesu
     * @return listę zadań o podanej nazwie w konkretnej instancji procesu
     */
    public List<Task> getTasksNameLike(String name, String processId) {
        if (StringUtils.isBlank(name))
            return getTasks();
        if (!(StringUtils.startsWith(name, "%") || StringUtils.endsWith(name, "%")))
            name = "%" + name + "%";

        TaskQuery query = taskService.createTaskQuery();
        if (StringUtils.isNotBlank(name))
            query.processInstanceId(processId);

        return query.taskNameLike(name).list();
    }

    /**
     * Realizacja zadania o podanym ID.
     *
     * @param taskId ID zadania
     */
    public void completeTask(String taskId) {
        completeTask(taskId, null);
    }

    /**
     * Realizacja zadania o podanym ID z mapą zmiennych.
     *
     * @param taskId    ID zadania
     * @param variables mapa zmiennych
     */
    public void completeTask(String taskId, Map<String, Object> variables) {
        taskService.complete(taskId, variables);
    }

    /**
     * Realizacja zadania o podanym ID.
     *
     * @param taskId ID zadania
     */
    public void addCandidateGroupTask(String taskId, String groupId) {
        taskService.addCandidateGroup(taskId, groupId);
    }

    /**
     * Zmienia użytkownika przypisanego do zadania
     *
     * @param taskId ID zadania
     * @param userDetails dane zalogowanego użytkownika
     */
    public void setTaskAssignee(String taskId, UserDetailsSecurity userDetails) {
        taskService.setAssignee(taskId, userDetails.getUsername());
        Task task = getTaskForId(taskId);
        if (task != null) {
            setVariable(task.getProcessInstanceId(), "assignee", userDetails.getUsername());

            if (task.getProcessDefinitionId().contains("CAR_DEALERSHIP_FACTORY")) {
                setVariable("factoryWorker", "assignee", userDetails.getUsername());
            } else if (task.getProcessDefinitionId().contains("CAR_DEALERSHIP_SALON")) {
                setVariable("dealership", "assignee", userDetails.getUsername());
            } else if (task.getProcessDefinitionId().contains("CAR_DEALERSHIP_KLIENT")) {
                setVariable("client", "assignee", userDetails.getUsername());
            }
        }
    }

    /**
     * Realizuje restartowanie procesu.
     *
     * @param processId   ID instancji procesu
     * @param startBefore zadania do wycofania
     * @return ID nowego zadania
     */
    public String restartProcessInstance(String processId, String userName, String... startBefore) {
        ProcessInstanceModificationBuilder builder = runtimeService.createProcessInstanceModification(processId);
        List<Task> processList = taskService.createTaskQuery().processInstanceId(processId).list();
        Set<String> taskDefinitionKeys = new HashSet<>();

        for (Task t : processList)
            taskDefinitionKeys.add(t.getTaskDefinitionKey());

        for (String key : taskDefinitionKeys)
            builder.cancelAllForActivity(key);

        for (String name : startBefore)
            builder.startBeforeActivity(name);

        builder.execute();

        List<Task> tasks = getTasksForProcessAndAssignee(processId, userName);
        if (!tasks.isEmpty())
            return tasks.get(0).getId();
        return null;
    }

    /**
     * Ustawia wartość zmiennej w procesie BPMN
     *
     * @param processId ID instancji procesu
     * @param key       klucz/nazwa zmiennej
     * @param value     wartość zmiennej
     */
    public void setVariable(String processId, String key, Object value) {
        List<Execution> processInstances = runtimeService.createExecutionQuery().processInstanceId(processId).list();
        processInstances.forEach(execution -> runtimeService.setVariable(execution.getId(), key, value));
    }

    /**
     * Zwraca wartość zmiennej zapisanej w procesie BPMN
     *
     * @param processId ID instancji procesu
     * @param key       klucz/nazwa zmiennej
     * @return wartość zmiennej
     */
    public Object getVariable(String processId, String key) {
        List<Execution> processInstances = runtimeService.createExecutionQuery().processInstanceId(processId).list();
        if (!processInstances.isEmpty())
            return runtimeService.getVariable(processInstances.get(0).getId(), key);

        return null;
    }

    /**
     * Zwraca zmienne zapisane w aktywnym procesie BPMN lub w historycznym procesie gdy został już zakończony.
     *
     * @param processId ID instancji procesu
     * @return mapa zmiennych
     */
    public Map<String, Object> getVariables(String processId) {
        List<Execution> processInstances = runtimeService.createExecutionQuery().processInstanceId(processId).list();
        if (!processInstances.isEmpty()) {
            return runtimeService.getVariables(processId);

        } else {
            List<HistoricVariableInstance> histVarInstances = null;
            List<HistoricProcessInstance> histProcessInstances = historyService.createHistoricProcessInstanceQuery().processInstanceId(processId).list();
            if (!histProcessInstances.isEmpty())
                histVarInstances = historyService.createHistoricVariableInstanceQuery().processInstanceId(processId).list();

            if (histVarInstances != null) {
                Map<String, Object> vars = new LinkedHashMap<>();
                for (HistoricVariableInstance h : histVarInstances)
                    vars.put(h.getName(), h.getValue());

                return vars;
            }
        }

        return null;
    }

    /**
     * Odebranie wiadomości
     *
     * @param eventName   nazwa zdarzenia
     * @param executionId ID wykonania
     */
    public void receiveMessage(String eventName, String executionId) {
        runtimeService.messageEventReceived(eventName, executionId);
    }

    /**
     * Tworzy nową wiadomość
     *
     * @param messageName nazwa wiadomości
     * @param businessKey klucz biznesowy procesu
     * @param variables   mapa zmiennych
     * @return rezultat z utworzenia wiadomości
     */
    public MessageCorrelationResult createMessage(String messageName, String businessKey, Map<String, Object> variables) {
        MessageCorrelationBuilder builder = runtimeService.createMessageCorrelation(messageName)
                .processInstanceBusinessKey(businessKey);

        if (variables != null && !variables.isEmpty())
            builder.setVariables(variables);

        return builder.correlateWithResult();
    }

    /**
     * Zwraca informacje o formularzu przypisane do danego zadania.
     *
     * @param taskId ID zadania
     * @return informacje o formularzu
     */
    public TaskFormData getTaskFormData(String taskId) {
        return formService.getTaskFormData(taskId);
    }

    /**
     * Zwraca listę wszystkich zmiennych do renderowania formularza zadania.
     *
     * @param taskId ID zadania
     * @return lista wszystkich zmiennyc
     */
    public VariableMap getTaskFormVariables(String taskId) {
        return formService.getTaskFormVariables(taskId);
    }
}
