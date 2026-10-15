package com.umc.pediupatro.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ItemPedido{

    @NotBlank(message = "O ID do produto é obrigatorio")
    private String produtoId;

    @NotNull(message = "A quantidade é obrigatoria")
    @Min (value = 1, message "A quantidade minima é 1")
    private Integer quantidade;
}
