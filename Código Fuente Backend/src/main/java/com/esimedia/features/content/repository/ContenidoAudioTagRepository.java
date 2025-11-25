package com.esimedia.features.content.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.esimedia.features.content.entity.ContenidoAudioTag;

import java.util.List;

@Repository
public interface ContenidoAudioTagRepository extends MongoRepository<ContenidoAudioTag, String> {
    
    // Buscar todas las tags de un contenido de audio específico
    List<ContenidoAudioTag> findByIdContenido(String idContenido);
    
    // Buscar todos los contenidos de audio que tienen una tag específica
    List<ContenidoAudioTag> findByIdTag(String idTag);
    
    // Verificar si existe una relación específica
    boolean existsByIdContenidoAndIdTag(String idContenido, String idTag);

    // Eliminar relación específica
    void deleteByIdContenidoAndIdTag(String idContenido, String idTag);

    // Eliminar todas las relaciones de un contenido
    void deleteByIdContenido(String idContenido);
    
    // Eliminar todas las relaciones de una tag
    void deleteByIdTag(String idTag);
}