package com.esimedia.features.content.repository;

import com.esimedia.features.content.enums.Resolucion;
import com.esimedia.features.content.entity.ContenidosVideo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContenidosVideoRepository extends MongoRepository<ContenidosVideo, String> {
    
    // Buscar por título
    Optional<ContenidosVideo> findByTitulo(String titulo);
    
    // Buscar por creador
    List<ContenidosVideo> findByIdCreador(String idCreador);
    
    // Buscar por resolución
    List<ContenidosVideo> findByResolucion(Resolucion resolucion);
    
    // Buscar por VIP
    List<ContenidosVideo> findByEsVIP(boolean esVIP);
    
    // Buscar por URL
    Optional<ContenidosVideo> findByUrlArchivo(String urlArchivo);
    
    // Verificar si existe por título
    boolean existsByTitulo(String titulo);
    
    // Verificar si existe por URL
    boolean existsByUrlArchivo(String urlArchivo);

}