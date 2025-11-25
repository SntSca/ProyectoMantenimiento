package com.esimedia.features.lists.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.esimedia.features.lists.entity.ListaContenidoPublica;

import java.util.List;

@Repository
public interface ListaContenidoPublicaRepository extends MongoRepository<ListaContenidoPublica, String> {
    
    /**
     * Buscar todos los contenidos de una lista pública
     */
    List<ListaContenidoPublica> findByIdLista(String idLista);
    
    /**
     * Buscar todas las listas que contienen un contenido específico
     */
    List<ListaContenidoPublica> findByIdContenido(String idContenido);
    
    /**
     * Verificar si un contenido específico está en una lista específica
     */
    boolean existsByIdListaAndIdContenido(String idLista, String idContenido);
    
    /**
     * Eliminar contenido específico de una lista específica
     */
    void deleteByIdListaAndIdContenido(String idLista, String idContenido);
    
    /**
     * Eliminar todos los contenidos de una lista
     */
    void deleteByIdLista(String idLista);
    
    /**
     * Contar contenidos en una lista
     */
    long countByIdLista(String idLista);
}