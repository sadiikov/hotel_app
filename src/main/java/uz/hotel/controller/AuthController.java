package uz.hotel.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uz.hotel.entity.User;
import uz.hotel.entity.enums.UserRole;
import uz.hotel.repository.UserDAO;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/sign-up")
    public String signUpGet() {
        return "sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpPost(@RequestParam("name") String name,
                             @RequestParam("email") String email,
                             @RequestParam("password") String password,
                             @RequestParam("balance") String balance,
                                 Model model){

        Optional<User> optionalUser = userDAO.getUserByEmailAndPassword(email, password);
        if (optionalUser.isPresent()){
            model.addAttribute("message", "User already registered with this email");
            return "error";
        }

        double balanceValue;
        try {
            balanceValue = Double.parseDouble(balance);
        }catch (NumberFormatException e){
            model.addAttribute("message", "Balance must be a valid number");
            return "error";
        }

        if(balanceValue<=0){
            model.addAttribute("message", "balance should be positive");
            return "error";
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setBalance(Double.parseDouble(balance));
        user.setRole(UserRole.USER);

        userDAO.saveUser(user);
        return "redirect:/sign-in";
    }

    @GetMapping("/sign-in")
    public String signInGet() {return "sign-in";}
}
