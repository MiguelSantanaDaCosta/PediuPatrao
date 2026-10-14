package com.pediupatro.entity.enums;

import java.util.Map;
import java.util.Set;

public enum StatusPedido {
    RECEBIDO,
    EM_PREPARACAO,
    PRONTO,
    SAIU_PARA_ENTREGA,
    RETIRADO,
    FINALIZADO,
    CANCELADO;

     private static final Map<StatusPedido, Set<StatusPedido>> TRANSICOES = Map.of(
        RECEBIDO,          Set.of(EM_PREPARACAO),
        EM_PREPARACAO,     Set.of(PRONTO),
        PRONTO,            Set.of(SAIU_PARA_ENTREGA, RETIRADO),
        SAIU_PARA_ENTREGA, Set.of(FINALIZADO),
        RETIRADO,          Set.of(FINALIZADO),
        FINALIZADO,        Set.of(),
        CANCELADO,         Set.of()
    );

    public boolean permiteTransicaoPara(StatusPedido novo) {
        return TRANSICOES.getOrDefault(this, Set.of()).contains(novo);
    }

    public boolean isTerminal() {
        return this == FINALIZADO || this == CANCELADO;
    }

    /** Estados em que o pedido já saiu do estabelecimento. */
    public boolean jaSaiu() {
        return this == SAIU_PARA_ENTREGA || this == RETIRADO;
    }

}


