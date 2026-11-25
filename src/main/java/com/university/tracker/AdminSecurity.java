package com.university.tracker;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class AdminSecurity {
    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    InMemoryUserDetailsManager admins(@Value("${ADMIN_USERNAME:}") String username,
                                     @Value("${ADMIN_PASSWORD:}") String password,
                                     BCryptPasswordEncoder encoder) {
        var users = new InMemoryUserDetailsManager();
        if (!username.isBlank() && !password.isBlank()) {
            users.createUser(User.withUsername(username).password(encoder.encode(password))
                    .roles("ADMIN").build());
        }
        return users;
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http) {
        return http.authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.GET, "/publications", "/publications/{id:[0-9]+}",
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                        .anyRequest().hasRole("ADMIN"))
                .formLogin(login -> login.loginPage("/login").defaultSuccessUrl("/publications").permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/publications"))
                .build();
    }
}
