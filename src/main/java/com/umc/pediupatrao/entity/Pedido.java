package com.umc.pediupatrao.entity;

import com.umc.pediupatrao.entity.enums.StatusPedido;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "pedidos")
@Getter @Setter
public class Pedido {
    @Id
    private String id;
    private StatusPedido status;
    private Double valorBruto;
    private Double valorTotal;
    private Double descontoPercentual;
    private String justificativaCancelamento;
    
    private LocalDateTime dataHoraEntrada;
    private LocalDateTime dataHoraSaida;
    private String responsavelRegistro;
    private String responsavelSaida;
    
    private List<ItemPedido> itens;
}
