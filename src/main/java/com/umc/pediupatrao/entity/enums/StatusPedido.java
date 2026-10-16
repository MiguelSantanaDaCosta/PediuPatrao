package com.umc.pediupatrao.entity.enums;

public enum StatusPedido {
    RECEBIDO,
    EM_PREPARACAO,
    PRONTO,
    SAIU_PARA_ENTREGA,
    RETIRADO,
    FINALIZADO,
    CANCELADO;

    public boolean isTerminal() {
        return this == FINALIZADO || this == CANCELADO;
    }

    // Estados em que o pedido já saiu do estabelecimento.
    public boolean jaSaiu() {
        return this == SAIU_PARA_ENTREGA || this == RETIRADO;
    }
}
