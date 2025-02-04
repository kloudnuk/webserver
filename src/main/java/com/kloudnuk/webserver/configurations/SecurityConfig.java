package com.kloudnuk.webserver.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.kloudnuk.webserver.security.UserService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    static RoleHierarchy hierarchy() {
        String map = "ROLE_ADMIN > ROLE_MANAGER > ROLE_CONTRIBUTOR > ROLE_GUEST";
        var hierarchy = RoleHierarchyImpl.fromHierarchy(map);
        return hierarchy;
    }

    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler expressionHandler =
                new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        return expressionHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("api/v1/orgs/create", "/login/**").anonymous()
                        .requestMatchers("/*").hasRole("GUEST").anyRequest().authenticated())
                .formLogin(configurer -> configurer.loginPage("/login").loginProcessingUrl("/login")
                        .usernameParameter("username").passwordParameter("password")
                        .defaultSuccessUrl("/").permitAll())
                .csrf(csrf -> csrf.disable()).httpBasic(Customizer.withDefaults())
                .authenticationProvider(authprovider()).build();
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserService users() {
        return new UserService(encoder());
    }

    @Bean
    public DaoAuthenticationProvider authprovider() {
        var provider = new DaoAuthenticationProvider(encoder());
        provider.setUserDetailsService(users());
        return provider;
    }
}
