package com.umc.pediupatrao.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ItemPedido{

    private String produtoId;
    private String nomeProduto;
    private int quantidade;
    private Double precoUnitario;
    private Double subtotal;

    public void recalcularSubtotal(){
        this.subtotal = (precoUnitario == null ? 0.0 : precoUnitario) * quantidade;
    }

}
