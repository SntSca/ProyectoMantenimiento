package com.esimedia.features.lists.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.esimedia.features.lists.entity.ListaContenidoPrivada;

import java.util.List;

@Repository
public interface ListaContenidoPrivadaRepository extends MongoRepository<ListaContenidoPrivada, String> {
    
    /**
     * Buscar todos los contenidos de una lista privada
     */
    List<ListaContenidoPrivada> findByIdLista(String idLista);
    
    /**
     * Buscar todas las listas que contienen un contenido específico
     */
    List<ListaContenidoPrivada> findByIdContenido(String idContenido);
    
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