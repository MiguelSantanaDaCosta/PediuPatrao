package com.umc.pediupatrao.repository;

import com.umc.pediupatrao.entity.Pedido;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
@JaversSpringDataAuditable // Intercepta automaticamente todos os save(), delete()
public interface PedidoRepository extends MongoRepository<Pedido, String> {
}
