package uz.hotel.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import uz.hotel.repository.UserDAO;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserDAO userDAO;
    private final CustomSuccessHandler customSuccessHandler;
    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable());
        http
                .authorizeHttpRequests(req ->{
                    req
                            .requestMatchers("/","/auth/**")
                            .permitAll()
                            
                            .requestMatchers("/admin/**")
                            .hasRole("ADMIN")

                            .requestMatchers("/user/**")
                            .hasRole("USER")
                            
//
//                            .requestMatchers("/articleDelete")
//                            .hasAnyAuthority("DELETE_ARTICLE")
//
//                            .requestMatchers("/articleRead")
//                            .hasAnyAuthority("READ_ARTICLE")
//
//                            .requestMatchers("/articleCreate")
//                            .hasAnyAuthority("CREATE_ARTICLE")
//
//                            .requestMatchers("/articleUpdate/**")
//                            .hasAnyAuthority("UPDATE_ARTICLE")
//
//                            .requestMatchers("/checkup")
//                            .hasAnyRole("ADMIN","MODERATOR")

                            .anyRequest()
                            .authenticated();
                });
        http
                .formLogin(formLogin -> {
                    formLogin
                            .loginPage("/auth/sign-in")               // GET: the custom login page
                            .loginProcessingUrl("/auth/sign-in")      // POST: form action for login
                            .usernameParameter("email")               // name of email field
                            .passwordParameter("password")            // name of password field
                            .successHandler(customSuccessHandler)     // optional: for role-based redirects
                            .failureUrl("/auth/sign-in?error")        // redirect back on login failure
                            .permitAll();
                });


        http
                .userDetailsService(userDetailsService());


        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(){
       return  (username) -> userDAO.getUserByEmail(username).orElseThrow(()->new UsernameNotFoundException("User not found"));
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
