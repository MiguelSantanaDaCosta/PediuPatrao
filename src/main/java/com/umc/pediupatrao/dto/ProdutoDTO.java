package com.umc.pediupatrao.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProdutoDTO {

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 120)
    private String nome;

    @NotBlank(message = "O tipo é obrigatório")
    @Size(max = 60)
    private String tipo;

    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser maior que zero")
    private Double preco;

    private Boolean ativo = true;
}
