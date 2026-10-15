package com.umc.pediupatrao.dto;

import com.umc.pediupatrao.entity.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UsuarioDTO {

    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 60)
    private String username;

    /**
     * Obrigatório na criação; opcional na atualização
     * (validado no service).
     */
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    private String password;

    @NotNull(message = "Role é obrigatória")
    private Role role;
}
