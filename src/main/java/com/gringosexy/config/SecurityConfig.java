package com.gringosexy.config;

import com.gringosexy.service.impl.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                // Static resources
                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/icons/**",
                    "/videos/**",
                    "/favicon.ico",
                    "/webjars/**"
                ).permitAll()
                // Public routes
                .requestMatchers(
                    "/",
                    "/api/cron/**",
                    "/preview/**",
                    "/login",
                    "/register",
                    "/pending-activation",
                    "/verify-email",
                    "/forgot-password",
                    "/reset-password"
                ).permitAll()
                // Admin & Super Admin routes
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                // Private user routes
                .requestMatchers(
                    "/dashboard/**",
                    "/profile/**",
                    "/content/**",
                    "/support/**"
                ).authenticated()
                // All others require authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("gringo-sexy-secret-key-2026-very-secure-token")
                .tokenValiditySeconds(86400 * 14) // 14 days
                .userDetailsService(userDetailsService)
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/errors/403")
            );

        return http.build();
    }
}
