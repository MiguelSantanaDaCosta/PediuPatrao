package com.umc.pediupatrao.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DescontoRequest {

    @NotNull(message = "O percentual é obrigatório")
    @DecimalMin(value = "0.0", message = "O desconto mínimo é 0%")
    @DecimalMax(value = "20.0", message = "O desconto máximo é 20%")
    private Double percentual;
}
