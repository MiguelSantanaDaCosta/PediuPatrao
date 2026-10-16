package com.umc.pediupatrao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class CancelamentoRequest {

    @NotBlank(message = "A justificativa é obrigatória")
    @Size(min = 5, max = 500, message = "A justificativa deve ter de 5 a 500 caracteres")
    private String justificativa;
}
