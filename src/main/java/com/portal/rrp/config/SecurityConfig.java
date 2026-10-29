package com.portal.rrp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * Login + role rules.
 *
 * Roles:
 *   USER  - customer: creates requests, sees and edits (while REQUESTED) only their own.
 *   ADMIN - staff: sees all requests, moves them through the status workflow
 *           (review, approve / reject, item received, refund, close) and sees the dashboard.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // public pages (login page, health check for Jenkins/Docker, styles, error page)
                .requestMatchers(antMatcher("/login"), antMatcher("/error"),
                                 antMatcher("/health"), antMatcher("/css/**")).permitAll()
                // admin-only pages and actions
                .requestMatchers(antMatcher("/dashboard"), antMatcher("/h2-console/**")).hasRole("ADMIN")
                .requestMatchers(antMatcher(HttpMethod.POST, "/requests/*/advance"),
                                 antMatcher(HttpMethod.POST, "/requests/*/reject")).hasRole("ADMIN")
                // everything else needs a logged-in user (USER or ADMIN)
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", false)
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll())
            // the H2 console is a separate web app that cannot send CSRF tokens and uses frames
            .csrf(csrf -> csrf.ignoringRequestMatchers(antMatcher("/h2-console/**")))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder encoder,
            @Value("${rrp.security.user-password:user123}") String userPassword,
            @Value("${rrp.security.admin-password:admin123}") String adminPassword) {
        return new InMemoryUserDetailsManager(
                User.withUsername("user").password(encoder.encode(userPassword)).roles("USER").build(),
                User.withUsername("user2").password(encoder.encode(userPassword)).roles("USER").build(),
                User.withUsername("admin").password(encoder.encode(adminPassword)).roles("ADMIN").build());
    }
}
