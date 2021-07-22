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

@Controller
@RequestMapping("/orders")
public class DealershipOrderController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(DealershipOrderController.class);

    @GetMapping()
    public String viewOrdersList(@AuthenticationPrincipal UserDetailsSecurity userDetail,
                                 @RequestParam(required = false) List<String> orderStatus, Model model) {

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
            orders = orderRepository.findAllByOrderStatusEnumIn(orderStatusEnum);
        } else {
           orders = orderRepository.findAll();
        }

        model.addAttribute("urlPath", "/orders");
        model.addAttribute("urlActiveStatuses", "/orders?orderStatus=" + OrderStatusEnum.activeStatusesAsString());
        model.addAttribute("urlNotActiveStatuses", "/orders?orderStatus=" + OrderStatusEnum.notActiveStatusesAsString());
        model.addAttribute("orders", orders);
        model.addAttribute("viewLabel", "Zamówienia");
        return "ordersList";
    }
}
