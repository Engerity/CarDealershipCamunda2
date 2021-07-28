package dealership.car.controller;

import dealership.car.camunda.model.RegisterUserModel;
import dealership.car.model.IRole;
import dealership.car.model.RoleEnum;
import dealership.car.model.User;
import dealership.car.service.UserSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.apache.commons.lang3.StringUtils;

import javax.validation.Valid;

/**
 * Kontroler dostępny w ramach operacji rejestracji użytkownika
 */
@Controller
@RequestMapping("/register")
public class RegisterUserController extends AbstractController {

	@Autowired
	private UserSecurityService userSecurityService;

	/**
	 * Wyświetlanie formularza do rejestracji
	 * @param registerUserModel obiekt DTO reprezentujacy dane rejestrowanego użytkownika
	 * @return nazwa szablonu widoku
	 */
	@GetMapping()
	public String processes(@ModelAttribute RegisterUserModel registerUserModel) {
		registerUserModel.getRoles().add(RoleEnum.ROLE_CLIENT);
		return "registerUser";
	}

	/**
	 * Obsługa rejestracji nowego użytkownika
	 * @param registerUserModel obiekt DTO reprezentujacy dane rejestrowanego użytkownika
	 * @param result obiekt wyniku powiązań, czyli wstępnej walidacji dancyh użytkownika
	 * @param redirect obiekt informacji przekierowania
	 * @return obiekt modelu danych i widoku do załadowania
	 */
	@PostMapping("/user")
	public ModelAndView createUser(@Valid RegisterUserModel registerUserModel, BindingResult result, RedirectAttributes redirect) {
		// Czy walidacja zgłasza błedy?
		if (result.hasErrors()) {
			StringBuilder errorMsg = new StringBuilder();
			for (DefaultMessageSourceResolvable objectError : result.getAllErrors()) {
				if (StringUtils.isNotBlank(errorMsg.toString()))
					errorMsg.append("\n");
				errorMsg.append(objectError.getCode())
						.append(": ")
						.append(objectError.getDefaultMessage())
						.append(".");
			}
			redirect.addFlashAttribute("globalMessage", errorMsg.toString());
			return new ModelAndView("redirect:/register");
		}

		// Czy taki użytkownik już istnieje?
		if (userSecurityService.userExists(registerUserModel.getUser())) {
			redirect.addFlashAttribute("globalMessage", "Użytkownik " + registerUserModel.getUser() + " już istnieje.");
			return new ModelAndView("redirect:/register");
		}

		// Tworzy nowego użytkownika
		User user = new User();
		user.setName(registerUserModel.getUser());
		user.setPassword(registerUserModel.getPassword());

		for (IRole iRole: registerUserModel.getRoles())
			user.getRoles().add(RoleEnum.byIRole(iRole));

		if (user.getRoles().isEmpty())
			user.getRoles().add(RoleEnum.ROLE_CLIENT);

		// Zapis do bazy danych
		userSecurityService.createUser(user);

		// Przekierowanie widoku
		redirect.addFlashAttribute("globalMessage", "Pomyślnie zarejestrowano użytkownika " + registerUserModel.getUser() + ".");
		return new ModelAndView("redirect:/login");
	}
}
