package com.umc.pediupatrao.mapper;

import com.umc.pediupatrao.dto.ProdutoDTO;
import com.umc.pediupatrao.dto.ProdutoResponse;
import com.umc.pediupatrao.entity.Produto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProdutoMapper {

    @Mapping(target = "id", ignore = true)
    Produto toEntity(ProdutoDTO dto);

    ProdutoResponse toResponse(Produto entity);

    @Mapping(target = "id", ignore = true)
    void updateEntity(ProdutoDTO dto, @MappingTarget Produto entity);
}
