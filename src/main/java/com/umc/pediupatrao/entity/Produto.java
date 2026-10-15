package com.umc.pediupatrao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;

@Document(collection = "produtos")
@Getter 
@Setter
public class Produto {
    
    @Id
    private String id;
    private String nome;
    private String tipo;
    private Double preco;
    private Boolean ativo;
}
