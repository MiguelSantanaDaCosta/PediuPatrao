package com.umc.pediupatrao.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javers.core.Javers;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.PropertyChange;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.diff.changetype.container.ContainerChange;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.stereotype.Service;

import com.umc.pediupatrao.dto.RegistroAuditoria;
import com.umc.pediupatrao.entity.AlteracaoCampo;
import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.exception.DominioException;

/** Somente LEITURA. Quem grava é o JaVers (@JaversSpringDataAuditable). */
@Service
public class AuditoriaService {

    // Defesa extra: nunca exibir credenciais, mesmo que um campo escape do
    // @DiffIgnore
    private static final Set<String> CAMPOS_PROIBIDOS = Set.of("senha", "password", "token", "secret", "credencial",
            "authorization");

    private final Javers javers;

    public AuditoriaService(Javers javers) {
        this.javers = javers;
    }

    public List<RegistroAuditoria> listar() {
        return converter(javers.findChanges(
                QueryBuilder.byClass(Pedido.class, Cliente.class, Usuario.class)
                        .withNewObjectChanges()
                        .limit(1000)
                        .build()));
    }

    public List<RegistroAuditoria> listarPorEntidade(String entidade) {
        return converter(javers.findChanges(
                QueryBuilder.byClass(classe(entidade))
                        .withNewObjectChanges()
                        .limit(1000)
                        .build()));
    }

    public List<RegistroAuditoria> listarPorInstancia(String entidade, String id) {
        return converter(javers.findChanges(
                QueryBuilder.byInstanceId(id, classe(entidade))
                        .withNewObjectChanges()
                        .limit(1000)
                        .build()));
    }

    // ---------- internos ----------

    private Class<?> classe(String entidade) {
        if (entidade == null) {
            throw new DominioException("Entidade inválida.");
        }
        return switch (entidade.toLowerCase()) {
            case "pedido" -> Pedido.class;
            case "cliente" -> Cliente.class;
            case "usuario", "usuário" -> Usuario.class;
            default -> throw new DominioException("Entidade inválida: " + entidade);
        };
    }

    /** Agrupa as mudanças por commit + objeto (um registro por evento). */
    private List<RegistroAuditoria> converter(List<Change> changes) {
        Map<String, List<Change>> grupos = new LinkedHashMap<>();
        for (Change c : changes) {
            String commit = c.getCommitMetadata().map(m -> m.getId().value()).orElse("?");
            String chave = commit + "|" + raiz(c.getAffectedGlobalId().value());
            grupos.computeIfAbsent(chave, k -> new ArrayList<>()).add(c);
        }

        List<RegistroAuditoria> out = new ArrayList<>();
        for (List<Change> grupo : grupos.values()) {
            out.add(montar(grupo));
        }

        out.sort(Comparator.comparing(RegistroAuditoria::dataHora,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return out;
    }

    private RegistroAuditoria montar(List<Change> grupo) {
        Change primeira = grupo.get(0);
        CommitMetadata meta = primeira.getCommitMetadata().orElse(null);
        Map<String, String> props = (meta == null) ? Map.of() : meta.getProperties();

        String raiz = raiz(primeira.getAffectedGlobalId().value());
        int barra = raiz.indexOf('/');
        String tipo = barra < 0 ? raiz : raiz.substring(0, barra);
        String id = barra < 0 ? "" : raiz.substring(barra + 1);
        String entidade = tipo.substring(tipo.lastIndexOf('.') + 1);

        List<AlteracaoCampo> campos = new ArrayList<>();
        for (Change c : grupo) {
            if (!(c instanceof PropertyChange<?> pc) || c instanceof ContainerChange<?>) {
                continue; // inclusões/remoções do objeto e listas: os itens aparecem individualmente
            }
            String propriedade = pc.getPropertyName();
            if (CAMPOS_PROIBIDOS.contains(propriedade.toLowerCase())) {
                continue;
            }
            String caminho = fragmento(c.getAffectedGlobalId().value());
            String campo = caminho.isEmpty() ? propriedade : caminho + "." + propriedade;

            if (c instanceof ValueChange vc) {
                campos.add(new AlteracaoCampo(campo, texto(vc.getLeft()), texto(vc.getRight())));
            } else {
                campos.add(new AlteracaoCampo(campo, null, c.toString()));
            }
        }

        return new RegistroAuditoria(
                meta == null ? "" : meta.getId().value(),
                meta == null ? null : meta.getCommitDate(),
                meta == null ? "SISTEMA" : meta.getAuthor(),
                props.getOrDefault("perfil", "N/A"),
                vazioParaTraco(props.get("operacao")),
                entidade,
                id,
                campos,
                props.get("justificativa") == null || props.get("justificativa").isBlank()
                        ? null
                        : props.get("justificativa"));
    }

    // "pkg.Pedido/123#itens/0" -> "pkg.Pedido/123"
    private String raiz(String globalId) {
        int i = globalId.indexOf('#');
        return i < 0 ? globalId : globalId.substring(0, i);
    }

    // "pkg.Pedido/123#itens/0" -> "itens/0"
    private String fragmento(String globalId) {
        int i = globalId.indexOf('#');
        return i < 0 ? "" : globalId.substring(i + 1);
    }

    private String texto(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private String vazioParaTraco(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}
