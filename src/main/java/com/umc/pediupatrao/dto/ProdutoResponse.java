package com.umc.pediupatrao.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProdutoResponse {
    private String id;
    private String nome;
    private String tipo;
    private Double preco;
    private Boolean ativo;
}
