package com.umc.pediupatrao.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.javers.core.Javers;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.PropertyChange;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.pediupatrao.entity.Pedido;

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
    public Map<String, Object> snapshot(@PathVariable String id,
            @RequestParam long commitId) {
        var q = QueryBuilder.byInstanceId(id, Pedido.class)
                .withCommitId(BigDecimal.valueOf(commitId))
                .build();

        CdoSnapshot snap = javers.findSnapshots(q).stream()
                .findFirst()
                .orElse(null);

        if (snap == null) {
            return null;
        }

        CommitMetadata meta = snap.getCommitMetadata();

        Map<String, Object> out = new HashMap<>();
        out.put("commitId", snap.getCommitId().valueAsNumber().toPlainString());
        out.put("author", meta == null ? "SISTEMA" : meta.getAuthor());
        out.put("date", meta == null ? "" : meta.getCommitDate().toString());
        out.put("entity", snap.getGlobalId().getTypeName());
        out.put("state", snap.getState().toString());
        return out;
    }

    private Map<String, Object> toDto(Change c) {
        CommitMetadata meta = c.getCommitMetadata().orElse(null);
        String propriedade = (c instanceof PropertyChange pc) ? pc.getPropertyName() : null;

        Map<String, Object> dto = new HashMap<>();
        dto.put("tipo", c.getClass().getSimpleName());
        dto.put("propriedade", propriedade == null ? "" : propriedade);
        dto.put("autor", meta == null ? "SISTEMA" : meta.getAuthor());
        dto.put("quando", meta == null ? "" : meta.getCommitDate().toString());
        dto.put("commitId", meta == null ? "" : meta.getId().valueAsNumber().toPlainString());
        return dto;
    }
}
