package com.umc.pediupatrao.dto;

import com.umc.pediupatrao.entity.enums.Role;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class UsuarioResponse {
    private String id;
    private String username;
    private Role role;
}
