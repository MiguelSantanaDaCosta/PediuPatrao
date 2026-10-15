package com.umc.pediupatrao.mapper;

import com.umc.pediupatrao.dto.ItemPedidoDTO;
import com.umc.pediupatrao.dto.PedidoRequest;
import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PedidoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "valorBruto", ignore = true)
    @Mapping(target = "valorTotal", ignore = true)
    @Mapping(target = "descontoPercentual", ignore = true)
    @Mapping(target = "justificativaCancelamento", ignore = true)
    @Mapping(target = "dataHoraEntrada", ignore = true)
    @Mapping(target = "dataHoraSaida", ignore = true)
    @Mapping(target = "responsavelRegistro", ignore = true)
    @Mapping(target = "responsavelSaida", ignore = true)
    Pedido toEntity(PedidoRequest request);

    @Mapping(target = "nomeProduto", ignore = true)
    @Mapping(target = "precoUnitario", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    ItemPedido toEntity(ItemPedidoDTO dto);
}
