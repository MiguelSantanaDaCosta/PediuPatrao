package com.umc.pediupatro.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;


@Getter
@Setter
public class PedidoRequest{
    @NotBlank(message = "O ID do cliente é obrigatorio")
    private String clienteID;
    @NotEmpty(message = "O pedido deve ter ao menos 1 item")
    @Valid
    private List<ItemPedidoDTO> itens;

}
