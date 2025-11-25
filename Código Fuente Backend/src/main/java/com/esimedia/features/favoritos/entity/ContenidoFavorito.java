package com.esimedia.features.favoritos.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "contenidoFavorito")
public class ContenidoFavorito {

    @Id
    private String id;

    private String idUsuario;

    private String idContenido;
}
