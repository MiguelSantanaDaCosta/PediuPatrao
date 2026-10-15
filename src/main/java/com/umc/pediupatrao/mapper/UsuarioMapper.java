package com.umc.pediupatrao.mapper;

import com.umc.pediupatrao.dto.UsuarioDTO;
import com.umc.pediupatrao.dto.UsuarioResponse;
import com.umc.pediupatrao.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    Usuario toEntity(UsuarioDTO dto);

    UsuarioResponse toResponse(Usuario entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEntity(UsuarioDTO dto, @MappingTarget Usuario entity);
}
