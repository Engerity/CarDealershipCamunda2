package dealership.car.controller;

import dealership.car.camunda.model.RegisterUserModel;
import dealership.car.model.IRole;
import dealership.car.model.RoleEnum;
import dealership.car.model.User;
import dealership.car.service.UserSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/register")
public class RegisterUserController extends AbstractController {

	@Autowired
	private UserSecurityService userSecurityService;

	@GetMapping()
	public String processes(@ModelAttribute RegisterUserModel registerUserModel) {
		registerUserModel.getRoles().add(RoleEnum.ROLE_CLIENT);
		return "registerUser";
	}

	@PostMapping("/user")
	public ModelAndView createUser(@Valid RegisterUserModel registerUserModel, BindingResult result, RedirectAttributes redirect) {
		if (result.hasErrors()) {
			return new ModelAndView("registerUser", "formErrors", result.getAllErrors());
		}

		if (userSecurityService.userExists(registerUserModel.getUser())) {
			// User already exists
			redirect.addFlashAttribute("globalMessage", "User " + registerUserModel.getUser() + " already exists.");
			return new ModelAndView("redirect:/register");
		}


		User user = new User();
		user.setName(registerUserModel.getUser());
		user.setPassword(registerUserModel.getPassword());

		for (IRole iRole: registerUserModel.getRoles())
			user.getRoles().add(RoleEnum.byIRole(iRole));

		if (user.getRoles().isEmpty())
			user.getRoles().add(RoleEnum.ROLE_CLIENT);

		userSecurityService.createUser(user);

		redirect.addFlashAttribute("globalMessage", "Successfully registered user " + registerUserModel.getUser() + ".");
		return new ModelAndView("redirect:/login");
	}
}
