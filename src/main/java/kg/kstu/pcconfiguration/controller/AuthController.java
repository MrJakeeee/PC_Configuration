package kg.kstu.pcconfiguration.controller;

import kg.kstu.pcconfiguration.model.AppUser;
import kg.kstu.pcconfiguration.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new AppUser());
        return "auth/register";
    }

    @PostMapping("/register")
    public String doRegister(@ModelAttribute AppUser user, Model model) {
        try {
            userService.register(user);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("user", user);
            model.addAttribute("error", ex.getMessage());
            return "auth/register";
        }
    }
}
