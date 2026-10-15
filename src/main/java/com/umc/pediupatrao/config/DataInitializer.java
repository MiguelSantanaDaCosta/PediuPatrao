package com.umc.pediupatrao.config;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.entity.enums.Role;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {
            criarSeNaoExistir(repo, encoder, "admin",     "admin123",     Role.ADMIN);
            criarSeNaoExistir(repo, encoder, "gerente",   "gerente123",   Role.GERENTE);
            criarSeNaoExistir(repo, encoder, "atendente", "atendente123", Role.ATENDENTE);
        };
    }

    private void criarSeNaoExistir(UsuarioRepository repo, PasswordEncoder encoder,
                                   String username, String senha, Role role) {
        if (repo.findByUsername(username).isEmpty()) {
            Usuario u = new Usuario();
            u.setUsername(username);
            u.setPassword(encoder.encode(senha));
            u.setRole(role);
            repo.save(u);
            System.out.println("[DataInitializer] Criado: " + username + " / " + role);
        }
    }
}
