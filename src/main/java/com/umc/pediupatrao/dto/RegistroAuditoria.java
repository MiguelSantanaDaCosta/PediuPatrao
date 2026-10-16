package com.umc.pediupatrao.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.umc.pediupatrao.entity.AlteracaoCampo;

public record RegistroAuditoria(
        String commitId,
        LocalDateTime dataHora,
        String usuario,
        String perfil,
        String operacao,
        String entidade,
        String entidadeId,
        List<AlteracaoCampo> campos,
        String justificativa) {
}
