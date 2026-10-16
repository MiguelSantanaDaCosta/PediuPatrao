package com.umc.pediupatrao.audit;

import java.util.function.Supplier;

import com.umc.pediupatrao.entity.enums.TipoOperacao;

public final class AuditContext {

    private record Ctx(String operacao, String justificativa) {
    }

    private static final ThreadLocal<Ctx> ATUAL = new ThreadLocal<>();

    private AuditContext() {
    }

    /** Executa a ação (que faz o save) com o contexto de auditoria preenchido. */
    public static <T> T run(TipoOperacao operacao, String justificativa, Supplier<T> acao) {
        ATUAL.set(new Ctx(operacao.name(), justificativa));
        try {
            return acao.get();
        } finally {
            ATUAL.remove();
        }
    }

    /** Versão sem retorno (ex.: deleteById). */
    public static void executar(TipoOperacao operacao, String justificativa, Runnable acao) {
        ATUAL.set(new Ctx(operacao.name(), justificativa));
        try {
            acao.run();
        } finally {
            ATUAL.remove();
        }
    }

    public static String operacao() {
        Ctx c = ATUAL.get();
        return c == null ? "" : c.operacao();
    }

    public static String justificativa() {
        Ctx c = ATUAL.get();
        return (c == null || c.justificativa() == null) ? "" : c.justificativa();
    }
}
