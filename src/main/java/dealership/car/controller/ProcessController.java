package dealership.car.controller;

import dealership.car.camunda.model.CustomFormField;
import dealership.car.model.Order;
import dealership.car.model.UserDetailsSecurity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Kontroler dostępny w ramach operacji obsługi zadań
 */
@Controller
@RequestMapping("/process")
public class ProcessController extends AbstractController {

    /**
     * Wyświetlenie szczegółowych informacji na temat procesu/zadania
     * @param processId id procesu Camunda
     * @param userDetails informacje o zalogowanym użytkowniku
     * @param model model danych przesyłany do widoku
     * @return nazwa szablonu widoku
     */
    @GetMapping("/info/{processId}")
    public String infoForTask(@PathVariable("processId") String processId, @AuthenticationPrincipal UserDetailsSecurity userDetails, Model model) {
        Map<String, Object> vars =  camundaProcessService.getVariables(processId);

        List<CustomFormField> variablesInfo = new ArrayList<>();
        for (Map.Entry<String, Object> entry : vars.entrySet()) {
            CustomFormField tmp = new CustomFormField();
            tmp.setLabel(entry.getKey());
            tmp.setValue(entry.getValue());

            if (entry.getValue() instanceof String)
                tmp.setTypeName("string");
            else if (entry.getValue() instanceof Boolean)
                tmp.setTypeName("boolean");
            else if (entry.getValue() != null)
                tmp.setTypeName(entry.getValue().getClass().getTypeName());

            variablesInfo.add(tmp);
        }


        String orderId = (String) vars.get("orderId");
        Order order = null;
        if (StringUtils.isNotBlank(orderId))
            order = orderRepository.findById(Long.valueOf(orderId)).orElse(null);

        model.addAttribute("order", order);
        model.addAttribute("variablesInfo", variablesInfo);

        return "taskInfo";
    }
}
