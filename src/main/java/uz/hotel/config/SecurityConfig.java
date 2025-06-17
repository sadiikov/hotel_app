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
import uz.hotel.dao.UserDAO;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    final UserDAO userDAO;
    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable());
        http
                .authorizeHttpRequests(req ->{
                    req
                            .requestMatchers("/","/auth/**")
                            .permitAll()

                            .requestMatchers("/api/admin/info/private")
                            .hasAnyAuthority("READ_PRIVATE_DATA")

                            .requestMatchers("/api/admin/**")
                            .hasRole("ADMIN")
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
                            .loginPage("/auth/sign-in")
                            .usernameParameter("email")
                            .passwordParameter("password")
                            .defaultSuccessUrl("/cabinet");
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
