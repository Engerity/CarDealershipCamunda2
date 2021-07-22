package dealership.car.controller;

import dealership.car.service.UserSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController extends AbstractController {

    @Autowired
    private UserSecurityService userSecurityService;

    @GetMapping()
    public String root() {
        return home();
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("userList", userSecurityService.getAllNonPredefinedUsers());
        return "login";
    }

}
