package com.esimedia.features.content.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.esimedia.features.content.entity.Tags;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagsRepository extends MongoRepository<Tags, String> {
    
    // Buscar tag por nombre (único)
    Optional<Tags> findByNombre(String nombre);
    
    // Buscar tags por nombres que contengan texto
    List<Tags> findByNombreContainingIgnoreCase(String nombre);
    
    // Verificar si existe un tag por nombre
    boolean existsByNombre(String nombre);
    
    // Buscar tags por lista de IDs
    List<Tags> findByIdTagIn(List<String> ids);
}