package com.umc.pediupatrao.mapper;

import com.umc.pediupatrao.dto.ClienteDTO;
import com.umc.pediupatrao.dto.ClienteResponse;
import com.umc.pediupatrao.entity.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    @Mapping(target = "id", ignore = true)
    Cliente toEntity(ClienteDTO dto);

    ClienteResponse toResponse(Cliente entity);

    @Mapping(target = "id", ignore = true)
    void updateEntity(ClienteDTO dto, @MappingTarget Cliente entity);
}
