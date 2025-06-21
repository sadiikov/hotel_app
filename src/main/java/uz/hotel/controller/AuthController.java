package uz.hotel.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uz.hotel.entity.User;
import uz.hotel.entity.enums.UserRole;
import uz.hotel.exceptions.DataNotFoundException;
import uz.hotel.exceptions.OperationCannotBeDoneException;
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
            throw new OperationCannotBeDoneException("User already registered with this email: " + email);
        }

        double balanceValue;
        try {
            balanceValue = Double.parseDouble(balance);
        }catch (NumberFormatException e){
            throw new DataNotFoundException("Balance must be a valid number");
        }

        if(balanceValue<=0){
            throw new OperationCannotBeDoneException("balance should be positive");
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
