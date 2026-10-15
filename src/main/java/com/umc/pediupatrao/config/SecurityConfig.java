package com.umc.pediupatrao.config;

import com.umc.pediupatrao.service.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.http.HttpMethod;
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;

    public SecurityConfig(UsuarioDetailsService usuarioDetailsService) {
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(usuarioDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/**")
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/dist/**", "/plugins/**",
                                 "/js/**", "/images/**", "/webjars/**").permitAll()

                // Administração
                .requestMatchers("/api/usuarios/**", "/usuarios/**").hasRole("ADMIN")
                .requestMatchers("/auditoria/**", "/api/auditoria/**")
                    .hasAnyRole("ADMIN", "GERENTE")

                // Pedidos — operações sensíveis restritas ao GERENTE
                .requestMatchers("/api/pedidos/*/desconto",
                                 "/api/pedidos/*/cancelar").hasRole("GERENTE")
                .requestMatchers("/api/pedidos/**", "/pedidos/**")
                    .hasAnyRole("ATENDENTE", "GERENTE", "ADMIN")

                // Produtos — leitura liberada para ATENDENTE; escrita só GERENTE/ADMIN
                .requestMatchers(HttpMethod.GET, "/api/produtos/**", "/produtos/**")
                    .hasAnyRole("ATENDENTE", "GERENTE", "ADMIN")
                .requestMatchers("/api/produtos/**", "/produtos/**")
                    .hasAnyRole("GERENTE", "ADMIN")

                // Clientes
                .requestMatchers("/api/clientes/**", "/clientes/**")
                    .hasAnyRole("ATENDENTE", "GERENTE", "ADMIN")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
                .successHandler(authenticationSuccessHandler()))
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll());

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> response.sendRedirect("/");
    }
}
