package com.umc.pediupatrao.entity;

import com.umc.pediupatrao.entity.enums.TipoOperacao;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "auditoria")
@Getter
@Setter
@NoArgsConstructor
public class Auditoria {

    @Id
    private String id;

    private String usuario;
    private String perfil;
    private LocalDateTime dataHora;
    private TipoOperacao tipoOperacao;
    private String entidade;
    private String entidadeId;
    private List<AlteracaoCampo> campos = new ArrayList<>();
    private String justificativa;
}
