package com.esimedia.features.lists.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.esimedia.features.lists.entity.ListaPublica;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListaPublicaRepository extends MongoRepository<ListaPublica, String> {
    
    /**
     * Buscar lista pública por nombre
     */
    Optional<ListaPublica> findByNombre(String nombre);
    
    /**
     * Buscar listas públicas por creador
     */
    List<ListaPublica> findByIdCreadorUsuario(String idCreadorUsuario);
    
    /**
     * Buscar listas públicas por visibilidad
     */
    List<ListaPublica> findByVisibilidad(Boolean visibilidad);
    
    /**
     * Buscar listas públicas por creador y visibilidad
     */
    List<ListaPublica> findByIdCreadorUsuarioAndVisibilidad(String idCreadorUsuario, Boolean visibilidad);
    
    /**
     * Verificar si existe una lista pública con el nombre dado
     */
    boolean existsByNombre(String nombre);
}