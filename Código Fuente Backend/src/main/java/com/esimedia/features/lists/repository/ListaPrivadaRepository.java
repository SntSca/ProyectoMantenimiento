package com.esimedia.features.lists.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.esimedia.features.lists.entity.ListaPrivada;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListaPrivadaRepository extends MongoRepository<ListaPrivada, String> {
    
    /**
     * Buscar lista privada por nombre y creador (para evitar duplicados por usuario)
     */
    Optional<ListaPrivada> findByNombreAndIdCreadorUsuario(String nombre, String idCreadorUsuario);
    
    /**
     * Buscar listas privadas por creador
     */
    List<ListaPrivada> findByIdCreadorUsuario(String idCreadorUsuario);
    
    /**
     * Buscar listas privadas por visibilidad
     */
    List<ListaPrivada> findByVisibilidad(Boolean visibilidad);
    
    /**
     * Buscar listas privadas por creador y visibilidad
     */
    List<ListaPrivada> findByIdCreadorUsuarioAndVisibilidad(String idCreadorUsuario, Boolean visibilidad);
    
    /**
     * Verificar si existe una lista privada con el nombre y creador dados
     */
    boolean existsByNombreAndIdCreadorUsuario(String nombre, String idCreadorUsuario);
}