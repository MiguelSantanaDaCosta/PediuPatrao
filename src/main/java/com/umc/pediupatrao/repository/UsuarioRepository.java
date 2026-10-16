package com.umc.pediupatrao.repository;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.entity.enums.Role;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@JaversSpringDataAuditable
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByUsername(String username);

    long countByRole(Role role);
}
