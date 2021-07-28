package dealership.car.controller;

import dealership.car.model.Order;
import dealership.car.model.OrderStatusEnum;
import dealership.car.model.UserDetailsSecurity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

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
}
