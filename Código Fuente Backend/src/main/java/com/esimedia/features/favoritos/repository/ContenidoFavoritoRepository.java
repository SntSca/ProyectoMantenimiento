package com.esimedia.features.favoritos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.esimedia.features.favoritos.entity.ContenidoFavorito;

@Repository
public interface ContenidoFavoritoRepository extends MongoRepository<ContenidoFavorito, String> {

    List<ContenidoFavorito> findByIdUsuario(String idUsuario);

    void deleteByIdUsuario(String idUsuario);

    Optional<ContenidoFavorito> findByIdUsuarioAndIdContenido(String idUsuario, String idContenido);

    boolean existsByIdUsuarioAndIdContenido(String idUsuario, String idContenido);

    void deleteByIdUsuarioAndIdContenido(String idUsuario, String idContenido);
}
