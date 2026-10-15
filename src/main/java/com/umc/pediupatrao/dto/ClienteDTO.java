package com.umc.pediupatrao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ClienteDTO {

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
    private String nome;

    @NotBlank(message = "O telefone é obrigatório")
    @Pattern(regexp = "\\d{10,11}", message = "Telefone deve conter 10 ou 11 dígitos")
    private String telefone;

    @Pattern(regexp = "\\d{8}", message = "CEP deve conter 8 dígitos")
    private String cep;

    @Size(max = 150)
    private String logradouro;

    @Size(max = 20)
    private String numero;

    @Size(max = 80)
    private String complemento;

    @Size(max = 80)
    private String bairro;

    @Size(max = 80)
    private String cidade;

    @Size(min = 2, max = 2, message = "Estado deve ter 2 caracteres (UF)")
    private String estado;

    private String endereco; 
}
