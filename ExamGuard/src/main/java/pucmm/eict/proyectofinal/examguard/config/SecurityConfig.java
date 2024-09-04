package pucmm.eict.proyectofinal.examguard.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pucmm.eict.proyectofinal.examguard.model.enums.UserRole;
import pucmm.eict.proyectofinal.examguard.service.UserService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authRequest ->
                        authRequest
                                .requestMatchers("/error/**").permitAll()
                                .requestMatchers("assets/**", "css/**", "js/**").permitAll()
                                .requestMatchers("/").permitAll()
                                .requestMatchers("/login").permitAll()
                                .requestMatchers("/register").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin(login -> login.loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/folders")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .failureHandler((request, response, exception) -> response.sendRedirect("/login?error"))
                )
                .rememberMe(rememberMe -> rememberMe
                        .rememberMeParameter("remember-me")
                        .key("remember-me")
                        .tokenValiditySeconds(14 * 24 * 60 * 60)
                )
                .logout(logout -> logout
                        .permitAll()
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            request.getSession().invalidate();
                            response.sendRedirect("/login");
                        })
                        .deleteCookies("JSESSIONID")
                )
                .build();
    }
}
