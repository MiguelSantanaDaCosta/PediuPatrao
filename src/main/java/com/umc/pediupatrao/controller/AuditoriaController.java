package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Pedido;
import org.javers.core.Javers;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.Change;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auditoria")
@PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
public class AuditoriaController {

    private final Javers javers;

    public AuditoriaController(Javers javers) {
        this.javers = javers;
    }

    /** Histórico completo (time-machine) de um pedido. */
    @GetMapping("/pedidos/{id}")
    public List<Map<String, Object>> historicoPedido(@PathVariable String id) {
        var query = QueryBuilder.byInstanceId(id, Pedido.class).build();
        return javers.findChanges(query).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /** Snapshot do pedido em um commit específico. */
    @GetMapping("/pedidos/{id}/snapshot")
    public Pedido snapshot(@PathVariable String id,
                           @RequestParam long commitId) {
        var q = QueryBuilder.byInstanceId(id, Pedido.class)
                            .withCommitId(commitId)
                            .build();
        return javers.findSnapshots(q).stream()
                .findFirst()
                .map(s -> (Pedido) s.getState())
                .orElse(null);
    }

    private Map<String, Object> toDto(Change c) {
        CommitMetadata meta = c.getCommitMetadata();
        return Map.of(
                "tipo", c.getClass().getSimpleName(),
                "propriedade", c.getPropertyName(),
                "autor", meta.getAuthor(),
                "quando", meta.getCommitDate().toString(),
                "commitId", meta.getId().valueAsNumber()
        );
    }
}
