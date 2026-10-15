package com.umc.pediupatrao.repository;

import com.umc.pediupatrao.entity.Cliente;
import java.util.List;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
@JaversSpringDataAuditable // Captura alterações de cadastro (RF05)
public interface ClienteRepository extends MongoRepository<Cliente, String> {
    List<Cliente> findByTelefone(String telefone);
}
